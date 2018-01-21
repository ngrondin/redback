package com.nic.redback;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.logging.Logger;

import javax.script.ScriptException;

import com.nic.firebus.Firebus;
import com.nic.firebus.Payload;
import com.nic.firebus.exceptions.FunctionErrorException;
import com.nic.firebus.exceptions.FunctionTimeoutException;
import com.nic.firebus.utils.JSONException;
import com.nic.firebus.utils.JSONList;
import com.nic.firebus.utils.JSONObject;
import com.nic.redback.security.UserProfile;

public class ObjectManager
{
	private Logger logger = Logger.getLogger("com.nic.redback");
	protected Firebus firebus;
	protected String configService;
	protected boolean cacheConfigs;
	protected String dataService;
	protected String idGeneratorService;
	protected HashMap<String, ObjectConfig> objectConfigs;

	public ObjectManager(JSONObject config)
	{
		cacheConfigs = true;
		configService = config.getString("configservice");
		dataService = config.getString("dataservice");
		idGeneratorService = config.getString("idgeneratorservice");
		if(config.containsKey("cacheconfigs") &&  config.getString("cacheconfigs").equalsIgnoreCase("false"))
			cacheConfigs = false;
		objectConfigs = new HashMap<String, ObjectConfig>();
	}
	
	public void setFirebus(Firebus fb)
	{
		firebus = fb;
	}
	
	public void refreshAllConfigs()
	{
		objectConfigs.clear();
	}
	
	protected ObjectConfig getObjectConfig(String object) throws RedbackException
	{
		ObjectConfig objectConfig = objectConfigs.get(object);
		if(objectConfig == null)
		{
			try
			{
			JSONObject configList = request(configService, new JSONObject("{object:rbo_config,filter:{name:" + object + "}}"));
			if(configList.getList("result").size() > 0)
			{
				objectConfig = new ObjectConfig(configList.getObject("result.0"));
				JSONObject scriptList = request(configService, new JSONObject("{object:rbo_script,filter:{object:" + object + "}}"));
				for(int i = 0; i < scriptList.getList("result").size(); i++)
					objectConfig.addScript(new Script(scriptList.getList("result").getObject(i)));
				if(cacheConfigs)
					objectConfigs.put(object, objectConfig);
			}
			}
			catch(Exception e)
			{
				logger.severe(e.getMessage());
				throw new RedbackException("Exception getting object config", e);
			}
		}
		return objectConfig;
	}


	
	protected void addRelatedBulk(UserProfile userProfile, ArrayList<RedbackObject> objects) throws RedbackException, ScriptException
	{
		if(objects != null  && objects.size() > 0)
		{
			ObjectConfig objectConfig = objects.get(0).getObjectConfig();
			Iterator<String> it = objectConfig.getAttributeNames().iterator();
			while(it.hasNext())
			{
				AttributeConfig attributeConfig = objectConfig.getAttributeConfig(it.next());
				String attributeName = attributeConfig.getName();
				if(attributeConfig.hasRelatedObject())
				{
					RelatedObjectConfig relatedObjectConfig = attributeConfig.getRelatedObjectConfig();
					JSONList orList = new JSONList();
					for(int j = 0; j < objects.size(); j++)
					{
						RedbackObject object = objects.get(j);
						if(object.getString(attributeName) != null)
							orList.add(object.getRelatedObjectFindFilter(attributeName));
					}
					JSONObject relatedObjectFilter = new JSONObject();
					relatedObjectFilter.put("$or", orList);
					ArrayList<RedbackObject> result = getObjectList(userProfile, relatedObjectConfig.getObjectName(), relatedObjectFilter, false);
					for(int k = 0; k < result.size(); k++)
					{
						RedbackObject resultObject = result.get(k);
						Value resultObjectLinkValue = resultObject.get(relatedObjectConfig.getLinkAttributeName());
						for(int j = 0; j < objects.size(); j++)
						{
							RedbackObject object = objects.get(j);
							Value linkValue = object.get(attributeName);
							if(linkValue != null  &&  linkValue.equals(resultObjectLinkValue))
								object.put(attributeName, resultObject);
						}
					}
				}
			}
		}
	}

	
	public RedbackObject getObject(UserProfile userProfile, String objectName, String id, boolean addRelated) throws RedbackException
	{
		ObjectConfig objectConfig = getObjectConfig(objectName);
		RedbackObject object = new RedbackObject(userProfile, this, objectConfig, id, addRelated);;
		return object;
	}
	
	
	public ArrayList<RedbackObject> getObjectList(UserProfile userProfile, String objectName, JSONObject filterData, boolean addRelated) throws RedbackException
	{
		ArrayList<RedbackObject> objectList = new ArrayList<RedbackObject>();
		ObjectConfig objectConfig = getObjectConfig(objectName);
		try
		{
			JSONObject dbFilter = objectConfig.generateDBFilter(filterData);
			dbFilter.put("domain", userProfile.getDBFilterDomainClause());
			JSONObject dbResult = requestData(objectConfig.getCollection(), dbFilter);
			JSONList dbResultList = dbResult.getList("result");
			
			for(int i = 0; i < dbResultList.size(); i++)
			{
				JSONObject dbData = dbResultList.getObject(i);
				RedbackObject object = new RedbackObject(userProfile, this, objectConfig, dbData, false);
				objectList.add(object);
			}
			if(addRelated)
				addRelatedBulk(userProfile, objectList);
		}
		catch(Exception e)
		{
			logger.severe(e.getMessage());
			throw new RedbackException("Error getting object list", e);
		}
		return objectList;
	}
	
	
	public ArrayList<RedbackObject> getObjectList(UserProfile userProfile, String objectName, String uid, String attributeName, JSONObject filterData, boolean addRelated) throws RedbackException
	{
		RedbackObject object = getObject(userProfile, objectName, uid, false);
		ObjectConfig objectConfig = getObjectConfig(objectName);
		ArrayList<RedbackObject> objectList = null;
		AttributeConfig attributeConfig = objectConfig.getAttributeConfig(attributeName);
		if(attributeConfig.hasRelatedObject())
		{
			RelatedObjectConfig relatedObjectConfig = attributeConfig.getRelatedObjectConfig();
			JSONObject relatedObjectListFilter = object.getRelatedObjectListFilter(attributeName);
			Iterator<String> it = filterData.keySet().iterator();
			while(it.hasNext())
			{
				String key = it.next();
				relatedObjectListFilter.put(key, filterData.get(key));
			}
			objectList = getObjectList(userProfile, relatedObjectConfig.getObjectName(), relatedObjectListFilter, false);
		}
		return objectList;
	}
	
