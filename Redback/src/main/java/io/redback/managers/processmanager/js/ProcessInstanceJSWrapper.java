package io.redback.managers.processmanager.js;

import java.util.HashSet;
import java.util.Set;

import jdk.nashorn.api.scripting.AbstractJSObject;
import io.firebus.utils.FirebusDataUtil;
import io.redback.managers.processmanager.ProcessInstance;


public class ProcessInstanceJSWrapper extends AbstractJSObject
{
	protected ProcessInstance processInstance;
	
	public ProcessInstanceJSWrapper(ProcessInstance pi)
	{
		processInstance = pi;
	}
	
	public Object call(Object arg0, Object... args)
	{
		return null;
	}
	
	public String getClassName()
	{
		return "RedbackProcessInstance";
	}

	public Object getMember(String name)
	{
		if(name.equals("pid"))
		{
			return processInstance.getId();
		}
		else if(name.equals("data"))
		{
			return FirebusDataUtil.convertDataObjectToJSObject(processInstance.getData());
		}
		else
		{
			return null;
		}
	}

	public boolean hasMember(String name)
	{
		if(name.equals("pid")  ||  name.equals("data"))
			return true;
		else
			return false;
	}


	public Set<String> keySet()
	{
		HashSet<String> keys = new HashSet<String>();
		keys.add("pid");
		keys.add("data");
		return keys;
	}

	public void setMember(String name, Object value)
	{
	}	
}
