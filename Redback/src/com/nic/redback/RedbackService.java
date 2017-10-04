package com.nic.redback;

import com.nic.firebus.Firebus;
import com.nic.firebus.interfaces.ServiceProvider;
import com.nic.firebus.utils.JSONObject;

public abstract class RedbackService implements  ServiceProvider
{
	protected Firebus firebus;
	protected JSONObject config;
	
	public RedbackService(JSONObject c)
	{
		config = c;
	}
	
	public void setFirebus(Firebus fb)
	{
		firebus = fb;
	}
	
	public static RedbackService instantiate(String type, JSONObject config)
	{
		if(type.equalsIgnoreCase("ObjectServer"))
		{
			return new ObjectServer(config);
		}
		return null;
	}


}
