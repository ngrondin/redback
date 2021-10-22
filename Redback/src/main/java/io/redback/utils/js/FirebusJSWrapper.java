package io.redback.utils.js;

import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;


import io.firebus.Firebus;
import io.firebus.Payload;
import io.firebus.data.DataException;
import io.firebus.data.DataMap;
import io.redback.exceptions.RedbackException;
import io.redback.security.Session;

public class FirebusJSWrapper extends ObjectJSWrapper
{
	private Logger logger = Logger.getLogger("io.redback");
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
							if(logger.getLevel() == Level.FINEST) logger.finest("Requesting firebus service : " + serviceName + "  " + request.toString().replace("\r\n", "").replace("\t", ""));
							Payload response = firebus.requestService(serviceName, request, timeout);
							logger.finest("Receiving firebus service respnse");
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
		} else {
			return null;
		}

	}

}
