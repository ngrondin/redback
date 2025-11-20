package io.redback.client.js;

import java.util.Date;

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
					long timeout = arguments.length >= 3 ? (long)arguments[2] : 0;
					Date schedule = arguments.length >= 4 ? (Date)arguments[3] : null;
					String uniqueKey = arguments.length >= 5 ? (String)arguments[4] : null;
					queueClient.enqueue(session, service, message, timeout, schedule, uniqueKey);
					return null;
				}
			};
		} else {
			return null;
		}
	}


}
