package com.nic.redback.services.objectserver.js;


import jdk.nashorn.api.scripting.AbstractJSObject;

import com.nic.redback.services.objectserver.RedbackObject;

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
			e.printStackTrace();
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
