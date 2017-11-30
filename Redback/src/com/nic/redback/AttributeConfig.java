package com.nic.redback;

import java.util.ArrayList;
import java.util.HashMap;

import com.nic.firebus.utils.JSONObject;

public class AttributeConfig 
{
	protected JSONObject config;	
	protected HashMap<String, ArrayList<Script>> scripts;
	
	public AttributeConfig(JSONObject cfg)
	{
		config = cfg;
		scripts = new HashMap<String, ArrayList<Script>>();
	}

	public void addScript(Script script)
	{
		String eventName = script.getEventName();
		ArrayList<Script> eventScripts = scripts.get(eventName);
		if(eventScripts == null)
		{
			eventScripts = new ArrayList<Script>();
			scripts.put(eventName, eventScripts);
		}
		eventScripts.add(script);
	}
	
	public String getName()
	{
		return config.getString("name");
	}
	
	public String getDBKey()
	{
		return config.getString("dbkey");
	}
	
	public String getExpression()
	{
		return config.getString("expression");
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
		return config.getObject("relatedobject") != null;
	}
	
	public RelatedObjectConfig getRelatedObjectConfig()
	{
		return new RelatedObjectConfig(config.getObject("relatedobject"));
	}
	
	public ArrayList<Script> getScriptsForEvent(String event)
	{
		return scripts.get(event);
	}
		
	protected JSONObject getJSON()
	{
		return config;
	}
}
