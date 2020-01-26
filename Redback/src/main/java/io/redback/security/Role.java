package io.redback.security;

import io.firebus.utils.DataMap;

public class Role
{
	protected DataMap config;
	
	public Role(DataMap c)
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
	
	public DataMap getAllRights()
	{
		return config.getObject("rights");
	}

	public String getRights(String name)
	{
		return config.getString("rights." + name);
	}
	
}
