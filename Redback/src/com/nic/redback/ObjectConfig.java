package com.nic.redback;

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
	
	public ObjectConfig(JSONObject cfg)
	{
		config = cfg;
		attributes = new HashMap<String, AttributeConfig>();
		JSONList list = config.getList("attributes");
		for(int i = 0; i < list.size(); i++)
		{
			JSONObject attrCfg = list.getObject(i);
			attributes.put(attrCfg.getString("name"), new AttributeConfig(attrCfg));
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
	
	protected JSONObject generateDBFilter(JSONObject objectFilter) throws JSONException, FunctionErrorException
	{
		JSONObject dbFilter = new JSONObject();

		JSONList anyFilterList = new JSONList();
		JSONEntity anyFilterDBValue = null;
		if(objectFilter.get("_any") != null)
			anyFilterDBValue =	generateDBAttributeFilterValue(objectFilter.get("_any"));

		if(objectFilter.get("uid") != null)
			dbFilter.put(getUIDDBKey(), generateDBAttributeFilterValue(objectFilter.get("uid")));
		
		Iterator<String> it = getAttributeNames().iterator();
		while(it.hasNext())
		{
			AttributeConfig attributeConfig = getAttributeConfig(it.next());
			String attrName = attributeConfig.getName();
			String attrDBKey = attributeConfig.getDBKey();
			JSONEntity attrFilter = objectFilter.get(attrName);
			if(attrFilter != null)
			{
				dbFilter.put(attrDBKey, generateDBAttributeFilterValue(attrFilter));
			}
			else if(anyFilterDBValue != null)
			{
				JSONObject orTerm = new JSONObject();
				orTerm.put(attrDBKey, anyFilterDBValue);
				anyFilterList.add(orTerm);
			}
		}
		if(anyFilterList.size() > 0)
			dbFilter.put("$or", anyFilterList);
		return dbFilter;
	}
	
	protected JSONEntity generateDBAttributeFilterValue(JSONEntity attrFilterValue) throws JSONException
	{
		JSONEntity dbAttributeFilterValue = null;
		if(attrFilterValue != null)
		{
			if(attrFilterValue instanceof JSONLiteral)
			{
				String filterValueStr = ((JSONLiteral)attrFilterValue).getString();
				if(filterValueStr.startsWith("*")  &&  filterValueStr.endsWith("*")  &&  filterValueStr.length() >= 2)
					dbAttributeFilterValue = new JSONObject("{$regex:\"" + filterValueStr.substring(1, filterValueStr.length() - 1) + "\"}");
				else
					dbAttributeFilterValue = attrFilterValue;
			}
			if(attrFilterValue instanceof JSONList)
			{
				JSONList attrValueList = (JSONList)attrFilterValue;
				dbAttributeFilterValue = new JSONObject();
				((JSONObject)dbAttributeFilterValue).put("$in", attrValueList);
			}
		}
		return dbAttributeFilterValue;
	}

}
