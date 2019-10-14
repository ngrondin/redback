package com.nic.redback.utils;


import java.util.logging.Logger;

import jdk.nashorn.api.scripting.AbstractJSObject;

import com.nic.redback.security.Session;

public class LoggerJSFunction extends AbstractJSObject
{
	private Logger logger = Logger.getLogger("com.nic.redback");
	protected Session session;
	protected String right;
	
	public LoggerJSFunction()
	{
	}

	public Object call(Object arg0, Object... args)
	{
		if(args[0].equals("info"))
			logger.info((String)args[1]);
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
