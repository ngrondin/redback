package com.nic.redback.services.objectserver;

import java.util.ArrayList;
import java.util.HashMap;

import com.nic.firebus.utils.JSONObject;

public class AttributeConfig 
{
	protected JSONObject config;	
	protected HashMap<String, ArrayList<ScriptConfig>> scripts;
	
	public AttributeConfig(JSONObject cfg)
	{
		config = cfg;
		scripts = new HashMap<String, ArrayList<ScriptConfig>>();
	}

	public void addScript(ScriptConfig script)
	{
		String eventName = script.getEventName();
		ArrayList<ScriptConfig> eventScripts = scripts.get(eventName);
		if(eventScripts == null)
		{
			eventScripts = new ArrayList<ScriptConfig>();
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
	
	public ArrayList<ScriptConfig> getScriptsForEvent(String event)
	{
		return scripts.get(event);
	}
		
	protected JSONObject getJSON()
	{
		return config;
	}
	
	public String toString()
	{
		return config.toString();
	}
}
