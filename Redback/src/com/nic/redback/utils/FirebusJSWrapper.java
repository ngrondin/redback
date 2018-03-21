package com.nic.redback.utils;

import com.nic.firebus.Firebus;
import com.nic.firebus.Payload;
import com.nic.firebus.utils.FirebusDataUtil;
import com.nic.firebus.utils.JSONObject;
import com.nic.redback.RedbackException;

import jdk.nashorn.api.scripting.JSObject;

public class FirebusJSWrapper 
{
	protected Firebus firebus;
	protected String sessionId;
	
	public FirebusJSWrapper(Firebus fb, String s)
	{
		firebus = fb;
		sessionId = s;
	}
	
	public JSObject request(String serviceName, JSObject jsRequest) throws RedbackException 
	{
		try
		{
			JSONObject requestObject = FirebusDataUtil.convertJSObjectToDataObject(jsRequest);
			Payload request = new Payload(requestObject.toString());
			request.metadata.put("sessionid", sessionId);
			Payload response = firebus.requestService(serviceName, request);
			JSONObject responseObject = new JSONObject(response.getString());
			JSObject jsResponse = FirebusDataUtil.convertDataObjectToJSObject(responseObject);
			return jsResponse;
		}
		catch(Exception e)
		{
			throw new RedbackException("Error processing firebus request", e);
		}
	}

}
