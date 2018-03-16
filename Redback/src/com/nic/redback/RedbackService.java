package com.nic.redback;

import java.util.logging.Logger;

import com.nic.firebus.Firebus;
import com.nic.firebus.Payload;
import com.nic.firebus.exceptions.FunctionErrorException;
import com.nic.firebus.exceptions.FunctionTimeoutException;
import com.nic.firebus.interfaces.ServiceProvider;
import com.nic.firebus.utils.JSONException;
import com.nic.firebus.utils.JSONObject;
import com.nic.redback.services.AccessManager;
import com.nic.redback.services.IDGenerator;
import com.nic.redback.services.ObjectServer;
import com.nic.redback.services.ProcessServer;
import com.nic.redback.services.UIServer;

public abstract class RedbackService implements  ServiceProvider
{
	private Logger logger = Logger.getLogger("com.nic.redback");
	protected String serviceName;
	protected Firebus firebus;
	protected JSONObject config;
	protected String configService;
	
	public RedbackService(JSONObject c)
	{
		config = c;
		configService = config.getString("configservice");
	}

	public void setName(String n)
	{
		serviceName = n;
	}
	
	public void setFirebus(Firebus fb)
	{
		firebus = fb;
	}

	
	protected JSONObject request(String service, String request) throws JSONException, FunctionErrorException, FunctionTimeoutException, RedbackException
	{
		if(service != null)
		{
			Payload reqPayload = new Payload(request);
			logger.info("Requesting firebus service : " + service);
			Payload respPayload = firebus.requestService(service, reqPayload);
			logger.info("Received firebus service response from : " + service);
			String respStr = respPayload.getString();
			JSONObject result = new JSONObject(respStr);
			return result;
		}
		else
		{
			throw new RedbackException("Missing sub-service configuration in service '" + serviceName + "' for request: " + request);
		}
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
		else if(type.equalsIgnoreCase("processserver"))
		{
			return new ProcessServer(config);
		}
		else if(type.equalsIgnoreCase("idgenerator"))
		{
			return new IDGenerator(config);
		}
		else if(type.equalsIgnoreCase("accessmanager"))
		{
			return new AccessManager(config);
		}
		return null;
	}


}
