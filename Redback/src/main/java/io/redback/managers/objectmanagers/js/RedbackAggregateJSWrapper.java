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
import io.redback.managers.objectmanager.RedbackAggregate;
import io.redback.managers.objectmanager.RedbackElement;
import io.redback.managers.objectmanager.RedbackObject;
import io.redback.managers.objectmanager.Value;

public class RedbackAggregateJSWrapper extends AbstractJSObject
{
	protected RedbackAggregate rbAggregate;
	
	public RedbackAggregateJSWrapper(RedbackAggregate o)
	{
		rbAggregate = o;
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
			if(name.equals("objectname"))
			{
				return rbAggregate.getObjectConfig().getName();
			}
			else
			{
				Object obj = null;
				Value val = rbAggregate.get(name);
				if(val != null)
				{
					obj = val.getObject();
				}
				else
				{
					val = rbAggregate.getMetric(name);
					if(val != null)
						obj = val.getObject();
				}
				
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

	public boolean hasMember(String name)
	{
		try
		{
			return rbAggregate.get(name) != null || rbAggregate.getMetric(name) != null;
		} 
		catch (RedbackException e)
		{
			throw new RuntimeException("Error getting the Redback Object attribute '" + name + "'", e);
		}
	}


	public Set<String> keySet()
	{
		return rbAggregate.getObjectConfig().getAttributeNames();
	}

	public void setMember(String arg0, Object arg1)
	{

	}

}
