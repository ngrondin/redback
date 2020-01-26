package io.redback.managers.objectmanagers.js;

import java.util.Date;
import java.util.Set;

import jdk.nashorn.api.scripting.AbstractJSObject;
import jdk.nashorn.api.scripting.JSObject;
import jdk.nashorn.internal.objects.NativeDate;
import jdk.nashorn.api.scripting.ScriptUtils;
import io.firebus.utils.DataMap;
import io.firebus.utils.FirebusDataUtil;
import io.redback.RedbackException;
import io.redback.managers.objectmanager.RedbackObject;
import io.redback.managers.objectmanager.Value;

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
	
	public String getClassName()
	{
		return "RedbackObject";
	}

	public Object getMember(String name)
	{
		try
		{
			if(name.equals("getRelated")  ||  name.equals("save")  ||  name.equals("getUpdatedAttributes")  ||  rbObject.getObjectConfig().getScriptForEvent(name) != null)
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
				Object obj = null;
				if(val != null)
					obj = val.getObject();

				if(obj instanceof DataMap)
					return FirebusDataUtil.convertDataObjectToJSObject((DataMap)obj);
				else
					return obj;
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
			Object in = ScriptUtils.unwrap(arg1);
			Object val = null;
			if(in instanceof CharSequence)
			{
				val = ((CharSequence)in).toString();
			}
			else if(in instanceof NativeDate)
			{
				val = new Date((long)NativeDate.getTime(in) + ((long)NativeDate.getTimezoneOffset(in) * 60000));
			}
			else if(in instanceof JSObject)
			{
				JSObject jso = (JSObject)in;
				if(jso.isArray())
					val = FirebusDataUtil.convertJSArrayToDataList(jso);
				else
					val = FirebusDataUtil.convertJSObjectToDataObject(jso);
			}
			else
			{
				val = in;
			}
			rbObject.put(arg0, new Value(val));
		} 
		catch (RedbackException e)
		{
			throw new RuntimeException("Error setting the Redback Object attribute '" + arg0 + "' ", e);
		}
	}

}
