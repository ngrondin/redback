package io.redback.utils;


import java.util.logging.Logger;

import io.redback.security.Session;
import jdk.nashorn.api.scripting.AbstractJSObject;

public class LoggerJSFunction extends AbstractJSObject
{
	private Logger logger = Logger.getLogger("io.redback");
	protected Session session;
	protected String right;
	
	public LoggerJSFunction()
	{
	}

	public Object call(Object arg0, Object... args)
	{
		if(args[0].equals("info"))
			logger.fine((String)args[1]);
		return null;
	}

	public boolean isFunction()
	{
		return true;
	}

	public boolean isStrictFunction()
	{
		return true;
	}
}
