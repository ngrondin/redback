package com.nic.redback.services.objectserver.js;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Set;

import jdk.nashorn.api.scripting.AbstractJSObject;
import jdk.nashorn.api.scripting.JSObject;

public class RedbackJSObject implements JSObject
{
	protected HashMap<String, Object> map;

	public RedbackJSObject()
	{
		map = new HashMap<String, Object>();
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
		return "Object";
	}

	public Object getMember(String arg0)
	{
		return map.get(arg0);
	}

	public Object getSlot(int arg0)
	{
		return null;
	}

	public boolean hasMember(String arg0)
	{
		return map.containsKey(arg0);
	}

	public boolean hasSlot(int arg0)
	{
		return false;
	}

	public boolean isArray()
	{
		return false;
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
		return map.keySet();
	}

	public Object newObject(Object... arg0)
	{
		return null;
	}

	public void removeMember(String arg0)
	{
		map.remove(arg0);
	}

	public void setMember(String arg0, Object arg1)
	{
		map.put(arg0, arg1);		
	}

	public void setSlot(int arg0, Object arg1)
	{
	}

	@Deprecated
	public double toNumber()
	{
		return 0;
	}

	public Collection<Object> values()
	{
		return null;
	}

}
