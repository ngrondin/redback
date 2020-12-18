package io.redback.utils.js;

import java.util.Arrays;
import java.util.Iterator;
import java.util.logging.Logger;

import org.graalvm.polyglot.Value;
import org.graalvm.polyglot.proxy.ProxyArray;
import org.graalvm.polyglot.proxy.ProxyExecutable;
import org.graalvm.polyglot.proxy.ProxyObject;

import io.firebus.Firebus;
import io.firebus.Payload;
import io.firebus.utils.DataException;
import io.firebus.utils.DataMap;
import io.redback.security.Session;

public class FirebusJSWrapper implements ProxyObject
{
	private Logger logger = Logger.getLogger("io.redback");
	protected Firebus firebus;
	protected Session session;
	protected String[] members = {"request"};
	
	public FirebusJSWrapper(Firebus fb, Session s)
	{
		firebus = fb;
		session = s;
	}

	public Object getMember(String key) {
		if(key.equals("request")) {
			return new ProxyExecutable() {
				public Object execute(Value... arguments) {
					String serviceName = arguments[0].asString();
					DataMap requestObject = (DataMap)JSConverter.toJava(arguments[1]);
					DataMap metaData = (DataMap)JSConverter.toJava(arguments.length >= 3 ? arguments[2] : null);
					int timeout = arguments.length >= 4 ? arguments[3].asInt() : 10000;
					if(serviceName != null)
					{
						try
						{
							Payload request = new Payload(requestObject.toString());
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
							logger.finest("Requesting firebus service : " + serviceName + "  " + request.toString().replace("\r\n", "").replace("\t", ""));
							Payload response = firebus.requestService(serviceName, request, timeout);
							logger.finest("Receiving firebus service respnse");
							try {
								DataMap responseObject = new DataMap(response.getString());
								return JSConverter.toJS(responseObject);
							} catch(DataException e ) {
								return response.getString();
							}
						}
						catch(Exception e)
						{
							logger.severe("Error processing firebus request :" + e);
						}
					}
					else
					{
						logger.severe("No service name provided in javascript firebus request");
					}
					return null;
				}
			};
		} else {
			return null;
		}

	}

	public Object getMemberKeys() {
		return ProxyArray.fromArray(((Object[])members));		
	}

	public boolean hasMember(String key) {
		return Arrays.asList(members).contains(key);
	}

	public void putMember(String key, Value value) {
				
	}

}
