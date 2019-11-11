package com.nic.redback.managers.objectmanager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;

import javax.script.ScriptException;

import com.nic.firebus.Firebus;
import com.nic.firebus.Payload;
import com.nic.firebus.exceptions.FunctionErrorException;
import com.nic.firebus.exceptions.FunctionTimeoutException;
import com.nic.firebus.utils.DataException;
import com.nic.firebus.utils.DataList;
import com.nic.firebus.utils.DataMap;
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
	protected DataMap globalVariables;
	protected HashMap<String, ObjectConfig> objectConfigs;
	protected HashMap<Long, HashMap<String, RedbackObject>> transactions;


	public ObjectManager(Firebus fb, DataMap config)
	{
		firebus = fb;
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
	
	public Firebus getFirebus()
	{
		return firebus;
	}
	
	public DataMap getGlobalVariables()
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
				objectConfig = new ObjectConfig(requestConfig("rbo", "object", object));
				if(cacheConfigs)
					objectConfigs.put(object, objectConfig);
			}
			catch(Exception e)
			{
				logger.severe(e.getMessage());
				throw new RedbackException("Exception getting object config", e);
			}
		}
		return objectConfig;
	}


	
	public void addRelatedBulk(Session session, List<RedbackObject> objects) throws RedbackException, ScriptException
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
					DataList orList = new DataList();
					for(int j = 0; j < objects.size(); j++)
					{
						RedbackObject object = objects.get(j);
						if(object.getString(attributeName) != null)
							orList.add(object.getRelatedFindFilter(attributeName));
					}
					DataMap relatedObjectFilter = new DataMap();
					relatedObjectFilter.put("$or", orList);
					ArrayList<RedbackObject> result = getObjectList(session, relatedObjectConfig.getObjectName(), relatedObjectFilter, null);
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
	
	
	protected DataMap generateSearchFilter(Session session, String objectName, String searchText) throws RedbackException
	{
		DataMap filter = new DataMap();
		DataList orList = new DataList();
		ObjectConfig config = getObjectConfig(objectName);
		Iterator<String> it = config.getAttributeNames().iterator();
		while(it.hasNext())
		{
			AttributeConfig attributeConfig = config.getAttributeConfig(it.next());
			if(attributeConfig.getDBKey() != null)
			{
				if(!attributeConfig.hasRelatedObject())
				{
					DataMap orTerm = new DataMap();
					orTerm.put(attributeConfig.getName(), "*" + searchText + "*");
					orList.add(orTerm);
				}
				else
				{
					RelatedObjectConfig roc = attributeConfig.getRelatedObjectConfig();
					String relatedObejctName = roc.getObjectName();
					ObjectConfig relatedConfig = getObjectConfig(relatedObejctName);
					if(relatedConfig != null)
					{
						DataMap relatedFilter = new DataMap();
						DataList relatedOrList = new DataList();
						Iterator<String> it2 = relatedConfig.getAttributeNames().iterator();
						while(it2.hasNext())
						{
							AttributeConfig relatedAttributeConfig = relatedConfig.getAttributeConfig(it2.next());
							if(relatedAttributeConfig.getDBKey() != null  &&  !relatedAttributeConfig.hasRelatedObject())
							{
								DataMap orTerm = new DataMap();
								orTerm.put(relatedAttributeConfig.getName(), "*" + searchText + "*");
								relatedOrList.add(orTerm);
							}
						}
						relatedFilter.put("$or", relatedOrList);
						ArrayList<RedbackObject> result = getObjectList(session, relatedObejctName, relatedFilter, null);
						if(result.size() > 0)
						{
							DataMap orTerm = new DataMap();
							DataList inList = new DataList();
							for(int k = 0; k < result.size(); k++)
							{
								RedbackObject resultObject = result.get(k);
								Value resultObjectLinkValue = resultObject.get(roc.getLinkAttributeName());
								inList.add(resultObjectLinkValue.getObject());
								//DataMap orTerm = new DataMap();
								//orTerm.put(attributeConfig.getName(), resultObjectLinkValue.getObject());
								//orList.add(orTerm);
							}
							orTerm.put(attributeConfig.getName(), new DataMap("$in", inList));
							orList.add(orTerm);
						}
					}
				}
			}
		}
		filter.put("$or", orList);
		return filter;
	}


	
	public RedbackObject getObject(Session session, String objectName, String id) throws RedbackException
	{
		RedbackObject object = getFromCurrentTransaction(objectName, id);
		if(object == null)
		{
			ObjectConfig objectConfig = getObjectConfig(objectName);
			try
			{
				DataMap dbFilter = new DataMap("{\"" + objectConfig.getUIDDBKey() + "\":\"" + id +"\"}");
				dbFilter.put(objectConfig.getDomainDBKey(), session.getUserProfile().getDBFilterDomainClause());
				DataMap dbResult = requestData(objectConfig.getCollection(), dbFilter);
				DataList dbResultList = dbResult.getList("result");
				if(dbResultList.size() > 0)
				{
					DataMap dbData = dbResultList.getObject(0);
					object = new RedbackObject(session, this, objectConfig, dbData);
					putInCurrentTransaction(object);
				}
			}
			catch(Exception e)
			{
				error( "Problem initiating object : " + e.getMessage(), e);
			}		
		}
		if(object == null)
			throw new RedbackException("No " + objectName + " object exists with uid " + id);
		return object;
	}
	
	
	public ArrayList<RedbackObject> getObjectList(Session session, String objectName, DataMap filterData, String searchText) throws RedbackException
	{
		ArrayList<RedbackObject> objectList = new ArrayList<RedbackObject>();
		ObjectConfig objectConfig = getObjectConfig(objectName);
		if(objectConfig != null)
		{
			try
			{
				DataMap objectFilter = new DataMap();
				if(filterData != null)
					objectFilter.merge(filterData);
				if(searchText != null)
					objectFilter.merge(generateSearchFilter(session, objectName, searchText));
				DataMap dbFilter = objectConfig.generateDBFilter(objectFilter);
				if(objectConfig.getDomainDBKey() != null)
					dbFilter.put(objectConfig.getDomainDBKey(), session.getUserProfile().getDBFilterDomainClause());
				DataMap dbResult = requestData(objectConfig.getCollection(), dbFilter);
				DataList dbResultList = dbResult.getList("result");
				
				for(int i = 0; i < dbResultList.size(); i++)
				{
					DataMap dbData = dbResultList.getObject(i);
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
		}
		else
		{
			error("No object config is available for '" + objectName + "'");	
		}
		return objectList;
	}
	
	public ArrayList<RedbackObject> getObjectList(Session session, String objectName, String uid, String attributeName, DataMap filterData, String searchText) throws RedbackException
	{
		RedbackObject object = getObject(session, objectName, uid);
		if(object != null)
			return object.getRelatedList(attributeName, filterData, searchText);
		else
			return new ArrayList<RedbackObject>();
	}
	
	public RedbackObject updateObject(Session session, String objectName, String id, DataMap updateData) throws RedbackException, ScriptException
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
	
	public RedbackObject createObject(Session session, String objectName, DataMap initialData) throws RedbackException, ScriptException
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
				if(!(initialData.get(attributeName) instanceof DataMap))  //THis is to avoid complex base filters setting initial data
					object.put(attributeName, new Value(initialData.get(attributeName)));
			}
			logger.fine("Created object " + object.getObjectConfig().getName() + ":" + object.getUID().getString());
		}
		return object;
	}
	
	public RedbackObject executeFunction(Session session, String objectName, String id, String function, DataMap updateData) throws RedbackException, ScriptException
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
	
	protected DataMap request(String service, DataMap request) throws DataException, FunctionErrorException, FunctionTimeoutException
	{
		Payload reqPayload = new Payload(request.toString());
		logger.finest("Requesting firebus service : " + service + "  " + request.toString().replace("\r\n", "").replace("\t", ""));
		Payload respPayload = firebus.requestService(service, reqPayload, 10000);
		logger.finest("Receiving firebus service respnse");
		String respStr = respPayload.getString();
		DataMap result = new DataMap(respStr);
		return result;
	}
	
	protected DataMap requestConfig(String service, String category, String name) throws DataException, FunctionErrorException, FunctionTimeoutException
	{
		DataMap request = new DataMap();
		request.put("action", "get");
		request.put("service", service);
		request.put("category", category);
		request.put("name", name);
		return request(configServiceName, request);
	}

	protected DataMap requestData(String objectName, DataMap filter) throws DataException, FunctionErrorException, FunctionTimeoutException
	{
		DataMap request = new DataMap();
		request.put("object", objectName);
		request.put("filter", filter);
		return request(dataServiceName, request);
	}

	protected void publishData(String collection, DataMap key, DataMap data)
	{
		logger.finest("Publishing to firebus service : " + dataServiceName + "  " + data.toString().replace("\r\n", "").replace("\t", ""));
		firebus.publish(dataServiceName, new Payload("{object:" + collection + ", key: " + key + ", data:" + data + "}"));
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

