package com.nic.redback.services;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.logging.Logger;

import com.nic.firebus.Firebus;
import com.nic.firebus.Payload;
import com.nic.firebus.exceptions.FunctionErrorException;
import com.nic.firebus.exceptions.FunctionTimeoutException;
import com.nic.firebus.interfaces.ServiceProvider;
import com.nic.firebus.utils.DataException;
import com.nic.firebus.utils.DataMap;
import com.nic.redback.RedbackException;

public abstract class Service implements ServiceProvider
{
	private Logger logger = Logger.getLogger("com.nic.redback");
	protected String serviceName;
	protected Firebus firebus;
	protected DataMap config;
	
	public Service(DataMap c, Firebus f)
	{
		firebus = f;
		config = c;
	}

	protected DataMap request(String service, DataMap request) throws DataException, FunctionErrorException, FunctionTimeoutException, RedbackException
	{
		return request(service, request.toString());
	}
	
	protected DataMap request(String service, String request) throws DataException, FunctionErrorException, FunctionTimeoutException, RedbackException
	{
		if(service != null)
		{
			Payload reqPayload = new Payload(request);
			logger.finest("Requesting firebus service : " + "  " + request.replace("\r\n", "").replace("\t", ""));
			Payload respPayload = firebus.requestService(service, reqPayload, 10000);
			logger.finest("Received firebus service response from : " + service);
			String respStr = respPayload.getString();
			DataMap result = new DataMap(respStr);
			return result;
		}
		else
		{
			error("Service Name or Request is null in firebus request");
			return null;
		}
	}
	
	protected Object error(String msg) throws RedbackException
	{
		return error(msg, null);
	}
	
	protected Object error(String msg, Exception cause) throws RedbackException
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

}
