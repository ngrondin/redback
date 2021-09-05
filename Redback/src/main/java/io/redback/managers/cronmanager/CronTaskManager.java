package io.redback.managers.cronmanager;

import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.logging.Logger;

import javax.script.Bindings;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;

import io.firebus.Firebus;
import io.firebus.Payload;
import io.firebus.data.DataList;
import io.firebus.data.DataMap;
import io.redback.client.AccessManagementClient;
import io.redback.client.ConfigurationClient;
import io.redback.client.DataClient;
import io.redback.exceptions.RedbackException;
import io.redback.security.Session;
import io.redback.security.UserProfile;
import io.redback.security.js.UserProfileJSWrapper;
import io.redback.utils.CollectionConfig;
import io.redback.utils.StringUtils;
import io.redback.utils.js.FirebusJSWrapper;
import io.redback.utils.js.LoggerJSFunction;

public class CronTaskManager extends Thread {

	private Logger logger = Logger.getLogger("io.redback");
	protected String uuid;
	protected Firebus firebus;
	protected DataMap config;
	protected ScriptEngine jsEngine;
	protected String configServiceName;
	protected String dataServiceName;
	protected String accessManagerServiceName;
	protected String cronUserName;
	protected String jwtSecret;
	protected String jwtIssuer;
	protected String cronUserToken;
	protected UserProfile cronUserProfile;
	protected CollectionConfig collectionConfig;
	protected DataClient dataClient;
	protected ConfigurationClient configClient;
	protected AccessManagementClient accessManagementClient;
	protected Map<String, CronTaskConfig> taskConfigs;
	protected boolean quit = false;
	protected long randomDelay;


	public CronTaskManager(Firebus fb, DataMap c)
	{
		uuid = UUID.randomUUID().toString();
		firebus = fb;
		config = c;
		jsEngine = new ScriptEngineManager().getEngineByName("graal.js");
		configServiceName = config.getString("configservice");
		dataServiceName = config.getString("dataservice");
		accessManagerServiceName = config.getString("accessmanagementservice");
		cronUserName = config.getString("cronuser");
		jwtSecret = config.getString("jwtsecret");
		jwtIssuer = config.getString("jwtissuer");
		dataClient = new DataClient(firebus, dataServiceName);
		configClient = new ConfigurationClient(firebus, configServiceName);
		accessManagementClient = new AccessManagementClient(firebus, accessManagerServiceName);
		collectionConfig = new CollectionConfig(config.getObject("collection"), "rbcr_task");
		taskConfigs = new HashMap<String, CronTaskConfig>();
		randomDelay = (new Random()).nextInt(5000);
		setName("rbCronTaskManager");
	}
	
	public ScriptEngine getScriptEngine()
	{
		return jsEngine;
	}

	
	public Session getSystemUserSession() throws RedbackException 
	{
		Session session = new Session();
		if(cronUserProfile != null  &&  cronUserProfile.getExpiry() < System.currentTimeMillis())
			cronUserProfile = null;

		if(cronUserProfile == null)
		{
			try
			{
				Algorithm algorithm = Algorithm.HMAC256(jwtSecret);
				cronUserToken = JWT.create()
						.withIssuer(jwtIssuer)
						.withClaim("email", cronUserName)
						.withExpiresAt(new Date(System.currentTimeMillis() + 3600000))
						.sign(algorithm);
				cronUserProfile = accessManagementClient.validate(session, cronUserToken);
			}
			catch(Exception e)
			{
				throw new RedbackException("Error authenticating sys user", e);
			}
		}
		session.setToken(cronUserToken);
		session.setUserProfile(cronUserProfile);
		return session;
	}

	public void run() {
		try {
			loadConfigs(getSystemUserSession());
			while(!quit) {
				try {
					long current = System.currentTimeMillis() - randomDelay;
					long nextTime = current + 86400000;
					refreshTaskStates();
					Iterator<String> it = taskConfigs.keySet().iterator();
					while(it.hasNext()) {
						String name = it.next();
						CronTaskConfig ctc = taskConfigs.get(name);
						if(ctc.getNextRun() < current && ctc.getLock() == null) {
							if(lockTask(ctc)) {
								try {
									runTask(ctc);
								} catch(Exception e) {
									logger.severe(StringUtils.rollUpExceptions(e));
								}
								if(ctc.getPeriod() > 0) {
									ctc.setNextRun(current + ctc.getPeriod());
									unlockTask(ctc);
								} else {
									deleteTask(ctc);
								}
							}
						}
						if(ctc.getNextRun() < nextTime)
							nextTime = ctc.getNextRun();
					}
					long sleep = nextTime - System.currentTimeMillis();
					if(sleep < 10000)
						sleep = 10000;
					sleep += randomDelay;
					Thread.sleep(sleep);
				} catch(Exception e) {
					logger.severe("General error in CronTaskManager thread : " + StringUtils.rollUpExceptions(e));
				}
			}
		} catch(Exception e) {
			logger.severe("Cron task manager cannot load configured tasks : " + StringUtils.rollUpExceptions(e));
		}
	}
	
	
	public void loadConfigs(Session session) throws RedbackException {
		try {
			taskConfigs.clear();
			DataMap resp = configClient.listConfigs(session, "rbcr", "task");
			DataList configList = resp.getList("result");
			for(int i = 0; i < configList.size(); i++) {
				CronTaskConfig ctc = new CronTaskConfig(this, configList.getObject(i));
				taskConfigs.put(ctc.getName(), ctc);
			}
		} catch(Exception e) {
			throw new RedbackException("Error loading the cron configs", e);
		}
	}
	
