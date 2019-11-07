package com.nic.redback.managers.objectmanagers.js;


import com.nic.redback.managers.objectmanager.RedbackObject;

import jdk.nashorn.api.scripting.AbstractJSObject;

public class RedbackObjectFunctionJSWrapper extends AbstractJSObject
{
	protected RedbackObject rbObject;
	protected String functionName;
	
	public RedbackObjectFunctionJSWrapper(RedbackObject o, String fn)
	{
		rbObject = o;
		functionName = fn;
	}

	public Object call(Object arg0, Object... args)
	{
		Object retVal = null;
		try
		{
			if(functionName.equals("getRelated"))
			{
				RedbackObject rbo = rbObject.getRelated((String)args[0]);
				if(rbo != null)
					retVal = new RedbackObjectJSWrapper(rbo);
			}
			else if(functionName.equals("save"))
			{
				rbObject.save();
			}
			else
			{
				rbObject.execute(functionName);
			}
		} 
		catch (Exception e)
		{
			throw new RuntimeException(e);
		} 
		return retVal;
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
