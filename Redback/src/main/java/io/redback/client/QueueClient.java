package io.redback.client;

import io.firebus.Firebus;
import io.firebus.data.DataMap;
import io.redback.exceptions.RedbackException;
import io.redback.security.Session;

public class QueueClient extends Client {

	public QueueClient(Firebus fb, String sn) 
	{
		super(fb, sn);
	}
	
	public void enqueue(Session session, String service, DataMap message) throws RedbackException {
		try {
			DataMap req = new DataMap();
			req.put("action", "enqueue");
			req.put("service", service);
			req.put("message", message);
			requestDataMap(session, req);
		} catch(Exception e) {
			throw new RedbackException("Error enqueing message", e);
		}
	}

	
}
