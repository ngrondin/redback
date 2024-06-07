package io.redback.client.js;

import io.firebus.data.DataMap;
import io.redback.client.QueueClient;
import io.redback.exceptions.RedbackException;
import io.redback.security.Session;
import io.redback.utils.js.CallableJSWrapper;
import io.redback.utils.js.ObjectJSWrapper;

public class QueueClientJSWrapper extends ObjectJSWrapper {
	protected QueueClient queueClient;
	protected Session session;

	
	public QueueClientJSWrapper(QueueClient qc, Session s)
	{
		super(new String[] {"enqueue"});
		queueClient = qc;
		session = s;
	}
	
	public Object get(String key) {
		if(key.equals("enqueue")) {
			return new CallableJSWrapper() {
				public Object call(Object... arguments) throws RedbackException {
					String service = (String)arguments[0];
					DataMap message = (DataMap)arguments[1];
					queueClient.enqueue(session, service, message);
					return null;
				}
			};
		} else {
			return null;
		}
	}


}
