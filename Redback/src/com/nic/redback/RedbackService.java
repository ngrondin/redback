package com.nic.redback;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.logging.Logger;

import com.nic.firebus.Firebus;
import com.nic.firebus.Payload;
import com.nic.firebus.exceptions.FunctionErrorException;
import com.nic.firebus.exceptions.FunctionTimeoutException;
import com.nic.firebus.interfaces.ServiceProvider;
import com.nic.firebus.utils.JSONException;
import com.nic.firebus.utils.JSONObject;
import com.nic.redback.services.AccessManager;
import com.nic.redback.services.ConfigurationServer;
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
	
	protected JSONObject request(String service, JSONObject request) throws JSONException, FunctionErrorException, FunctionTimeoutException, RedbackException
	{
		return request(service, request.toString());
	}
	
	protected JSONObject request(String service, String request) throws JSONException, FunctionErrorException, FunctionTimeoutException, RedbackException
	{
		if(service != null)
		{
			Payload reqPayload = new Payload(request);
			logger.info("Requesting firebus config service : " + "  " + request.replace("\r\n", "").replace("\t", ""));
			Payload respPayload = firebus.requestService(service, reqPayload, 10000);
			logger.info("Received firebus service response from : " + service);
			String respStr = respPayload.getString();
			JSONObject result = new JSONObject(respStr);
			return result;
		}
		else
		{
			error("Service Name or Request is null in firebus request");
			return null;
		}
	}

	protected void error(String msg) throws RedbackException
	{
		error(msg, null);
	}
	
	protected void error(String msg, Exception cause) throws RedbackException
	{
		logger.severe(msg);
		if(cause != null)
			throw new RedbackException(msg, cause);
		else
			throw new RedbackException(msg);
	}

	protected String buildErrorMessage(Exception e)
	{
		String msg = "";
		Throwable t = e;
		while(t != null)
		{
			if(msg.length() > 0)
				msg += " : ";
			msg += t.getMessage();
			t = t.getCause();
		}
		return msg;
	}
	
	protected String getStackTrace(Exception e)
	{
		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);
		e.printStackTrace(pw);
		String sStackTrace = sw.toString(); 
		return sStackTrace;
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
		else if(type.equalsIgnoreCase("configurationserver"))
		{
			return new ConfigurationServer(config);
		}
		return null;
	}


}
