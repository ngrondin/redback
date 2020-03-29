package io.redback.managers.objectmanager;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.script.Compilable;
import javax.script.CompiledScript;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;

import io.firebus.exceptions.FunctionErrorException;
import io.firebus.utils.DataEntity;
import io.firebus.utils.DataException;
import io.firebus.utils.DataList;
import io.firebus.utils.DataLiteral;
import io.firebus.utils.DataMap;
import io.redback.RedbackException;
import io.redback.utils.StringUtils;

public class ObjectConfig
{
	protected DataMap config;
	protected HashMap<String, AttributeConfig> attributes;
	protected HashMap<String, CompiledScript> scripts;
	
	public ObjectConfig(DataMap cfg, List<IncludeScript> includes) throws RedbackException
	{
		config = cfg;
		attributes = new HashMap<String, AttributeConfig>();
		scripts = new HashMap<String, CompiledScript>();
		DataList list = config.getList("attributes");
		for(int i = 0; i < list.size(); i++)
		{
			DataMap attrCfg = list.getObject(i);
			attributes.put(attrCfg.getString("name"), new AttributeConfig(attrCfg, getName()));
		}
		
		StringBuilder allIncludes = new StringBuilder();
		if(includes != null)
		{
			for(int i = 0; i < includes.size(); i++)
			{
				allIncludes.append(includes.get(i).getSource());
				allIncludes.append("\r\n\r\n");
			}
		}
		
		DataMap scriptsCfg = config.getObject("scripts");
		if(scriptsCfg != null)
		{
			Iterator<String> events = scriptsCfg.keySet().iterator();
			while(events.hasNext())
			{
				String event = events.next();
				String scriptName = getName() + "." + event;
				try
				{
					ScriptEngine jsEngine = new ScriptEngineManager().getEngineByName("javascript");
					jsEngine.put(ScriptEngine.FILENAME, scriptName);
					String source = StringUtils.unescape(scriptsCfg.getString(event));
					source = allIncludes.toString() + source;
					CompiledScript script = ((Compilable)jsEngine).compile(source);
					scripts.put(event, script);
				} 
				catch(Exception e)
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

	public Set<String> getAttributeNames()
	{
		return attributes.keySet();
	}
	
	public AttributeConfig getAttributeConfig(String name)
	{
		return attributes.get(name);
	}
	
	public CompiledScript getScriptForEvent(String event)
	{
		return scripts.get(event);
	}
	

}