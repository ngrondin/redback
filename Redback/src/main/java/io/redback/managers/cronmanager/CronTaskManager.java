package io.redback.managers.cronmanager;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

import io.firebus.Firebus;
import io.firebus.Payload;
import io.firebus.data.DataList;
import io.firebus.data.DataMap;
import io.firebus.exceptions.FunctionErrorException;
import io.firebus.exceptions.FunctionTimeoutException;
import io.firebus.logging.Logger;
import io.redback.client.AccessManagementClient;
import io.redback.client.ConfigClient;
import io.redback.client.DataClient;
import io.redback.exceptions.RedbackException;
import io.redback.security.Session;
import io.redback.security.SysUserManager;
import io.redback.security.UserProfile;
import io.redback.utils.CollectionConfig;

public class CronTaskManager extends Thread {

	protected String uuid;
	protected Firebus firebus;
	protected DataMap config;
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
		configServiceName = config.getString("configservice");
		dataServiceName = config.getString("dataservice");
		accessManagerServiceName = config.getString("accessmanagementservice");
		dataClient = new DataClient(firebus, dataServiceName);
		configClient = new ConfigClient(firebus, configServiceName);
		accessManagementClient = new AccessManagementClient(firebus, accessManagerServiceName);
		sysUserManager = new SysUserManager(accessManagementClient, config);
		collectionConfig = new CollectionConfig(dataClient, config.getObject("collection"), "rbcr_task");
		taskConfigs = new HashMap<String, CronTaskConfig>();
		randomDelay = (new Random()).nextInt(5000);
		setName("rbCronTaskManager");
	}


	public void run() {
		try {
			loadConfigs(sysUserManager.getSession());
			while(!quit) {
				try {
					long current = System.currentTimeMillis();
					long nextTime = current + 86400000;
					refreshTaskStates();
					for(String name: taskConfigs.keySet()) {
						CronTaskConfig ctc = taskConfigs.get(name);
						if(ctc.getNextRun() < current + randomDelay) {
							if(lockTask(ctc)) {
								try {
									runTask(ctc);
								} catch(Exception e) {
									Logger.severe("rb.cron.run", "Error while running cron task", e);
								} finally {
									unlockTask(ctc, current + ctc.getPeriod() + randomDelay);									
								}
							}
						}
						if(ctc.getNextRun() < nextTime)
							nextTime = ctc.getNextRun();
					}
					long sleep = nextTime - System.currentTimeMillis();
					if(sleep < 10000)
						sleep = 10000;
					sleep -= randomDelay;
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
		taskConfigs.clear();
		DataMap cfgresp = configClient.listConfigs(session, "rbcr", "task");
		DataList configList = cfgresp.getList("result");
		for(int i = 0; i < configList.size(); i++) {
			CronTaskConfig ctc = new CronTaskConfig(this, configList.getObject(i));
			if(ctc.getPeriod() > 0)
				taskConfigs.put(ctc.getName(), ctc);
		}
	}
	
	protected void refreshTaskStates() throws RedbackException {
		DataMap resp = collectionConfig.getData(new DataMap("nextrun", new DataMap("$ne", null)));
		DataList list = resp.getList("result");
		for(int i = 0; i < list.size(); i++) {
			DataMap taskState = list.getObject(i);
			String name = taskState.getString("name");
			CronTaskConfig ctc = taskConfigs.get(name);
			if(ctc != null) {
				long nextRun = taskState.getDate("nextrun").getTime();
				ctc.setNextRun(nextRun);
			}				
		}
	}
	
	protected boolean lockTask(CronTaskConfig ctc) throws RedbackException {
		DataMap key = new DataMap("name", ctc.getName(), "lock", null);
		DataMap data = new DataMap("lock", uuid, "lockdate", new Date());
		collectionConfig.putData(key, data, "update");
		DataMap resp = collectionConfig.getData(new DataMap("name", ctc.getName()));
		DataList result = resp.getList("result");
		if(result.size() > 0) {
			DataMap lockData = result.getObject(0); 
			String lock = lockData.getString("lock");
			if(lock != null && lock.equals(uuid)) {
				return true;
			} else { 
				if(lock != null) {
					Date lockDate = lockData.getDate("lockdate");
					if(lockDate != null && lockDate.getTime() < System.currentTimeMillis() - ctc.getPeriod()) {
						key = new DataMap("name", ctc.getName());
						data = new DataMap("lock", null, "lockdate", null);
						collectionConfig.putData(key, data);
					}
				}
				return false;
			}
		} else {
			collectionConfig.putData(key, data);
			return true;
		}
	}
	
	protected void runTask(CronTaskConfig ctc) throws RedbackException, FunctionErrorException, FunctionTimeoutException {
		Session session = sysUserManager.getSession();
		if(ctc.getFirebusCall() != null) {
			DataMap call = ctc.getFirebusCall();
			String serviceName = config.getString(call.getString("service"));
			Payload req = new Payload(call.getObject("payload"));
			boolean faf = call.containsKey("fireandforget") ? call.getBoolean("fireandforget") : false;
			int timeout = call.containsKey("timeout") ? call.getNumber("timeout").intValue() : 60000;
			req.metadata.put("session", session.id);
			req.metadata.put("token", session.getToken());
			req.metadata.put("mime", "application/json");
			Logger.info("rb.cron.runtask", new DataMap("task", ctc.getName()));
			if(faf)
				firebus.requestServiceAndForget(serviceName, req);
			else
				firebus.requestService(serviceName, req, timeout);
		}
	}
	
	protected void unlockTask(CronTaskConfig ctc, long nextRun) throws RedbackException {
		DataMap key = new DataMap("name", ctc.getName());
		DataMap data = new DataMap("lock", null, "lockdate", null, "nextrun", new Date(nextRun));
		collectionConfig.putData(key, data);
		ctc.setNextRun(nextRun);
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
			this.loadConfigs(null);
		} catch(Exception e) {
			
		}
	}
}
