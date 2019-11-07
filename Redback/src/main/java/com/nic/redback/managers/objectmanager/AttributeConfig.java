package com.nic.redback.managers.objectmanager;

import java.util.HashMap;
import java.util.Iterator;

import javax.script.Compilable;
import javax.script.CompiledScript;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import com.nic.firebus.utils.DataMap;
import com.nic.redback.RedbackException;
import com.nic.redback.utils.StringUtils;

public class AttributeConfig 
{
	protected DataMap config;	
	protected String objectName;
	protected HashMap<String, CompiledScript> scripts;
	protected RelatedObjectConfig relatedObjectConfig;
	protected Expression editable;
	protected Expression expression;
	
	public AttributeConfig(DataMap cfg, String on) throws RedbackException
	{
		config = cfg;
		objectName = on;
		scripts = new HashMap<String, CompiledScript>();
		if(config.get("relatedobject") != null)
			relatedObjectConfig = new RelatedObjectConfig(config.getObject("relatedobject"));
		
		if(config.get("editable") != null)
			editable = new Expression(config.getString("editable"));
		else 
			editable = new Expression("true");
		
		if(config.get("expression") != null)
			expression = new Expression(config.getString("expression"));
		
		DataMap scriptsCfg = config.getObject("scripts");
		if(scriptsCfg != null)
		{
			Iterator<String> events = scriptsCfg.keySet().iterator();
			while(events.hasNext())
			{
				String event = events.next();
				String scriptName = objectName + "." + getName() + "." + event;
				try
				{
					ScriptEngine jsEngine = new ScriptEngineManager().getEngineByName("javascript");
					jsEngine.put(ScriptEngine.FILENAME, scriptName);
					String source = StringUtils.unescape(scriptsCfg.getString(event));
					CompiledScript script = ((Compilable)jsEngine).compile(source);
					scripts.put(event, script);
				} 
				catch(ScriptException e)
				{
					throw new RedbackException("Problem compiling script", e);
				}
			}			
		}
	}
	
	public String getName()
	{
		return config.getString("name");
	}
	
	public String getDBKey()
	{
		return config.getString("dbkey");
	}
	
	public Expression getExpression()
	{
		return expression;
	}
	
	public Expression getEditableExpression()
	{
		return editable;
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
		return relatedObjectConfig;
	}
	
	public CompiledScript getScriptForEvent(String event)
	{
		return scripts.get(event);
	}
		
	protected DataMap getJSON()
	{
		return config;
	}
	
	public String toString()
	{
		return config.toString();
	}
}
