package io.redback.services;

//import java.util.logging.Logger;

import io.firebus.Firebus;
import io.firebus.utils.DataMap;

public abstract class Service 
{
	//private Logger logger = Logger.getLogger("io.redback");
	protected String serviceName;
	protected Firebus firebus;
	protected DataMap config;
	
	public Service(String n, DataMap c, Firebus f)
	{
		serviceName = n;
		config = c;
		firebus = f;
	}
/*
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
			throw new FunctionErrorException("Service Name or Request is null in firebus request");
		}
	}
	*/
	/*
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
	*/
	public abstract void clearCaches();

}
