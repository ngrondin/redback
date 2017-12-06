package com.nic.redback;

import com.nic.firebus.utils.JSONObject;
import com.nic.redback.utils.StringUtils;

public class Script 
{
	protected JSONObject config;		
	
	public Script(JSONObject cfg)
	{
		config = cfg;
	}
	
	public String getObjectName()
	{
		return config.getString("object");
	}

	public String getEventName()
	{
		return config.getString("event");
	}

	public String getAttributeName()
	{
		return config.getString("attribute");
	}

	public String getSource()
	{
		return StringUtils.unescape(config.getString("script"));
	}
}
