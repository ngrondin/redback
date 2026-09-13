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
		String level = arguments.length == 1 ? "info" : arguments[0].toString().toLowerCase();
		Object val = arguments.length == 1 ?  arguments[0] : arguments[1];
		Exception err = arguments.length == 3 ? (Exception)arguments[2] : null;
		String msg = val != null ? val.toString() : "null";
		if(level.equals("error") || level.equals("severe"))
			Logger.severe("rb.js", msg, null, err);
		if(level.equals("security"))
			Logger.security("rb.js", msg, err);
		if(level.equals("warning"))
			Logger.warning("rb.js", msg);
		if(level.equals("usererror"))
			Logger.userError("rb.js", msg);
		if(level.equals("info"))
			Logger.info("rb.js", msg);
		if(level.equals("fine"))
			Logger.fine("rb.js", msg);
		if(level.equals("finder"))
			Logger.finer("rb.js", msg);
			
		return null;
	}
}
