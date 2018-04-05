package com.nic.redback.utils;


import jdk.nashorn.api.scripting.AbstractJSObject;

import com.nic.redback.security.Session;

public class SessionRightsFunctionJSWrapper extends AbstractJSObject
{
	protected Session session;
	protected String right;
	
	public SessionRightsFunctionJSWrapper(Session s, String r)
	{
		session = s;
		right = r;
	}

	public Object call(Object arg0, Object... args)
	{
		boolean val = false;
		String name = (String)arg0;
		if(right.equals("read"))
			val = session.getUserProfile().canRead(name);
		else if(right.equals("write"))
			val = session.getUserProfile().canWrite(name);
		else if(right.equals("execute"))
			val = session.getUserProfile().canExecute(name);
		return val;
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
