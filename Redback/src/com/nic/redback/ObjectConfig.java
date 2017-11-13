package com.nic.redback;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

import com.nic.firebus.exceptions.FunctionErrorException;
import com.nic.firebus.utils.JSONEntity;
import com.nic.firebus.utils.JSONException;
import com.nic.firebus.utils.JSONList;
import com.nic.firebus.utils.JSONLiteral;
import com.nic.firebus.utils.JSONObject;

public class ObjectConfig
{
	protected JSONObject config;
	protected HashMap<String, AttributeConfig> attributes;
	protected HashMap<String, ArrayList<String>> scripts;
	
	public ObjectConfig(JSONObject cfg)
	{
		config = cfg;
		attributes = new HashMap<String, AttributeConfig>();
		scripts = new HashMap<String, ArrayList<String>>();
		JSONList list = config.getList("attributes");
		for(int i = 0; i < list.size(); i++)
		{
			JSONObject attrCfg = list.getObject(i);
			attributes.put(attrCfg.getString("name"), new AttributeConfig(attrCfg));
		}
	}
	
	public void addScript(String event, String script)
	{
		ArrayList<String> eventScripts = scripts.get(event);
		if(eventScripts == null)
		{
			eventScripts = new ArrayList<String>();
			scripts.put(event, eventScripts);
		}
		eventScripts.add(script);
	}
	
	public void addAttributeScript(String name, String event, String script)
	{
		getAttributeConfig(name).addScript(event, script);
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
		return config.getString("uid");
	}

	public String getUIDGeneratorName()
	{
		return config.getString("uidgenerator");
	}

	public Set<String> getAttributeNames()
	{
		return attributes.keySet();
	}
	
	public AttributeConfig getAttributeConfig(String name)
	{
		return attributes.get(name);
	}
	
	public ArrayList<String> getScriptsForEvent(String event)
	{
		return scripts.get(event);
	}
	
	protected JSONObject generateDBFilter(JSONObject objectFilter) throws JSONException, FunctionErrorException
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
			else if(key.equals("$or"))
			{
				JSONList objectOrList = objectFilter.getList(key);
				JSONList dbOrList = new JSONList();
				for(int i = 0; i < objectOrList.size(); i++)
				{
					dbOrList.add(generateDBFilter(objectOrList.getObject(i)));
				}
				dbFilter.put("$or", dbOrList);
			}
			else if(key.equals("$multi"))
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
				dbFilter.put("$or", dbOrList);
			}			
			else if(key.equals("uid"))
			{
				dbFilter.put(getUIDDBKey(), objectFilter.getString(key));
			}
			else
			{
				AttributeConfig attributeConfig = getAttributeConfig(key);
				if(attributeConfig != null)
				{
					String attributeDBKey = attributeConfig.getDBKey();
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
							String filterValueStr = objectFilter.getString(key);
							if(filterValueStr.startsWith("*")  &&  filterValueStr.endsWith("*")  &&  filterValueStr.length() >= 2)
								dbFilterValue =  new JSONObject("{$regex:\"" + filterValueStr.substring(1, filterValueStr.length() - 1) + "\"}");
							else
								dbFilterValue = new JSONLiteral(filterValueStr);
						}
						dbFilter.put(attributeDBKey, dbFilterValue);
					}
				}				
			}
		}

		return dbFilter;
	}
	
}
