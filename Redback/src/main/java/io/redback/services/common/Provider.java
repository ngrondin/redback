package io.redback.services.common;

import io.firebus.Firebus;
import io.firebus.data.DataMap;
import io.firebus.exceptions.FunctionErrorException;
import io.firebus.logging.Logger;
import io.redback.exceptions.RedbackException;
import io.redback.utils.StringUtils;

public abstract class Provider 
{
	protected String serviceName;
	protected Firebus firebus;
	protected DataMap config;
	
	public Provider(String n, DataMap c, Firebus f)
	{
		serviceName = n;
		config = c;
		firebus = f;
	}

	
	protected FunctionErrorException handleException(String event, String msg, Exception e) {
		int errorCode = 0;
		if(e instanceof RedbackException) {
			RedbackException rbe = (RedbackException)e;
			errorCode = rbe.getErrorCode();
		} else {
			
		}
		if(errorCode == 0 || errorCode >= 500)
			Logger.severe(event, msg, e);
		else 
			Logger.severe(event, StringUtils.rollUpExceptions(e) + ": " + msg, e);
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
