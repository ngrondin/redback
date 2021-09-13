package io.redback.security.js;



import io.redback.exceptions.RedbackException;
import io.redback.security.Session;
import io.redback.utils.js.CallableJSWrapper;

public class SessionRightsJSFunction extends CallableJSWrapper
{
	protected Session session;
	protected String right;
	
	public SessionRightsJSFunction(Session s, String r)
	{
		session = s;
		right = r;
	}

	public Object call(Object... arguments) throws RedbackException {
		boolean val = false;
		String name = (String)arguments[0];
		if(right.equals("read"))
			val = session.getUserProfile().canRead(name);
		else if(right.equals("write"))
			val = session.getUserProfile().canWrite(name);
		else if(right.equals("execute"))
			val = session.getUserProfile().canExecute(name);
		return val;
	}
}
