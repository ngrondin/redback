package com.nic.redback;

import com.nic.firebus.utils.JSONObject;

public class RelatedObjectConfig
{
	protected JSONObject config;
	
	public RelatedObjectConfig(JSONObject cfg)
	{
		config = cfg;
	}
	
	public String getObjectName()
	{
		return config.getString("name");
	}
	
	public String getLinkAttributeName()
	{
		return config.getString("linkattribute");
	}

	public JSONObject getRelationship()
	{
		return config.getObject("relationship");
	}
	
	public JSONObject getJSON()
	{
		return config;
	}

}