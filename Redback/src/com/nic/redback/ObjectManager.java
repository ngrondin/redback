package com.nic.redback;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.logging.Logger;

import com.nic.firebus.Firebus;
import com.nic.firebus.Payload;
import com.nic.firebus.exceptions.FunctionErrorException;
import com.nic.firebus.utils.JSONException;
import com.nic.firebus.utils.JSONList;
import com.nic.firebus.utils.JSONObject;

public class ObjectManager
{
	private Logger logger = Logger.getLogger("com.nic.redback");
	protected Firebus firebus;
	protected String configService;
	protected String dataService;
	protected String idGeneratorService;
	protected HashMap<String, ObjectConfig> objectConfigs;

	public ObjectManager(String cs, String ds, String igs)
	{
		configService = cs;
		dataService = ds;
		idGeneratorService = igs;
		objectConfigs = new HashMap<String, ObjectConfig>();
	}
	
	public void setFirebus(Firebus fb)
	{
		firebus = fb;
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
				{
					JSONObject scriptEntry = scriptList.getList("result").getObject(i);
					String event = scriptEntry.getString("event");
					String attribute = scriptEntry.getString("attribute");
					String script = scriptEntry.getString("script");
					if(attribute != null)
						objectConfig.addAttributeScript(attribute, event, script);
					else
						objectConfig.addScript(event, script);
				}
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

	
	
	protected void addRelatedBulk(RedbackObject object) throws RedbackException
	{
		ArrayList<RedbackObject> objects = new ArrayList<RedbackObject>();
		objects.add(object);
		addRelatedBulk(objects);
	}
	
	protected void addRelatedBulk(ArrayList<RedbackObject> objects) throws RedbackException
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
				ArrayList<RedbackObject> result = getObjectList(relatedObjectConfig.getObjectName(), relatedObjectFilter, false);
				for(int k = 0; k < result.size(); k++)
				{
					RedbackObject resultObject = result.get(k);
					String resultObjectLinkValue = resultObject.getString(relatedObjectConfig.getLinkAttributeName());
					for(int j = 0; j < objects.size(); j++)
					{
						RedbackObject object = objects.get(j);
						String linkValue = object.getString(attributeName);
						if(linkValue != null  &&  linkValue.equals(resultObjectLinkValue))
							object.put(attributeName, resultObject);
					}
				}
			}
		}
	}

	
	public RedbackObject getObject(String objectName, String id, boolean addRelated) throws RedbackException
	{
		ObjectConfig objectConfig = getObjectConfig(objectName);
		RedbackObject object = new RedbackObject(this, objectConfig, id, addRelated);;
		return object;
	}
	
	
	public ArrayList<RedbackObject> getObjectList(String objectName, JSONObject filterData, boolean addRelated) throws RedbackException
	{
		ArrayList<RedbackObject> objectList = new ArrayList<RedbackObject>();
		ObjectConfig objectConfig = getObjectConfig(objectName);
		try
		{
			JSONObject dbFilter = objectConfig.generateDBFilter(filterData);
			JSONObject dbResult = request(dataService, new JSONObject("{object:" + objectConfig.getCollection() + ",filter:" + dbFilter + "}"));
			JSONList dbResultList = dbResult.getList("result");
			
			for(int i = 0; i < dbResultList.size(); i++)
			{
				JSONObject dbData = dbResultList.getObject(i);
				RedbackObject object = new RedbackObject(this, objectConfig, dbData, false);
				objectList.add(object);
			}
			if(addRelated)
				addRelatedBulk(objectList);
		}
		catch(Exception e)
		{
			logger.severe(e.getMessage());
			throw new RedbackException("Error getting object list", e);
		}
		return objectList;
	}
	
	
	public RedbackObject updateObject(String objectName, String id, JSONObject updateData, boolean addRelated) throws RedbackException
	{
		RedbackObject object = getObject(objectName, id, false);
		if(object != null)
		{
			Iterator<String> it = updateData.keySet().iterator();
			while(it.hasNext())
			{
				String attributeName = it.next();
				object.put(attributeName, updateData.getString(attributeName));
			}
			if(addRelated)
				object.loadRelated();
			object.save();
		}
		return object;
	}
	
	public RedbackObject createObject(String objectName, JSONObject initialData, boolean addRelated) throws RedbackException
	{
		ObjectConfig objectConfig = getObjectConfig(objectName);
		RedbackObject object = new RedbackObject(this, objectConfig);
		Iterator<String> it = initialData.keySet().iterator();
		while(it.hasNext())
		{
			String attributeName = it.next();
			object.put(attributeName, initialData.getString(attributeName));
		}
		if(addRelated)
			object.loadRelated();
		object.save();
		return object;
	}
	
	public RedbackObject executeFunction(String objectName, String id, String function, JSONObject updateData, boolean addRelated) throws RedbackException
	{
		RedbackObject object = getObject(objectName, id, false);
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
	
	public String getID(String name) throws FunctionErrorException
	{
		String value = firebus.requestService(idGeneratorService, new Payload(name)).getString();
		return value;
	}

	
	protected JSONObject request(String service, JSONObject request) throws JSONException, FunctionErrorException
	{
		Payload reqPayload = new Payload(request.toString());
		Payload respPayload = firebus.requestService(configService, reqPayload);
		String respStr = respPayload.getString();
		JSONObject result = new JSONObject(respStr);
		return result;
	}
	
	public JSONObject requestData(String objectName, JSONObject filter) throws JSONException, FunctionErrorException
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

