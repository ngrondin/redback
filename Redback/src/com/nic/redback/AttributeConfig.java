package com.nic.redback;

import com.nic.firebus.utils.JSONObject;

public class AttributeConfig 
{
	protected JSONObject config;	
	
	public AttributeConfig(JSONObject cfg)
	{
		config = cfg;
	}

	public String getName()
	{
		return config.getString("name");
	}
	
	public String getDBKey()
	{
		return config.getString("key");
	}
	
	public String getEditableExpression()
	{
		return config.getString("editable");
	}
	
	public String getIdGeneratorName()
	{
		return config.getString("idgenerator");
	}
	
	public String getDefaultValue()
	{
		return config.getString("default");
	}

	public boolean hasRelatedObject()
	{
		return config.getString("relatedobject.name") != null;
	}
	
	public JSONObject getRelatedObject()
	{
		return config.getObject("relatedobject");
	}
	
	public String getRelatedObjectName()
	{
		return config.getString("relatedobject.name");
	}
	
	public String getRelatedObjectValueAttribute()
	{
		return config.getString("relatedobject.valueattribute");
	}
	
	public JSONObject getRelatedObjectRelationship()
	{
		return config.getObject("relatedobject.relationship");
	}
	
	protected JSONObject getJSON()
	{
		return config;
	}
}
