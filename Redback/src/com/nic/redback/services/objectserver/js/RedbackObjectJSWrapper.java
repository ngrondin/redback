package com.nic.redback.services.objectserver.js;

import java.util.Date;
import java.util.Set;

import jdk.nashorn.api.scripting.AbstractJSObject;
import jdk.nashorn.api.scripting.JSObject;
import jdk.nashorn.internal.objects.NativeDate;

import com.nic.redback.RedbackException;
import com.nic.redback.services.objectserver.RedbackObject;
import com.nic.redback.services.objectserver.Value;

public class RedbackObjectJSWrapper extends AbstractJSObject
{
	protected RedbackObject rbObject;
	
	public RedbackObjectJSWrapper(RedbackObject o)
	{
		rbObject = o;
	}

	public Object call(Object arg0, Object... args)
	{
		return null;
	}
	
	public JSObject getRelated(String name)
	{
		RedbackObject rbo = rbObject.getRelated(name);
		if(rbo != null)
			return new RedbackObjectJSWrapper(rbo);
		else
			return null;
		
	}

	public String getClassName()
	{
		return "RedbackObject";
	}

	public Object getMember(String name)
	{
		try
		{
			if(name.equals("getRelated")  ||  name.equals("save")  ||  rbObject.getObjectConfig().getScriptForEvent(name) != null)
			{
				return new RedbackObjectFunctionJSWrapper(rbObject, name);
			}
			else if(name.equals("objectname"))
			{
				return rbObject.getObjectConfig().getName();
			}
			else
			{
				Value val = rbObject.get(name);
				if(val != null)
					return rbObject.get(name).getObject();
				else
					return null;
			}
		} 
		catch (RedbackException e)
		{
			throw new RuntimeException("Error getting the Redback Object attribute '" + name + "'", e);
		}
	}

	public boolean hasMember(String arg0)
	{
		try
		{
			return rbObject.get(arg0) != null;
		} 
		catch (RedbackException e)
		{
			throw new RuntimeException("Error getting the Redback Object attribute '" + arg0 + "'", e);
		}
	}


	public Set<String> keySet()
	{
		return rbObject.getObjectConfig().getAttributeNames();
	}

	public void setMember(String arg0, Object arg1)
	{
		try
		{
			Object val = arg1;
			if(val instanceof NativeDate)
				val = new Date((long)NativeDate.getTime(val) + ((long)NativeDate.getTimezoneOffset(val) * 60000));
			rbObject.put(arg0, new Value(val));
		} 
		catch (RedbackException e)
		{
			throw new RuntimeException("Error setting the Redback Object attribute '" + arg0 + "' ", e);
		}
	}

}
