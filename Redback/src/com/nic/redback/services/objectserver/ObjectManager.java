package com.nic.redback.services.objectserver;

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
import com.nic.redback.RedbackException;
import com.nic.redback.security.Session;

public class ObjectManager
{
	private Logger logger = Logger.getLogger("com.nic.redback");
	protected Firebus firebus;
	protected boolean cacheConfigs;
	protected String configServiceName;
	protected String dataServiceName;
	protected String idGeneratorServiceName;
	protected JSONObject globalVariables;
	protected HashMap<String, ObjectConfig> objectConfigs;
	protected HashMap<Long, HashMap<String, RedbackObject>> transactions;


	public ObjectManager(JSONObject config)
	{
		cacheConfigs = true;
		configServiceName = config.getString("configservice");
		dataServiceName = config.getString("dataservice");
		idGeneratorServiceName = config.getString("idgeneratorservice");
		globalVariables = config.getObject("globalvariables");
		if(config.containsKey("cacheconfigs") &&  config.getString("cacheconfigs").equalsIgnoreCase("false"))
			cacheConfigs = false;
		objectConfigs = new HashMap<String, ObjectConfig>();
		transactions = new HashMap<Long, HashMap<String, RedbackObject>>();
	}
	
	public void setFirebus(Firebus fb)
	{
		firebus = fb;
	}
	
	public Firebus getFirebus()
	{
		return firebus;
	}
	
	public JSONObject getGlobalVariables()
	{
		return globalVariables;
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
				JSONObject configList = request(configServiceName, new JSONObject("{object:rbo_config,filter:{name:" + object + "}}"));
				if(configList.getList("result").size() > 0)
				{
					objectConfig = new ObjectConfig(configList.getObject("result.0"));
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


	
	public void addRelatedBulk(Session session, ArrayList<RedbackObject> objects) throws RedbackException, ScriptException
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
					ArrayList<RedbackObject> result = getObjectList(session, relatedObjectConfig.getObjectName(), relatedObjectFilter);
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

	
	public RedbackObject getObject(Session session, String objectName, String id) throws RedbackException
	{
		RedbackObject object = getFromCurrentTransaction(objectName, id);
		if(object == null)
		{
			ObjectConfig objectConfig = getObjectConfig(objectName);
			try
			{
				JSONObject dbFilter = new JSONObject("{\"" + objectConfig.getUIDDBKey() + "\":\"" + id +"\"}");
				dbFilter.put("domain", session.getUserProfile().getDBFilterDomainClause());
				JSONObject dbResult = requestData(objectConfig.getCollection(), dbFilter);
				JSONList dbResultList = dbResult.getList("result");
				if(dbResultList.size() > 0)
				{
					JSONObject dbData = dbResultList.getObject(0);
					object = new RedbackObject(session, this, objectConfig, dbData);
					putInCurrentTransaction(object);
				}
			}
			catch(Exception e)
			{
				error( "Problem initiating object : " + e.getMessage(), e);
			}		
		}
		return object;
	}
	
	
	public ArrayList<RedbackObject> getObjectList(Session session, String objectName, JSONObject filterData) throws RedbackException
	{
		ArrayList<RedbackObject> objectList = new ArrayList<RedbackObject>();
		ObjectConfig objectConfig = getObjectConfig(objectName);
		try
		{
			JSONObject dbFilter = objectConfig.generateDBFilter(filterData);
			dbFilter.put("domain", session.getUserProfile().getDBFilterDomainClause());
			JSONObject dbResult = requestData(objectConfig.getCollection(), dbFilter);
			JSONList dbResultList = dbResult.getList("result");
			
			for(int i = 0; i < dbResultList.size(); i++)
			{
				JSONObject dbData = dbResultList.getObject(i);
				RedbackObject object = getFromCurrentTransaction(objectName, dbData.getString(objectConfig.getUIDDBKey()));
				if(object == null)
				{
					object = new RedbackObject(session, this, objectConfig, dbData);
					putInCurrentTransaction(object);
				}
				objectList.add(object);
			}
		}
		catch(Exception e)
		{
			logger.severe(e.getMessage());
			throw new RedbackException("Error getting object list", e);
		}
		return objectList;
	}
	
	public ArrayList<RedbackObject> getObjectList(Session session, String objectName, String uid, String attributeName, JSONObject filterData) throws RedbackException
	{
		RedbackObject object = getObject(session, objectName, uid);
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
			objectList = getObjectList(session, relatedObjectConfig.getObjectName(), relatedObjectListFilter);
		}
		return objectList;
	}
	
	public RedbackObject updateObject(Session session, String objectName, String id, JSONObject updateData) throws RedbackException, ScriptException
	{
		RedbackObject object = getObject(session, objectName, id);
		if(object != null)
		{
			Iterator<String> it = updateData.keySet().iterator();
			while(it.hasNext())
			{
				String attributeName = it.next();
				object.put(attributeName, new Value(updateData.get(attributeName)));
			}
		}
		return object;
	}
	
	public RedbackObject createObject(Session session, String objectName, JSONObject initialData) throws RedbackException, ScriptException
	{
		ObjectConfig objectConfig = getObjectConfig(objectName);
		RedbackObject object = new RedbackObject(session, this, objectConfig);
		putInCurrentTransaction(object);
		if(initialData != null)
		{
			Iterator<String> it = initialData.keySet().iterator();
			while(it.hasNext())
			{
				String attributeName = it.next();
				object.put(attributeName, new Value(initialData.get(attributeName)));
			}
		}
		return object;
	}
	
	public RedbackObject executeFunction(Session session, String objectName, String id, String function, JSONObject updateData) throws RedbackException, ScriptException
	{
		RedbackObject object = getObject(session, objectName, id);
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
			object.execute(function);
			object.save();
		}
		return object;
	}
	
