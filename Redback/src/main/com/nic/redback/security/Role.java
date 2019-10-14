package com.nic.redback.security;

import com.nic.firebus.utils.JSONObject;

public class Role
{
	protected JSONObject config;
	
	public Role(JSONObject c)
	{
		config = c;
	}
	
	public String getName()
	{
		return config.getString("name");
	}
	
	/*
	public Set<String> getRightNameSet()
	{
		return config.getObject("rights").keySet();
	}
	*/
	
	public JSONObject getAllRights()
	{
		return config.getObject("rights");
	}

	public String getRights(String name)
	{
		return config.getString("rights." + name);
	}
	
}
