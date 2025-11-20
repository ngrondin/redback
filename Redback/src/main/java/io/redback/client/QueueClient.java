package io.redback.client;

import java.util.Date;

import io.firebus.Firebus;
import io.firebus.data.DataMap;
import io.redback.exceptions.RedbackException;
import io.redback.security.Session;

public class QueueClient extends Client {

	public QueueClient(Firebus fb, String sn) 
	{
		super(fb, sn);
	}
	
	public void enqueue(Session session, String service, DataMap message, long timeout) throws RedbackException {
		enqueue(session, service, message, timeout, null, null);
	}
	
	public void enqueue(Session session, String service, DataMap message, long timeout, Date schedule, String uniqueKey) throws RedbackException {
		try {
			DataMap req = new DataMap();
			req.put("action", "enqueue");
			req.put("service", service);
			req.put("message", message);
			if(timeout > 0)
				req.put("timeout", timeout);
			if(schedule != null)
				req.put("schedule", schedule);
			if(uniqueKey != null)
				req.put("uniquekey", uniqueKey);
			requestDataMap(session, req);
		} catch(Exception e) {
			throw new RedbackException("Error enqueing message", e);
		}
	}

	
}
