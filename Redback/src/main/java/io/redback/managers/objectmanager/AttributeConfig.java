package io.redback.managers.objectmanager;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import javax.script.Compilable;
import javax.script.CompiledScript;
import javax.script.ScriptException;

import io.firebus.utils.DataMap;
import io.redback.RedbackException;
import io.redback.utils.Expression;
import io.redback.utils.StringUtils;

public class AttributeConfig 
{
	protected ObjectManager objectManager;
	protected DataMap config;	
	protected String objectName;
	protected HashMap<String, CompiledScript> scripts;
	protected RelatedObjectConfig relatedObjectConfig;
	protected Expression editable;
	protected Expression expression;
	protected Expression defaultValue;
	
	public AttributeConfig(ObjectManager om, String on, DataMap cfg) throws RedbackException
	{
		objectManager = om;
		objectName = on;
		config = cfg;
		scripts = new HashMap<String, CompiledScript>();
		List<ScriptConfig> includes = objectManager.getIncludeScripts();
		StringBuilder allIncludes = new StringBuilder();
		allIncludes.append("\r\n\r\n");
		if(includes != null)
		{
			for(int i = 0; i < includes.size(); i++)
			{
				allIncludes.append(includes.get(i).getSource());
				allIncludes.append("\r\n\r\n");
			}
		}

		if(config.get("relatedobject") != null)
			relatedObjectConfig = new RelatedObjectConfig(objectManager, config.getObject("relatedobject"));
		
		if(config.get("editable") != null && config.getString("editable").length() > 0)
			editable = new Expression(objectManager.getScriptEngine(), config.getString("editable"));
		else 
			editable = new Expression(objectManager.getScriptEngine(), "true");
		
		if(config.get("expression") != null && config.getString("expression").length() > 0)
			expression = new Expression(objectManager.getScriptEngine(), config.getString("expression"));

		if(config.get("default") != null && config.getString("default").length() > 0)
			defaultValue = new Expression(objectManager.getScriptEngine(), config.getString("default"));
		
		DataMap scriptsCfg = config.getObject("scripts");
		if(scriptsCfg != null)
		{
			Iterator<String> events = scriptsCfg.keySet().iterator();
			while(events.hasNext())
			{
				String event = events.next();
				//String scriptName = objectName + "." + getName() + "." + event;
				try
				{
					//ScriptEngine jsEngine = new ScriptEngineManager().getEngineByName("javascript");
					//jsEngine.put(ScriptEngine.FILENAME, scriptName);
					String source = StringUtils.unescape(scriptsCfg.getString(event));
					source = source + allIncludes.toString();
					CompiledScript script = ((Compilable)objectManager.getScriptEngine()).compile(source);
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
