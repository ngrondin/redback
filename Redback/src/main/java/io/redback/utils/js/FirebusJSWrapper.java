package io.redback.utils.js;

import java.util.Iterator;


import io.firebus.Firebus;
import io.firebus.Payload;
import io.firebus.data.DataException;
import io.firebus.data.DataMap;
import io.redback.exceptions.RedbackException;
import io.redback.security.Session;

public class FirebusJSWrapper extends ObjectJSWrapper
{
	protected Firebus firebus;
	protected Session session;
	
	public FirebusJSWrapper(Firebus fb, Session s)
	{
		super(new String[] {"request"});
		firebus = fb;
		session = s;
	}

	public Object get(String key) {
		if(key.equals("request")) {
			return new CallableJSWrapper() {
				public Object call(Object... arguments) throws RedbackException {
					String serviceName = (String)arguments[0];
					DataMap requestObject = (DataMap)(arguments[1]);
					DataMap metaData = (DataMap)(arguments.length >= 3 ? arguments[2] : null);
					int timeout = arguments.length >= 4 ? (Integer)arguments[3] : 10000;
					if(serviceName != null)
					{
						try
						{
							Payload request = new Payload(requestObject);
							request.metadata.put("token", session.getToken());
							request.metadata.put("session", session.getId());
							if(metaData != null) 
							{
								Iterator<String> it = metaData.keySet().iterator();
								while(it.hasNext()) {
									String key = it.next();
									request.metadata.put(key, metaData.getString(key));
								}
							}
							Payload response = firebus.requestService(serviceName, request, timeout);
							try {
								DataMap responseObject = response.getDataMap();
								return responseObject;
							} catch(DataException e ) {
								return response.getString();
							}
						}
						catch(Exception e)
						{
							throw new RedbackException("Error processing firebus request", e);
						}
					}
					else
					{
						throw new RedbackException("No service name provided in javascript firebus request");
					}
				}
			};
		} else if(key.equals("publish")) {
			return new CallableJSWrapper() {
				public Object call(Object... arguments) throws RedbackException {
					String name = (String)arguments[0];
					DataMap requestObject = (DataMap)(arguments[1]);
					DataMap metaData = (DataMap)(arguments.length >= 3 ? arguments[2] : null);
					if(name != null)
					{
						try
						{
							Payload request = new Payload(requestObject);
							request.metadata.put("token", session.getToken());
							request.metadata.put("session", session.getId());
							if(metaData != null) 
							{
								Iterator<String> it = metaData.keySet().iterator();
								while(it.hasNext()) {
									String key = it.next();
									request.metadata.put(key, metaData.getString(key));
								}
							}
							firebus.publish(name, request);
						}
						catch(Exception e)
						{
							throw new RedbackException("Error processing firebus publish", e);
						}
					}
					else
					{
						throw new RedbackException("No service name provided in javascript firebus publish");
					}
					return null;
				}
			};
		} else {
			return null;
		}

	}

}
