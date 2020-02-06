package io.redback.utils;

import java.util.Iterator;
import java.util.logging.Logger;

import io.firebus.Firebus;
import io.firebus.Payload;
import io.firebus.utils.DataMap;
import io.firebus.utils.FirebusDataUtil;
import io.redback.RedbackException;
import io.redback.security.Session;
import jdk.nashorn.api.scripting.JSObject;

public class FirebusJSWrapper 
{
	private Logger logger = Logger.getLogger("io.redback");
	protected Firebus firebus;
	protected Session session;
	
	public FirebusJSWrapper(Firebus fb, Session s)
	{
		firebus = fb;
		session = s;
	}
	
	public JSObject request(String serviceName, JSObject jsRequest) throws RedbackException 
	{
		return request(serviceName, null, jsRequest);
	}
	
	public JSObject request(String serviceName, JSObject metadata, JSObject jsRequest) throws RedbackException 
	{
		if(serviceName != null)
		{
			try
			{
				DataMap requestObject = FirebusDataUtil.convertJSObjectToDataObject(jsRequest);
				Payload request = new Payload(requestObject.toString());
				request.metadata.put("token", session.getToken());
				if(metadata != null) 
				{
					Iterator<String> it = metadata.keySet().iterator();
					while(it.hasNext()) {
						String key = it.next();
						request.metadata.put(key, metadata.getMember(key).toString());
					}
				}
				logger.finest("Requesting firebus service : " + serviceName + "  " + request.toString().replace("\r\n", "").replace("\t", ""));
				Payload response = firebus.requestService(serviceName, request);
				logger.finest("Receiving firebus service respnse");
				DataMap responseObject = new DataMap(response.getString());
				JSObject jsResponse = FirebusDataUtil.convertDataObjectToJSObject(responseObject);
				return jsResponse;
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

}
