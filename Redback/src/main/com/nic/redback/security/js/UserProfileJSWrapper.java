package com.nic.redback.security.js;

import java.util.HashSet;
import java.util.Set;

import jdk.nashorn.api.scripting.AbstractJSObject;

import com.nic.redback.security.UserProfile;

public class UserProfileJSWrapper extends AbstractJSObject
{
	protected UserProfile userProfile;
	protected String functionName;
	
	public UserProfileJSWrapper(UserProfile up)
	{
		userProfile = up;
	}

	public UserProfileJSWrapper(UserProfile up, String fn)
	{
		userProfile = up;
		functionName = fn;
	}

	public String getClassName()
	{
		return "Session";
	}

	public Object getMember(String name)
	{
		if(name.equals("username"))
		{
			return userProfile.getUsername();
		}
		else if(name.equals("getAttribute") || name.equals("getRights") || name.equals("canRead")  ||  name.equals("canWrite")  ||  name.equals("canExecute"))
		{
			return new UserProfileJSWrapper(userProfile, name);
		}
		else
		{
			return null;
		}
	}
	
	public Object call(Object jsObj, Object... args)
	{
		String param = (String)args[0];
		Object val = null;
		if(functionName.equals("getAttribute"))
			val = userProfile.getAttribute(param);
		else if(functionName.equals("getRights"))
			val = userProfile.getRights(param);
		else if(functionName.equals("canRead"))
			val = userProfile.canRead(param);
		else if(functionName.equals("canWrite"))
			val = userProfile.canWrite(param);
		else if(functionName.equals("canExecute"))
			val = userProfile.canExecute(param);
		return val;
	}

	public boolean hasMember(String name)
	{
		if(name.equals("username") || name.equals("getAttributes") || name.equals("getRights") || name.equals("canRead")  ||  name.equals("canWrite")  ||  name.equals("canExecute"))
			return true;
		else
			return false;
	}


	public Set<String> keySet()
	{
		HashSet<String> set = new HashSet<String>();
		set.add("username");
		set.add("getAttributes");
		set.add("getRights");
		set.add("canRead");
		set.add("canWrite");
		set.add("canExecute");
		return set;
	}

}
