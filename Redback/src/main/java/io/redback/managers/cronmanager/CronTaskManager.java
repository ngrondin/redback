package io.redback.managers.cronmanager;

import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

import javax.script.Bindings;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;

import io.firebus.Firebus;
import io.firebus.Payload;
import io.firebus.data.DataList;
import io.firebus.data.DataMap;
import io.firebus.logging.Logger;
import io.redback.client.AccessManagementClient;
import io.redback.client.ConfigClient;
import io.redback.client.DataClient;
import io.redback.exceptions.RedbackException;
import io.redback.security.Session;
import io.redback.security.SysUserManager;
import io.redback.security.UserProfile;
import io.redback.security.js.UserProfileJSWrapper;
import io.redback.utils.CollectionConfig;
import io.redback.utils.js.FirebusJSWrapper;
import io.redback.utils.js.LoggerJSFunction;

public class CronTaskManager extends Thread {

	protected String uuid;
	protected Firebus firebus;
	protected DataMap config;
	protected ScriptEngine jsEngine;
	protected String configServiceName;
	protected String dataServiceName;
	protected String accessManagerServiceName;
	protected String cronUserToken;
	protected UserProfile cronUserProfile;
	protected CollectionConfig collectionConfig;
	protected DataClient dataClient;
	protected ConfigClient configClient;
	protected AccessManagementClient accessManagementClient;
	protected Map<String, CronTaskConfig> taskConfigs;
	protected boolean quit = false;
	protected long randomDelay;
	protected SysUserManager sysUserManager;


	public CronTaskManager(Firebus fb, DataMap c)
	{
		uuid = UUID.randomUUID().toString();
		firebus = fb;
		config = c;
		jsEngine = new ScriptEngineManager().getEngineByName("graal.js");
		configServiceName = config.getString("configservice");
		dataServiceName = config.getString("dataservice");
		accessManagerServiceName = config.getString("accessmanagementservice");
		dataClient = new DataClient(firebus, dataServiceName);
		configClient = new ConfigClient(firebus, configServiceName);
		accessManagementClient = new AccessManagementClient(firebus, accessManagerServiceName);
		sysUserManager = new SysUserManager(accessManagementClient, config);
		collectionConfig = new CollectionConfig(config.getObject("collection"), "rbcr_task");
		taskConfigs = new HashMap<String, CronTaskConfig>();
		randomDelay = (new Random()).nextInt(5000);
		setName("rbCronTaskManager");
	}
	
	public ScriptEngine getScriptEngine()
	{
		return jsEngine;
	}

	public void run() {
		try {
			loadConfigs(sysUserManager.getSession());
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
									Logger.severe("rb.cron.run", "Error while running cron task", e);
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
					Logger.severe("rb.cron.run", "General error in CronTaskManager thread", e);
				}
			}
		} catch(Exception e) {
			Logger.severe("rb.cron.run", "Cron task manager cannot load configured tasks" , e);
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
			Session session = sysUserManager.getSession();
			if(ctc.getScript() != null) {
				Bindings scriptContext = jsEngine.createBindings();
				scriptContext.put("userprofile", new UserProfileJSWrapper(session.getUserProfile()));
				scriptContext.put("firebus", new FirebusJSWrapper(firebus, session));
				scriptContext.put("log", new LoggerJSFunction());
				ctc.getScript().eval(scriptContext);
			} else if(ctc.getFirebusCall() != null) {
				DataMap call = ctc.getFirebusCall();
				String serviceName = config.getString(call.getString("service"));
				Payload req = new Payload(call.getObject("payload"));
				req.metadata.put("session", session.id);
				req.metadata.put("token", session.getToken());
				req.metadata.put("mime", "application/json");
				boolean faf = call.getBoolean("fireandforget");
				Logger.info("rb.cron.runtask", new DataMap("task", ctc.getName(), "token", session.getToken()));
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
			loadConfigs(sysUserManager.getSession());
		} catch(RedbackException e) {
			Logger.severe("rb.cron.clearcache", "Cannot clear config caches" , e);
		}
	}
}
