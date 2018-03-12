package com.nic.redback.services.objectserver.js;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Set;

import jdk.nashorn.api.scripting.AbstractJSObject;
import jdk.nashorn.api.scripting.JSObject;

public class RedbackJSArray implements JSObject
{
	protected ArrayList<Object> list;
	
	public RedbackJSArray()
	{
		list = new ArrayList<Object>();
	}
	
	public Object call(Object arg0, Object... arg1)
	{
		// TODO Auto-generated method stub
		return null;
	}

	public Object eval(String arg0)
	{
		// TODO Auto-generated method stub
		return null;
	}

	public String getClassName()
	{
		return "Array";
	}

	public Object getMember(String arg0)
	{
		if(arg0.equals("length"))
			return list.size();
		else
			return null;
	}

	public Object getSlot(int arg0)
	{
		return list.get(arg0);
	}

	public boolean hasMember(String arg0)
	{
		return false;
	}

	public boolean hasSlot(int arg0)
	{
		return list.size() > arg0;
	}

	public boolean isArray()
	{
		return true;
	}

	public boolean isFunction()
	{
		return false;
	}

	public boolean isInstance(Object arg0)
	{
		return false;
	}

	public boolean isInstanceOf(Object arg0)
	{
		return false;
	}

	public boolean isStrictFunction()
	{
		return false;
	}

	public Set<String> keySet()
	{
		return null;
	}

	public Object newObject(Object... arg0)
	{
		return null;
	}

	public void removeMember(String arg0)
	{
	}

	public void setMember(String arg0, Object arg1)
	{
	}

	public void setSlot(int arg0, Object arg1)
	{
		list.add(arg0, arg1);
	}

	@Deprecated
	public double toNumber()
	{
		return 0;
	}

	public Collection<Object> values()
	{
		return list;
	}

}
