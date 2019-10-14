package com.nic.redback.utils;

import java.util.logging.Logger;

import com.nic.firebus.Firebus;
import com.nic.firebus.Payload;
import com.nic.firebus.utils.FirebusDataUtil;
import com.nic.firebus.utils.JSONObject;
import com.nic.redback.RedbackException;

import jdk.nashorn.api.scripting.JSObject;

public class FirebusJSWrapper 
{
	private Logger logger = Logger.getLogger("com.nic.redback");
	protected Firebus firebus;
	protected String sessionId;
	
	public FirebusJSWrapper(Firebus fb, String s)
	{
		firebus = fb;
		sessionId = s;
	}
	
	public JSObject request(String serviceName, JSObject jsRequest) throws RedbackException 
	{
		if(serviceName != null)
		{
			try
			{
				JSONObject requestObject = FirebusDataUtil.convertJSObjectToDataObject(jsRequest);
				Payload request = new Payload(requestObject.toString());
				request.metadata.put("sessionid", sessionId);
				logger.info("Requesting firebus service : " + serviceName + "  " + request.toString().replace("\r\n", "").replace("\t", ""));
				Payload response = firebus.requestService(serviceName, request);
				logger.info("Receiving firebus service respnse");
				JSONObject responseObject = new JSONObject(response.getString());
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
