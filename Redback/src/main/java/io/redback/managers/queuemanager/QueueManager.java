package io.redback.managers.queuemanager;

import java.util.Date;
import java.util.UUID;

import io.firebus.Firebus;
import io.firebus.Payload;
import io.firebus.data.DataList;
import io.firebus.data.DataMap;
import io.firebus.logging.Logger;
import io.redback.client.AccessManagementClient;
import io.redback.client.DataClient;
import io.redback.exceptions.RedbackException;
import io.redback.exceptions.RedbackUnauthorisedException;
import io.redback.security.Session;
import io.redback.security.SysUserManager;
import io.redback.security.UserProfile;
import io.redback.utils.CollectionConfig;

public class QueueManager extends Thread {
	protected String uuid;
	protected Firebus firebus;
	protected DataMap config;
	protected DataClient dataClient;
	protected CollectionConfig collection;
	protected AccessManagementClient accessManagementClient;
	protected SysUserManager sysUserManager;
	
	public QueueManager(DataMap c, Firebus f) {
		uuid = UUID.randomUUID().toString();
		firebus = f;
		config = c;
		dataClient = new DataClient(firebus, config.getString("dataservice"));
		collection = new CollectionConfig(dataClient, config.getObject("collection"), "rbq_message");
		accessManagementClient = new AccessManagementClient(firebus, config.getString("accessmanagementservice"));
		sysUserManager = new SysUserManager(accessManagementClient, config);
		setName("rbQueue");
		start();
	}
	
	public synchronized void enqueue(Session session, String service, DataMap message, int requestTimeout, Date schedule, String uniqueKey) throws RedbackException {
		String uuid = null;
		if(uniqueKey != null) 
			uuid = getUUIDofUniqueKey(uniqueKey);
		if(uuid == null)
			uuid = UUID.randomUUID().toString();
		DataMap key = new DataMap("_id", uuid);
		DataMap data = new DataMap();
		data.put("session", session.id);
		data.put("token", session.token);
		if(session.getDomainLock() != null)
			data.put("domain", session.getDomainLock());
		if(session.getTimezone() != null)
			data.put("timezone", session.getTimezone());
		data.put("service", service);
		data.put("message", message);
		if(requestTimeout > 0)
			data.put("timeout", requestTimeout);
		if(schedule != null) 
			data.put("schedule", schedule);
		if(uniqueKey != null)
			data.put("uniquekey", uniqueKey);
		data.put("lock", null);
		data.put("failed", null);
		collection.putData(key, data);
		if(schedule == null)
			notify();
	}
	
	public void run() {
		while(true) {
			try {
				String msgUuid = null;
				while((msgUuid = getNextUuid()) != null) {
					processMessage(msgUuid);
				}
				synchronized(this) {
					wait(10000);
				}
			} catch(Exception e) {
				Logger.severe("rb.rbq.run", e);
			}
		}
	}
	
	protected String getNextUuid() throws RedbackException {
		DataList scheduleOrClauses = new DataList();
		scheduleOrClauses.add(new DataMap("schedule", null));
		scheduleOrClauses.add(new DataMap("schedule", new DataMap("$lt", new Date())));
		DataMap resp = collection.getData(new DataMap("lock", null, "failed", null, "$or", scheduleOrClauses), 0, 1);
		DataList list = resp.getList("result");
		if(list.size() > 0) {
			return list.getObject(0).getString("_id");
		} else {
			return null;
		}
	}
	
	protected void processMessage(String msgUuid) throws RedbackException {
		DataMap msg = lockAndGet(msgUuid);
		if(msg != null) {
			try {
				String service = msg.getString("service");
				DataMap message = msg.getObject("message");
				int requestTimeout = msg.containsKey("timeout") ? msg.getNumber("timeout").intValue() : 10000;
				String token = checkToken(msg.getString("token"));
				Payload payload = new Payload(message);
				payload.metadata.put("session", msg.getString("session"));
				payload.metadata.put("token", token);
				if(msg.containsKey("timezone"))
					payload.metadata.put("timezone", msg.getString("timezone"));
				if(msg.containsKey("domain"))
					payload.metadata.put("domain", msg.getString("domain"));
				if(requestTimeout > 0) firebus.requestService(service, payload, requestTimeout);
				else firebus.requestService(service, payload);
				remove(msgUuid);
			} catch(Exception e) {
				setFailed(msgUuid);
			}
		}
	}
	
	protected DataMap lockAndGet(String msgUuid) throws RedbackException {
		collection.putData(new DataMap("_id", msgUuid, "lock", null, "failed", null), new DataMap("lock", uuid));
		DataMap checkResp = collection.getData(new DataMap("_id", msgUuid, "lock", uuid));
		if(checkResp.getList("result").size() > 0) {
			return checkResp.getList("result").getObject(0);
		} else {
			return null;
		}
	}
	
	protected String getUUIDofUniqueKey(String uniqueKey) throws RedbackException {
		DataMap resp = collection.getData(new DataMap("uniquekey", uniqueKey));
		if(resp.getList("result").size() > 0) {
			return resp.getList("result").getObject(0).getString("_id");
		} else {
			return null;
		}
	}
	
	protected void remove(String msgUuid) throws RedbackException {
		collection.deleteData(new DataMap("_id", msgUuid));
	}
	
	protected void setFailed(String msgUuid) throws RedbackException {
		collection.putData(new DataMap("_id", msgUuid), new DataMap("lock", null, "failed", new Date()));
	}
	
	protected String checkToken(String token) throws RedbackException {
		UserProfile up = accessManagementClient.validate(new Session(), token);
		if(up.getExpiry() > System.currentTimeMillis() + 10000) {
			return token;
		} else {
			Session sysUserSession = sysUserManager.getSession();
			if(up.getUsername().equals(sysUserSession.getUserProfile().getUsername())) {
				return sysUserSession.getToken();				
			} else {
				throw new RedbackUnauthorisedException("Queue message token expired");
			}
		}
	}
}
