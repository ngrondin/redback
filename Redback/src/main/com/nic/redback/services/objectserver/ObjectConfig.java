package com.nic.redback.services.objectserver;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

import javax.script.Compilable;
import javax.script.CompiledScript;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;

import com.nic.firebus.exceptions.FunctionErrorException;
import com.nic.firebus.utils.JSONEntity;
import com.nic.firebus.utils.JSONException;
import com.nic.firebus.utils.JSONList;
import com.nic.firebus.utils.JSONLiteral;
import com.nic.firebus.utils.JSONObject;
import com.nic.redback.RedbackException;
import com.nic.redback.utils.StringUtils;

public class ObjectConfig
{
	protected JSONObject config;
	protected HashMap<String, AttributeConfig> attributes;
	protected HashMap<String, CompiledScript> scripts;
	
	public ObjectConfig(JSONObject cfg) throws RedbackException
	{
		config = cfg;
		attributes = new HashMap<String, AttributeConfig>();
		scripts = new HashMap<String, CompiledScript>();
		JSONList list = config.getList("attributes");
		for(int i = 0; i < list.size(); i++)
		{
			JSONObject attrCfg = list.getObject(i);
			attributes.put(attrCfg.getString("name"), new AttributeConfig(attrCfg, getName()));
		}
		
		JSONObject scriptsCfg = config.getObject("scripts");
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
	
	public JSONObject generateDBFilter(JSONObject objectFilter) throws JSONException, FunctionErrorException
	{
		JSONObject dbFilter = new JSONObject();
		Iterator<String> it = objectFilter.keySet().iterator();
		while(it.hasNext())
		{
			String key = it.next();
			if(key.equals("$eq")  ||  key.equals("$gt")  ||  key.equals("$gte")  ||  key.equals("$lt")  ||  key.equals("$lte")  ||  key.equals("$ne"))
			{
				dbFilter.put(key, objectFilter.getString(key));
			}
			else if(key.equals("$in")  ||  key.equals("$nin"))
			{
				dbFilter.put(key, objectFilter.getList(key));
			}
			else if(key.equals("$or") || key.equals("$and"))
			{
				JSONList objectList = objectFilter.getList(key);
				JSONList dbList = new JSONList();
				for(int i = 0; i < objectList.size(); i++)
				{
					dbList.add(generateDBFilter(objectList.getObject(i)));
				}
				dbFilter.put(key, dbList);
			}
/*			else if(key.equals("$multi"))
			{
				JSONList dbOrList = new JSONList();
				Iterator<String> it2 = getAttributeNames().iterator();
				while(it2.hasNext())
				{
					AttributeConfig attributeConfig = getAttributeConfig(it2.next());
					if(attributeConfig.getDBKey() != null)
					{
						JSONObject orTerm = new JSONObject();
						orTerm.put(attributeConfig.getName(), objectFilter.get(key));
						dbOrList.add(generateDBFilter(orTerm));
					}
				}
				dbFilter.put("$or", generateDBMultiOrList(objectFilter.get(key)));
			}	*/		
			else
			{
				String attributeDBKey = null; 
				AttributeConfig attributeConfig = getAttributeConfig(key);
				if(key.equals("uid"))
					attributeDBKey = getUIDDBKey();
				else if(attributeConfig != null)
					attributeDBKey = attributeConfig.getDBKey();
				
				if(attributeDBKey != null)
				{
					JSONEntity objectFilterValue = objectFilter.get(key);
					JSONEntity dbFilterValue = null;
					if(objectFilterValue instanceof JSONObject)
					{
						dbFilterValue = generateDBFilter((JSONObject)objectFilterValue);
					}
					else if(objectFilterValue instanceof JSONLiteral)
					{
						String objectFilterValueString = ((JSONLiteral)objectFilterValue).getString();
						if(objectFilterValueString != null  &&  objectFilterValueString.startsWith("*")  &&  objectFilterValueString.endsWith("*")  &&  objectFilterValueString.length() >= 2)
							dbFilterValue =  new JSONObject("{$regex:\"" + objectFilterValueString.substring(1, objectFilterValueString.length() - 1) + "\"}");
						else
							dbFilterValue = ((JSONLiteral)objectFilterValue).getCopy();
					}
					dbFilter.put(attributeDBKey, dbFilterValue);
				}
			}
		}

		return dbFilter;
	}
	
}
