package io.redback.utils.js;


import java.util.logging.Logger;

import io.redback.exceptions.RedbackException;
import io.redback.security.Session;


public class LoggerJSFunction extends CallableJSWrapper
{
	private Logger logger = Logger.getLogger("io.redback");
	protected Session session;
	protected String right;
	
	public LoggerJSFunction()
	{
	}


	public Object call(Object... arguments) throws RedbackException {
		String level = (String)arguments[0];
		String msg = (String)arguments[1];
		if(level.equals("info"))
			logger.info((String)msg);
		if(level.equals("fine"))
			logger.fine((String)msg);
		if(level.equals("finder"))
			logger.finer((String)msg);
		return null;
	}
}
