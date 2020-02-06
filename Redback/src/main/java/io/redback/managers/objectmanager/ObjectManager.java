package io.redback.managers.objectmanager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;

import javax.script.ScriptException;

import io.firebus.Firebus;
import io.firebus.Payload;
import io.firebus.exceptions.FunctionErrorException;
import io.firebus.exceptions.FunctionTimeoutException;
import io.firebus.utils.DataEntity;
import io.firebus.utils.DataException;
import io.firebus.utils.DataList;
import io.firebus.utils.DataLiteral;
import io.firebus.utils.DataMap;
import io.redback.RedbackException;
import io.redback.security.Session;

public class ObjectManager
{
	private Logger logger = Logger.getLogger("io.redback");
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
					ArrayList<RedbackObject> result = listObjects(session, relatedObjectConfig.getObjectName(), relatedObjectFilter, null);
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
				DataMap dbFilter = new DataMap("{\"" + objectConfig.getUIDDBKey() + "\":\"" + id +"\"}");
				if(objectConfig.getDomainDBKey() != null  &&  !session.getUserProfile().hasAllDomains())
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
	
	public ArrayList<RedbackObject> listObjects(Session session, String objectName, DataMap filterData, String searchText) throws RedbackException
	{
		return listObjects(session, objectName, filterData, searchText, 0);
	}
	
	public ArrayList<RedbackObject> listObjects(Session session, String objectName, DataMap filterData, String searchText, int page) throws RedbackException
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
				DataMap dbFilter = generateDBFilter(session, objectConfig, objectFilter);
				if(objectConfig.getDomainDBKey() != null  &&  !session.getUserProfile().hasAllDomains())
					dbFilter.put(objectConfig.getDomainDBKey(), session.getUserProfile().getDBFilterDomainClause());
				DataMap dbResult = requestData(objectConfig.getCollection(), dbFilter, page);
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
	
	public ArrayList<RedbackObject> listObjects(Session session, String objectName, String uid, String attributeName, DataMap filterData, String searchText) throws RedbackException
	{
		return listObjects(session, objectName, uid, attributeName, filterData, searchText, 0);
	}
	
	public ArrayList<RedbackObject> listObjects(Session session, String objectName, String uid, String attributeName, DataMap filterData, String searchText, int page) throws RedbackException
	{
		RedbackObject object = getObject(session, objectName, uid);
		if(object != null)
			return object.getRelatedList(attributeName, filterData, searchText, page);
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
	
	public RedbackObject createObject(Session session, String objectName, String domain, DataMap initialData) throws RedbackException, ScriptException
	{
		ObjectConfig objectConfig = getObjectConfig(objectName);
		RedbackObject object = new RedbackObject(session, this, objectConfig, domain);
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
	
	
	public DataMap generateDBFilter(Session session, ObjectConfig objectConfig, DataMap objectFilter) throws DataException, FunctionErrorException, RedbackException
	{
		DataMap dbFilter = new DataMap();
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
				DataList list = objectFilter.getList(key);
				DataList dbList = new DataList();
				for(int i = 0; i < list.size(); i++)
				{
					dbList.add(generateDBFilter(session, objectConfig, list.getObject(i)));
				}
				dbFilter.put(key, dbList);
			}
			else if(key.contains(".")) 
			{
				String rootAttribute = key.substring(0, key.indexOf("."));
				String remainder = key.substring(key.indexOf(".") + 1);
				AttributeConfig attributeConfig = objectConfig.getAttributeConfig(rootAttribute);
				if(attributeConfig.hasRelatedObject())
				{
					DataList dbList = new DataList();
					RelatedObjectConfig roc = attributeConfig.getRelatedObjectConfig();
					ObjectConfig nextObjectConfig = getObjectConfig(roc.getObjectName());
					ArrayList<RedbackObject> list = listObjects(session, nextObjectConfig.getName(), new DataMap(remainder, objectFilter.get(key)), null);
					if(list.size() > 0) {
						for(int k = 0; k < list.size(); k++)
						{
							RedbackObject resultObject = list.get(k);
							Value resultObjectLinkValue = resultObject.get(roc.getLinkAttributeName());
							dbList.add(resultObjectLinkValue.getObject());
						}
						dbFilter.put(rootAttribute, new DataMap("$in", dbList));
					} else {
						dbFilter.put(rootAttribute, "");
					}
				}
			}
			else
			{
				String attributeDBKey = null; 
				AttributeConfig attributeConfig = objectConfig.getAttributeConfig(key);
				if(key.equals("uid"))
					attributeDBKey = objectConfig.getUIDDBKey();
				else if(attributeConfig != null)
					attributeDBKey = attributeConfig.getDBKey();
				
				if(attributeDBKey != null)
				{
					DataEntity objectFilterValue = objectFilter.get(key);
					DataEntity dbFilterValue = null;
					if(objectFilterValue instanceof DataMap)
					{
						dbFilterValue = generateDBFilter(session, objectConfig, (DataMap)objectFilterValue);
					}
					else if(objectFilterValue instanceof DataLiteral)
					{
						String objectFilterValueString = ((DataLiteral)objectFilterValue).getString();
						if(objectFilterValueString != null  &&  objectFilterValueString.startsWith("*")  &&  objectFilterValueString.endsWith("*")  &&  objectFilterValueString.length() >= 2)
							dbFilterValue =  new DataMap("{$regex:\"" + objectFilterValueString.substring(1, objectFilterValueString.length() - 1) + "\"}");
						else
							dbFilterValue = ((DataLiteral)objectFilterValue).getCopy();
					}
					dbFilter.put(attributeDBKey, dbFilterValue);
				}
			}
		}

		return dbFilter;
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
						ArrayList<RedbackObject> result = listObjects(session, relatedObejctName, relatedFilter, null);
						if(result.size() > 0)
						{
							DataMap orTerm = new DataMap();
							DataList inList = new DataList();
							for(int k = 0; k < result.size(); k++)
							{
								RedbackObject resultObject = result.get(k);
								Value resultObjectLinkValue = resultObject.get(roc.getLinkAttributeName());
								inList.add(resultObjectLinkValue.getObject());
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
		return requestData(objectName, filter, 0);
	}
	
	protected DataMap requestData(String objectName, DataMap filter, int page) throws DataException, FunctionErrorException, FunctionTimeoutException
	{
		DataMap request = new DataMap();
		request.put("object", objectName);
		request.put("filter", filter);
		request.put("page", page);
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

