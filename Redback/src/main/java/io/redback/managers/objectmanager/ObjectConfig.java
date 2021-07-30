package io.redback.managers.objectmanager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;


import io.firebus.utils.DataList;
import io.firebus.utils.DataMap;
import io.redback.exceptions.RedbackException;
import io.redback.managers.jsmanager.Expression;
import io.redback.managers.jsmanager.Function;

public class ObjectConfig
{
	protected ObjectManager objectManager;
	protected DataMap config;
	protected HashMap<String, AttributeConfig> attributes;
	protected HashMap<String, Function> scripts;
	protected Function generationScript;
	protected Expression canDeleteExpr;
	protected List<String> scriptVars;
	
	public ObjectConfig(ObjectManager om, DataMap cfg) throws RedbackException
	{
		objectManager = om;
		config = cfg;
		attributes = new HashMap<String, AttributeConfig>();
		scripts = new HashMap<String, Function>();

		scriptVars = new ArrayList<String>();
		scriptVars.add("session");
		scriptVars.add("userprofile");
		scriptVars.add("firebus");
		scriptVars.add("om");
		scriptVars.add("pm"); //Deprecated
		scriptVars.add("pc");
		scriptVars.add("geo");
		scriptVars.add("fc");
		scriptVars.add("rc");
		scriptVars.add("nc");
		scriptVars.add("dc");
		scriptVars.add("ic");
		scriptVars.add("self");
		scriptVars.add("canRead");
		scriptVars.add("canWrite");
		scriptVars.add("canExecute");
		scriptVars.add("uid");
		
		DataList list = config.getList("attributes");
		for(int i = 0; i < list.size(); i++)
			scriptVars.add(list.getObject(i).getString("name"));
		
		for(int i = 0; i < list.size(); i++)
		{
			DataMap attrCfg = list.getObject(i);
			attributes.put(attrCfg.getString("name"), new AttributeConfig(objectManager, this, attrCfg));
		}
		
		if(config.containsKey("datagen")) {
			List<String> scriptVars2 = new ArrayList<String>(scriptVars);
			scriptVars2.add("filter");
			scriptVars2.add("sort");
			scriptVars2.add("search");
			scriptVars2.add("tuple");
			scriptVars2.add("metrics");
			scriptVars2.add("page");
			scriptVars2.add("pageSize");
			scriptVars2.add("action");
			generationScript = new Function(objectManager.getJSManager(), getName() + "_datagen", scriptVars2, config.getString("datagen"));
		}
				
		DataMap scriptsCfg = config.getObject("scripts");
		if(scriptsCfg != null)
		{
			Iterator<String> events = scriptsCfg.keySet().iterator();
			while(events.hasNext())
			{
				String event = events.next();
				try
				{
					Function func = new Function(objectManager.getJSManager(), getName() + "_event_" + event, scriptVars, scriptsCfg.getString(event));
					scripts.put(event, func);
				} 
				catch(Exception e)
				{
					throw new RedbackException("Problem compiling script", e);
				}
			}			
		}
		
		canDeleteExpr = new Expression(objectManager.getJSManager(), getName() + "_candelete", scriptVars, (config.getString("candelete") != null ? config.getString("candelete") : "false"));
	}

	public String getName()
	{
		return config.getString("name");
	}

	public String getCollection()
	{
		return config.getString("collection");
	}

	public String getUIDDBKey()
	{
		return config.getString("uiddbkey");
	}

	public String getUIDGeneratorName()
	{
		return config.getString("uidgenerator");
	}

	public String getDomainDBKey()
	{
		return config.getString("domaindbkey");
	}
	
	public boolean isDomainManaged()
	{
		String domainDBKey = config.getString("domaindbkey");
		if(domainDBKey != null && domainDBKey.length() > 0)
			return true;
		else
			return false;
	}
	
	public boolean isPersistent() 
	{
		if(config.containsKey("collection"))
			return true;
		else 
			return false;
	}

	public Set<String> getAttributeNames()
	{
		return attributes.keySet();
	}
	
	public AttributeConfig getAttributeConfig(String name)
	{
		return attributes.get(name);
	}
	
	public Function getScriptForEvent(String event)
	{
		return scripts.get(event);
	}
	
	public Expression getCanDeleteExpression()
	{
		return canDeleteExpr;
	}
	
	public List<String> getScriptVariables()
	{
		return scriptVars;
	}
	
	public Function getGenerationScript()
	{
		return generationScript;
	}

}