	public RedbackObject updateObject(UserProfile userProfile, String objectName, String id, JSONObject updateData, boolean addRelated) throws RedbackException, ScriptException
	{
		RedbackObject object = getObject(userProfile, objectName, id, false);
		if(object != null)
		{
			Iterator<String> it = updateData.keySet().iterator();
			while(it.hasNext())
			{
				String attributeName = it.next();
				object.put(attributeName, new Value(updateData.get(attributeName)));
			}
			if(addRelated)
				object.loadRelated();
			object.save();
		}
		return object;
	}
	
	public RedbackObject createObject(UserProfile userProfile, String objectName, JSONObject initialData, boolean addRelated) throws RedbackException, ScriptException
	{
		ObjectConfig objectConfig = getObjectConfig(objectName);
		RedbackObject object = new RedbackObject(userProfile, this, objectConfig);
		Iterator<String> it = initialData.keySet().iterator();
		while(it.hasNext())
		{
			String attributeName = it.next();
			object.put(attributeName, new Value(initialData.get(attributeName)));
		}
		if(addRelated)
			object.loadRelated();
		object.save();
		return object;
	}
	
	public RedbackObject executeFunction(UserProfile userProfile, String objectName, String id, String function, JSONObject updateData, boolean addRelated) throws RedbackException, ScriptException
	{
		RedbackObject object = getObject(userProfile, objectName, id, false);
		if(object != null)
		{
			if(updateData != null)
			{
				Iterator<String> it = updateData.keySet().iterator();
				while(it.hasNext())
				{
					String attributeName = it.next();
					object.put(attributeName, updateData.getString(attributeName));
				}
			}
			if(addRelated)
				object.loadRelated();
			object.execute(function);
			object.save();
		}
		return object;
	}
	
	public Value getID(String name) throws FunctionErrorException, FunctionTimeoutException
	{
		String value = firebus.requestService(idGeneratorService, new Payload(name)).getString();
		return new Value(value);
	}

	
	protected JSONObject request(String service, JSONObject request) throws JSONException, FunctionErrorException, FunctionTimeoutException
	{
		Payload reqPayload = new Payload(request.toString());
		logger.info("Requesting firebus service : " + service);
		Payload respPayload = firebus.requestService(service, reqPayload);
		logger.info("Receiving firebus service respnse from : " + service);
		String respStr = respPayload.getString();
		JSONObject result = new JSONObject(respStr);
		return result;
	}
	
	public JSONObject requestData(String objectName, JSONObject filter) throws JSONException, FunctionErrorException, FunctionTimeoutException
	{
		JSONObject request = new JSONObject();
		request.put("object", objectName);
		request.put("filter", filter);
		return request(dataService, request);
	}
	
	public void publishData(String collection, JSONObject data)
	{
		firebus.publish(dataService, new Payload("{object:" + collection + ",data:" + data + "}"));
	}

}

