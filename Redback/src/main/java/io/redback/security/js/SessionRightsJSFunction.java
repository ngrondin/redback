package io.redback.security.js;


import org.graalvm.polyglot.Value;
import org.graalvm.polyglot.proxy.ProxyExecutable;

import io.redback.security.Session;

public class SessionRightsJSFunction implements ProxyExecutable
{
	protected Session session;
	protected String right;
	
	public SessionRightsJSFunction(Session s, String r)
	{
		session = s;
		right = r;
	}

	public Object execute(Value... arguments) {
		boolean val = false;
		String name = arguments[0].asString();
		if(right.equals("read"))
			val = session.getUserProfile().canRead(name);
		else if(right.equals("write"))
			val = session.getUserProfile().canWrite(name);
		else if(right.equals("execute"))
			val = session.getUserProfile().canExecute(name);
		return val;
	}
}
