package io.redback.utils.js;


import io.firebus.logging.Logger;
import io.redback.exceptions.RedbackException;
import io.redback.security.Session;


public class LoggerJSFunction extends CallableJSWrapper
{
	protected Session session;
	protected String right;
	
	public LoggerJSFunction()
	{
	}


	public Object call(Object... arguments) throws RedbackException {
		String level = (String)arguments[0];
		String msg = arguments[1] instanceof String ? (String)arguments[1] : arguments[1].toString();
		if(level.equals("info"))
			Logger.info("rb.js", msg);
		if(level.equals("fine"))
			Logger.fine("rb.js", msg);
		if(level.equals("finder"))
			Logger.finer("rb.js", msg);
		return null;
	}
}
