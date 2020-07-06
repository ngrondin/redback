package io.redback.managers.objectmanager;

import java.util.HashMap;
import java.util.Iterator;


import io.firebus.utils.DataMap;
import io.redback.RedbackException;
import io.redback.managers.jsmanager.Expression;
import io.redback.managers.jsmanager.Function;

public class AttributeConfig 
{
	protected ObjectManager objectManager;
	protected ObjectConfig objectConfig;
	protected DataMap config;	
	protected HashMap<String, Function> scripts;
	protected RelatedObjectConfig relatedObjectConfig;
	protected Expression editable;
	protected Expression expression;
	protected Expression defaultValue;
	
	public AttributeConfig(ObjectManager om, ObjectConfig oc, DataMap cfg) throws RedbackException
	{
		objectManager = om;
		objectConfig = oc;
		config = cfg;
		scripts = new HashMap<String, Function>();

		if(config.get("relatedobject") != null)
			relatedObjectConfig = new RelatedObjectConfig(objectManager, objectConfig, this, config.getObject("relatedobject"));
		
		if(config.get("editable") != null && config.getString("editable").length() > 0)
			editable = new Expression(objectManager.getJSManager(), oc.getName() + "_attr_" + getName() + "_editable", objectConfig.getScriptVariables(), config.getString("editable"));
		else 
			editable = new Expression(objectManager.getJSManager(), oc.getName() + "_attr_" + getName() + "_editable", objectConfig.getScriptVariables(), "true");
		
		if(config.get("expression") != null && config.getString("expression").length() > 0)
			expression = new Expression(objectManager.getJSManager(), oc.getName() + "_attr_" + getName() + "_expression", objectConfig.getScriptVariables(), config.getString("expression"));

		if(config.get("default") != null && config.getString("default").length() > 0)
			defaultValue = new Expression(objectManager.getJSManager(), oc.getName() + "_attr_" + getName() + "_default", objectConfig.getScriptVariables(), config.getString("default"));
		
		DataMap scriptsCfg = config.getObject("scripts");
		if(scriptsCfg != null)
		{
			Iterator<String> events = scriptsCfg.keySet().iterator();
			while(events.hasNext())
			{
				String event = events.next();
				try
				{
					Function function = new Function(objectManager.getJSManager(), oc.getName() + "_" + getName() + "_event_" + event, objectConfig.getScriptVariables(), scriptsCfg.getString(event));
					scripts.put(event, function);
				} 
				catch(RedbackException e)
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
	
	public Expression getDefaultValue()
	{
		return defaultValue;
	}

	public boolean hasRelatedObject()
	{
		return config.getObject("relatedobject") != null;
	}
	
	public boolean canBeSearched()
	{
		return config.getBoolean("search");
	}
	
	public RelatedObjectConfig getRelatedObjectConfig()
	{
		return relatedObjectConfig;
	}
	
	public Function getScriptForEvent(String event)
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