	protected void refreshTaskStates() throws RedbackException {
		try {
			DataMap resp = dataClient.getData(collectionConfig.getName(), new DataMap("nextrun", new DataMap("$ne", null)), null);
			DataList list = resp.getList("result");
			for(int i = 0; i < list.size(); i++) {
				DataMap taskState = collectionConfig.convertObjectToCanonical(list.getObject(i));
				String name = taskState.getString("name");
				CronTaskConfig ctc = taskConfigs.get(name);
				if(ctc == null) {
					ctc = new CronTaskConfig(this, taskState); 
					taskConfigs.put(ctc.getName(), ctc);
				} else {
					long nextRun = taskState.getDate("nextrun").getTime();
					String lock = taskState.getString("lock");
					ctc.setNextRun(nextRun);
					ctc.setLock(lock);
				}				
			}
		} catch(Exception e) {
			throw new RedbackException("Error when refreshing last run times", e);
		}
	}
	
	protected boolean lockTask(CronTaskConfig ctc) throws RedbackException {
		try {
			DataMap key = new DataMap("name", ctc.getName());
			DataMap resp = dataClient.getData(collectionConfig.getName(), collectionConfig.convertObjectToSpecific(key), null);
			DataList list = resp.getList("result");
			if(list.size() == 0) {
				DataMap data = new DataMap();
				data.put("name", ctc.getName());
				data.put("nextrun", new Date(ctc.getNextRun()));
				data.put("lock", uuid);
				dataClient.putData(collectionConfig.getName(), collectionConfig.convertObjectToSpecific(key), collectionConfig.convertObjectToSpecific(data));
				return true;
			} else {
				DataMap dataItem = list.getObject(0);
				String lock = dataItem.getString("lock");
				if(lock == null) {
					DataMap data = new DataMap();
					data.put("lock", uuid);
					dataClient.putData(collectionConfig.getName(), collectionConfig.convertObjectToSpecific(key), collectionConfig.convertObjectToSpecific(data));
					ctc.setLock(uuid);
					return true;
				} else {
					return false;
				}
			}
		} catch(Exception e) {
			throw new RedbackException("Error when trying to lock task", e);
		}
	}
	
	protected void runTask(CronTaskConfig ctc) throws RedbackException {
		try {
			Session session = getSystemUserSession();
			if(ctc.getScript() != null) {
				Bindings scriptContext = jsEngine.createBindings();
				scriptContext.put("userprofile", new UserProfileJSWrapper(session.getUserProfile()));
				scriptContext.put("firebus", new FirebusJSWrapper(firebus, session));
				scriptContext.put("log", new LoggerJSFunction());
				ctc.getScript().eval(scriptContext);
			} else if(ctc.getFirebusCall() != null) {
				DataMap call = ctc.getFirebusCall();
				String serviceName = config.getString(call.getString("service"));
				Payload req = new Payload(call.getObject("payload").toString());
				req.metadata.put("mime", "application/json");
				boolean faf = call.getBoolean("fireandforget");
				req.metadata.put("token", session.getToken());
				if(faf)
					firebus.requestServiceAndForget(serviceName, req);
				else
					firebus.requestService(serviceName, req);
			}
		} catch (Exception e) {
			throw new RedbackException("Error running the cron task", e);
		}
	}
	
	protected void unlockTask(CronTaskConfig ctc) throws RedbackException {
		DataMap key = new DataMap();
		key.put("name", ctc.getName());
		DataMap data = new DataMap();
		data.put("lock", null);
		data.put("nextrun", new Date(ctc.getNextRun()));
		dataClient.putData(collectionConfig.getName(), collectionConfig.convertObjectToSpecific(key), collectionConfig.convertObjectToSpecific(data));
		ctc.setLock(null);
	}
	
	protected void deleteTask(CronTaskConfig ctc) throws RedbackException {
		DataMap key = new DataMap();
		key.put("name", ctc.getName());
		DataMap data = new DataMap();
		data.put("lock", null);
		data.put("nextrun", null);
		dataClient.putData(collectionConfig.getName(), collectionConfig.convertObjectToSpecific(key), collectionConfig.convertObjectToSpecific(data));
		taskConfigs.remove(ctc.getName());
	}	

	public void clearCaches() {
		try {
			loadConfigs(getSystemUserSession());
		} catch(RedbackException e) {
			logger.severe("Cannot clear config caches: " + e.getMessage());
		}
	}
}
