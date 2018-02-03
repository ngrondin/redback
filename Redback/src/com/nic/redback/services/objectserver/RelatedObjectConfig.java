package com.nic.redback.services.objectserver;

import com.nic.firebus.utils.JSONObject;
import com.nic.redback.RedbackException;

public class RelatedObjectConfig
{
	protected JSONObject config;
	protected FilterConfig listFilter;
	
	public RelatedObjectConfig(JSONObject cfg) throws RedbackException
	{
		config = cfg;
		if(config.get("listfilter") != null)
			listFilter = new FilterConfig(config.getObject("listfilter"));
	}
	
	public String getObjectName()
	{
		return config.getString("name");
	}
	
	public String getLinkAttributeName()
	{
		return config.getString("linkattribute");
	}

	public FilterConfig getListFilterConfig()
	{
		return listFilter;
	}
	
	public JSONObject getJSON()
	{
		return config;
	}

}
