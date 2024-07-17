package io.redback.managers.queuemanager;

import java.util.Date;
import java.util.UUID;

import io.firebus.Firebus;
import io.firebus.Payload;
import io.firebus.data.DataList;
import io.firebus.data.DataMap;
import io.firebus.logging.Logger;
import io.redback.client.DataClient;
import io.redback.exceptions.RedbackException;
import io.redback.security.Session;
import io.redback.utils.CollectionConfig;

public class QueueManager extends Thread {
	protected String uuid;
	protected Firebus firebus;
	protected DataMap config;
	protected DataClient dataClient;
	protected CollectionConfig collection;
	
	public QueueManager(DataMap c, Firebus f) {
		uuid = UUID.randomUUID().toString();
		firebus = f;
		config = c;
		dataClient = new DataClient(firebus, config.getString("dataservice"));
		collection = new CollectionConfig(dataClient, config.getObject("collection"), "rbq_message");
		setName("rbQueue");
		start();
	}
	
	public synchronized void enqueue(Session session, String service, DataMap message, int requestTimeout) throws RedbackException {
		String msgUuid = UUID.randomUUID().toString();
		DataMap key = new DataMap("_id", msgUuid);
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
		data.put("lock", null);
		data.put("failed", null);
		collection.putData(key, data);
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
					wait(30000);
				}
			} catch(Exception e) {
				Logger.severe("rb.rbq.run", e);
			}
		}
	}
	
	protected void processMessage(String msgUuid) throws RedbackException {
		DataMap msg = get(msgUuid);
		if(msg != null) {
			String service = msg.getString("service");
			DataMap message = msg.getObject("message");
			int requestTimeout = msg.getNumber("timeout").intValue();
			Payload payload = new Payload(message);
			payload.metadata.put("session", msg.getString("session"));
			payload.metadata.put("token", msg.getString("token"));
			if(msg.containsKey("timezone"))
				payload.metadata.put("timezone", msg.getString("timezone"));
			if(msg.containsKey("domain"))
				payload.metadata.put("domain", msg.getString("domain"));
			try {
				if(requestTimeout > 0) firebus.requestService(service, payload, requestTimeout);
				else firebus.requestService(service, payload);
				remove(msgUuid);
			} catch(Exception e) {
				setFailed(msgUuid);
			}
		}
	}
	
	protected String getNextUuid() throws RedbackException {
		DataMap resp = collection.getData(new DataMap("lock", null, "failed", null, 0, 1));
		DataList list = resp.getList("result");
		if(list.size() > 0) {
			return list.getObject(0).getString("_id");
		} else {
			return null;
		}
	}
	
	protected DataMap get(String msgUuid) throws RedbackException {
		collection.putData(new DataMap("_id", msgUuid, "lock", null, "failed", null), new DataMap("lock", uuid));
		DataMap checkResp = collection.getData(new DataMap("_id", msgUuid, "lock", uuid));
		if(checkResp.getList("result").size() > 0) {
			return checkResp.getList("result").getObject(0);
		} else {
			return null;
		}
	}
	
	protected void remove(String msgUuid) throws RedbackException {
		collection.deleteData(new DataMap("_id", msgUuid));
	}
	
	protected void setFailed(String msgUuid) throws RedbackException {
		collection.putData(new DataMap("_id", msgUuid), new DataMap("failed", new Date()));
	}
}
