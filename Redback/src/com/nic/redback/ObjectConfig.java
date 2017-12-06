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
	protected HashMap<String, ArrayList<Script>> scripts;
	
	public ObjectConfig(JSONObject cfg)
	{
		config = cfg;
		attributes = new HashMap<String, AttributeConfig>();
		scripts = new HashMap<String, ArrayList<Script>>();
		JSONList list = config.getList("attributes");
		for(int i = 0; i < list.size(); i++)
		{
			JSONObject attrCfg = list.getObject(i);
			attributes.put(attrCfg.getString("name"), new AttributeConfig(attrCfg));
		}
	}
	
	public void addScript(Script script)
	{
		String attributeName = script.getAttributeName();
		if(attributeName != null)
		{
			getAttributeConfig(attributeName).addScript(script);
		}
		else
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
	
	public ArrayList<Script> getScriptsForEvent(String event)
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
							//JSONLiteral objectFilterValueJSONLiteral = (JSONLiteral)objectFilterValue;
							String objectFilterValueString = ((JSONLiteral)objectFilterValue).getString();
							if(objectFilterValueString.startsWith("*")  &&  objectFilterValueString.endsWith("*")  &&  objectFilterValueString.length() >= 2)
								dbFilterValue =  new JSONObject("{$regex:\"" + objectFilterValueString.substring(1, objectFilterValueString.length() - 1) + "\"}");
							else
								dbFilterValue = ((JSONLiteral)objectFilterValue).getCopy();
						}
						dbFilter.put(attributeDBKey, dbFilterValue);
					}
				}				
			}
		}

		return dbFilter;
	}
	
}
