package io.redback.services.common;

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
	
	protected String getLogline(Payload payload) {
		String mime = payload.metadata.get("mime");
		String body = null;
		if(mime != null && mime.equals("application/json")) {
			body = payload.getString().replaceAll("\r", "").replaceAll("\n", "").replaceAll("\t", "");
			return body;
		} else if(mime != null && mime.equals("text/plain")) {
			body = payload.getString();
		} else {
			body = "";
		}
		return body;
	}
	
	protected FunctionErrorException handleException(Exception e, String msg) {
		int errorCode = 0;
		if(e instanceof RedbackException) {
			RedbackException rbe = (RedbackException)e;
			errorCode = rbe.getErrorCode();
		} else {
			
		}
		if(errorCode == 0 || errorCode >= 500)
			logger.severe(StringUtils.getStackTrace(e));
		else 
			logger.warning("Invalid request: " + StringUtils.rollUpExceptions(e));
		return new FunctionErrorException(msg, e, errorCode);
	}


	public abstract void configure();
	
	public abstract void start();

}
