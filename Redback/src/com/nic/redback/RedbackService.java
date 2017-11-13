package com.nic.redback;

import com.nic.firebus.Firebus;
import com.nic.firebus.Payload;
import com.nic.firebus.exceptions.FunctionErrorException;
import com.nic.firebus.exceptions.FunctionTimeoutException;
import com.nic.firebus.interfaces.ServiceProvider;
import com.nic.firebus.utils.JSONException;
import com.nic.firebus.utils.JSONObject;

public abstract class RedbackService implements  ServiceProvider
{
	protected Firebus firebus;
	protected JSONObject config;
	protected String configService;
	
	public RedbackService(JSONObject c)
	{
		config = c;
		configService = config.getString("configservice");
	}
	
	public void setFirebus(Firebus fb)
	{
		firebus = fb;
	}
	
	protected JSONObject request(String service, String request) throws JSONException, FunctionErrorException, FunctionTimeoutException
	{
		Payload reqPayload = new Payload(request);
		Payload respPayload = firebus.requestService(configService, reqPayload);
		String respStr = respPayload.getString();
		JSONObject result = new JSONObject(respStr);
		return result;
	}


	
	public static RedbackService instantiate(String type, JSONObject config)
	{
		if(type.equalsIgnoreCase("objectserver"))
		{
			return new ObjectServer(config);
		}
		else if(type.equalsIgnoreCase("uiserver"))
		{
			return new UIServer(config);
		}
		else if(type.equalsIgnoreCase("idgenerator"))
		{
			return new IDGenerator(config);
		}
		return null;
	}


}
