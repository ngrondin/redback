package io.redback.services.common;

import java.util.logging.Level;
import java.util.logging.Logger;

//import java.util.logging.Logger;

import io.firebus.Firebus;
import io.firebus.Payload;
import io.firebus.exceptions.FunctionErrorException;
import io.firebus.data.DataMap;
import io.redback.exceptions.RedbackException;
import io.redback.utils.StringUtils;

public abstract class Provider 
{
	private Logger logger = Logger.getLogger("io.redback");
	protected String serviceName;
	protected Firebus firebus;
	protected DataMap config;
	
	public Provider(String n, DataMap c, Firebus f)
	{
		serviceName = n;
		config = c;
		firebus = f;
	}
	
	protected void logExecution(Payload payload, long duration) {
		DataMap params = new DataMap();
		params.put("dur", duration);
		params.put("req", payload.getDataObject());
		logger.log(Level.INFO, "exec", params);
	}
	
	protected FunctionErrorException handleException(Exception e, String msg) {
		int errorCode = 0;
		if(e instanceof RedbackException) {
			RedbackException rbe = (RedbackException)e;
			errorCode = rbe.getErrorCode();
		} else {
			
		}
		if(errorCode == 0 || errorCode >= 500)
			logger.log(Level.SEVERE, msg, e);
		else 
			logger.log(Level.WARNING, StringUtils.rollUpExceptions(e));
		return new FunctionErrorException(msg, e, errorCode);
	}


	public void configure() {
		
	}
	
	public void start() {
		
	}
	
	public DataMap getStatus() {
		return null;
	}

}
