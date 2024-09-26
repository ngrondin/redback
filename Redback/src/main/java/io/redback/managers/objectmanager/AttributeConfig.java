package io.redback.managers.objectmanager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import io.firebus.data.DataMap;
import io.firebus.script.Expression;
import io.firebus.script.Function;
import io.redback.exceptions.RedbackException;

public class AttributeConfig 
{
	protected ObjectManager objectManager;
	protected ObjectConfig objectConfig;
	protected DataMap config;	
	protected HashMap<String, Function> scripts;
	protected RelatedObjectConfig relatedObjectConfig;
	protected Expression editable;
	protected Expression mandatory;
	protected Expression expression;
	protected Expression defaultValue;
	
	public AttributeConfig(ObjectManager om, ObjectConfig oc, DataMap cfg) throws RedbackException
	{
		objectManager = om;
		objectConfig = oc;
		config = cfg;
		scripts = new HashMap<String, Function>();

		try {
			if(config.get("relatedobject") != null)
				relatedObjectConfig = new RelatedObjectConfig(objectManager, objectConfig, this, config.getObject("relatedobject"));
			
			if(config.get("editable") != null && config.getString("editable").length() > 0)
				editable = objectManager.getScriptFactory().createExpression(oc.getName() + "_attr_" + getName() + "_editable", config.getString("editable"));
			else 
				editable = objectManager.getScriptFactory().createExpression(oc.getName() + "_attr_" + getName() + "_editable", "true");
	
			if(config.get("mandatory") != null && config.getString("mandatory").length() > 0)
				mandatory = objectManager.getScriptFactory().createExpression(oc.getName() + "_attr_" + getName() + "_mandatory", config.getString("mandatory"));
			else 
				mandatory = objectManager.getScriptFactory().createExpression(oc.getName() + "_attr_" + getName() + "_mandatory", "false");
	
			if(config.get("expression") != null && config.getString("expression").length() > 0)
				expression = objectManager.getScriptFactory().createExpression(oc.getName() + "_attr_" + getName() + "_expression", config.getString("expression"));
	
			if(config.get("default") != null && config.getString("default").length() > 0)
				defaultValue = objectManager.getScriptFactory().createExpression(oc.getName() + "_attr_" + getName() + "_default", config.getString("default"));
			
			DataMap scriptsCfg = config.getObject("scripts");
			List<String> scriptVars = new ArrayList<String>(objectConfig.getScriptVariables());
			scriptVars.add("previousValue");
			if(scriptsCfg != null)
			{
				Iterator<String> events = scriptsCfg.keySet().iterator();
				while(events.hasNext())
				{
					String event = events.next();
					String name = oc.getName() + "_" + getName() + "_event_" + event;
					Function function = objectManager.getScriptFactory().createFunction(name, scriptVars.toArray(new String[] {}), scriptsCfg.getString(event));
					scripts.put(event, function);

				}			
			}
		} catch(Exception e) {
			throw new RedbackException("Error initialising attribute config", e);
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
	
	public Expression getMandatoryExpression()
	{
		return mandatory;
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
	
	public boolean noTrace()
	{
		return config.getBoolean("notrace");
	}
	
	public boolean isInternal()
	{
		return config.getBoolean("internal");
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
