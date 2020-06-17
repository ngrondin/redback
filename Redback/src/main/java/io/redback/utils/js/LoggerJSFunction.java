package io.redback.utils.js;


import java.util.logging.Logger;

import org.graalvm.polyglot.Value;
import org.graalvm.polyglot.proxy.ProxyExecutable;

import io.redback.security.Session;


public class LoggerJSFunction implements ProxyExecutable
{
	private Logger logger = Logger.getLogger("io.redback");
	protected Session session;
	protected String right;
	
	public LoggerJSFunction()
	{
	}


	@Override
	public Object execute(Value... arguments) {
		String level = arguments[0].asString();
		String msg = arguments[1].asString();
		if(level.equals("info"))
			logger.fine((String)msg);
		return null;
	}
}