	public Value getNewID(String name) throws FunctionErrorException, FunctionTimeoutException
	{
		Payload response = firebus.requestService(idGeneratorServiceName, new Payload(name)); 
		String value = response.getString();
		return new Value(value);
	}

	
	protected RedbackObject getFromCurrentTransaction(String objectName, String uid)
	{
		long txId = Thread.currentThread().getId();
		if(transactions.containsKey(txId))
		{
			return transactions.get(txId).get(objectName + uid);
		}
		else
		{
			return null;
		}
	}

	protected void putInCurrentTransaction(RedbackObject obj)
	{
		long txId = Thread.currentThread().getId();
		synchronized(transactions)
		{
			if(!transactions.containsKey(txId))
				transactions.put(txId, new HashMap<String, RedbackObject>());
		}
		transactions.get(txId).put(obj.getObjectConfig().getName() + obj.getUID().getString(), obj);
	}
	
	public void commitCurrentTransaction() throws ScriptException, RedbackException
	{
		long txId = Thread.currentThread().getId();
		if(transactions.containsKey(txId))
		{
			HashMap<String, RedbackObject> objects = transactions.get(txId);
			Iterator<String> it = objects.keySet().iterator();
			while(it.hasNext())
			{
				String key = it.next();
				RedbackObject object = objects.get(key);
				object.save();
			}
			synchronized(transactions)
			{
				transactions.remove(txId);
			}
		}		
	}
	
	protected JSONObject request(String service, JSONObject request) throws JSONException, FunctionErrorException, FunctionTimeoutException
	{
		Payload reqPayload = new Payload(request.toString());
		logger.info("Requesting firebus service : " + service + "  " + request.toString().replace("\r\n", "").replace("\t", ""));
		Payload respPayload = firebus.requestService(service, reqPayload);
		logger.info("Receiving firebus service respnse");
		String respStr = respPayload.getString();
		JSONObject result = new JSONObject(respStr);
		return result;
	}
	
	protected JSONObject requestData(String objectName, JSONObject filter) throws JSONException, FunctionErrorException, FunctionTimeoutException
	{
		JSONObject request = new JSONObject();
		request.put("object", objectName);
		request.put("filter", filter);
		return request(dataServiceName, request);
	}

	protected void publishData(String collection, JSONObject data)
	{
		logger.info("Publishing to firebus service : " + dataServiceName + "  " + data.toString().replace("\r\n", "").replace("\t", ""));
		firebus.publish(dataServiceName, new Payload("{object:" + collection + ",data:" + data + "}"));
	}

	protected void error(String msg) throws RedbackException
	{
		error(msg, null);
	}
	
	protected void error(String msg, Exception cause) throws RedbackException
	{
		logger.severe(msg);
		if(cause != null)
			throw new RedbackException(msg, cause);
		else
			throw new RedbackException(msg);
	}


}

