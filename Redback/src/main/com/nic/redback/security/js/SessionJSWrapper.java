package com.nic.redback.security.js;

import java.util.HashSet;
import java.util.Set;

import jdk.nashorn.api.scripting.AbstractJSObject;

import com.nic.redback.security.Session;

public class SessionJSWrapper extends AbstractJSObject
{
	protected Session session;
	
	public SessionJSWrapper(Session s)
	{
		session = s;
	}


	public Object call(Object arg0, Object... args)
	{
		return null;
	}
	
	public String getClassName()
	{
		return "Session";
	}

	public Object getMember(String name)
	{
		if(name.equals("userProfile"))
		{
			return session.getUserProfile();
		}
		else
		{
			return null;
		}
	}

	public boolean hasMember(String arg0)
	{
		if(arg0.equals("userProfile"))
			return true;
		else
			return false;
	}


	public Set<String> keySet()
	{
		HashSet<String> set = new HashSet<String>();
		set.add("userProfile");
		return set;
	}

}
