package io.redback.managers.objectmanager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;

import javax.script.Bindings;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
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
import io.redback.client.ConfigurationClient;
import io.redback.client.DataClient;
import io.redback.managers.objectmanagers.js.ObjectManagerJSWrapper;
import io.redback.managers.objectmanagers.js.RedbackAggregateJSWrapper;
import io.redback.managers.objectmanagers.js.RedbackObjectJSWrapper;
import io.redback.security.Session;
import io.redback.security.js.SessionRightsJSFunction;
import io.redback.security.js.UserProfileJSWrapper;
import io.redback.utils.js.FirebusJSWrapper;
import io.redback.utils.js.JSConverter;
import io.redback.utils.js.LoggerJSFunction;

public class ObjectManager
{
	private Logger logger = Logger.getLogger("io.redback");
	protected Firebus firebus;
	protected ScriptEngine jsEngine;
	protected boolean cacheConfigs;
	protected String configServiceName;
	protected String dataServiceName;
	protected String idGeneratorServiceName;
	protected String processServiceName;
	protected String signalConsumerName;
	protected DataMap globalVariables;
	protected HashMap<String, ObjectConfig> objectConfigs;
	protected HashMap<String, ScriptConfig> globalScripts;
	protected List<ScriptConfig> includeScripts;
	protected HashMap<Long, HashMap<String, RedbackObject>> transactions;
	protected DataClient dataClient;
	protected ConfigurationClient configClient;

	public ObjectManager(Firebus fb, DataMap config)
	{
		firebus = fb;
		cacheConfigs = true;
		jsEngine = new ScriptEngineManager().getEngineByName("graal.js");
		configServiceName = config.getString("configservice");
		dataServiceName = config.getString("dataservice");
		idGeneratorServiceName = config.getString("idgeneratorservice");
		processServiceName = config.getString("processservice");
		signalConsumerName = config.getString("signalconsumer");
		globalVariables = config.getObject("globalvariables");
		dataClient = new DataClient(firebus, dataServiceName);
		configClient = new ConfigurationClient(firebus, configServiceName);
		if(config.containsKey("cacheconfigs") &&  config.getString("cacheconfigs").equalsIgnoreCase("false"))
			cacheConfigs = false;
		objectConfigs = new HashMap<String, ObjectConfig>();
		globalScripts = new HashMap<String, ScriptConfig>();
		transactions = new HashMap<Long, HashMap<String, RedbackObject>>();
	}
	
	public Firebus getFirebus()
	{
		return firebus;
	}
	
	public ScriptEngine getScriptEngine()
	{
		return jsEngine;
	}
	
	public DataMap getGlobalVariables()
	{
		return globalVariables;
	}
	
	public void refreshAllConfigs()
	{
		objectConfigs.clear();
		globalScripts.clear();
		if(includeScripts != null)
			includeScripts.clear();
		includeScripts = null;
	}
	
	public Bindings createScriptContext(RedbackElement element) throws RedbackException
	{
		Bindings context = createScriptContext(element.getUserSession());
		if(element != null)
		{
			if(element instanceof RedbackObject)
			{
				RedbackObject rbObject = (RedbackObject)element;
				context.put("uid", rbObject.getUID().getString());
				context.put("self", new RedbackObjectJSWrapper(rbObject));
			}
			else if(element instanceof RedbackAggregate)
			{
				RedbackAggregate rbAggregate = (RedbackAggregate)element;
				context.put("self", new RedbackAggregateJSWrapper(rbAggregate));
			}
			Iterator<String> it = element.getAttributeNames().iterator();
			while(it.hasNext())
			{	
				String key = it.next();
				if(element.getObjectConfig().getAttributeConfig(key).getExpression() == null)
					context.put(key, JSConverter.toJS(element.get(key).getObject()));
			}
		}		
		return context;
	}
	
	public Bindings createScriptContext(Session session) throws RedbackException
	{
		Bindings context = jsEngine.createBindings();
		context.put("om", new ObjectManagerJSWrapper(this, session));
		context.put("userprofile", new UserProfileJSWrapper(session.getUserProfile()));
		context.put("firebus", new FirebusJSWrapper(firebus, session));
		context.put("global", JSConverter.toJS(getGlobalVariables()));
		context.put("log", new LoggerJSFunction());
		context.put("canRead", new SessionRightsJSFunction(session, "read"));
		context.put("canWrite", new SessionRightsJSFunction(session, "write"));
		context.put("canExecute", new SessionRightsJSFunction(session, "execute"));
		return context;
	}
	
	protected List<ScriptConfig> getIncludeScripts() throws RedbackException
	{
		if(includeScripts == null)
		{
			includeScripts = new ArrayList<ScriptConfig>();
			DataMap result = configClient.listConfigs("rbo", "include");
			DataList resultList = result.getList("result");
			for(int i = 0; i < resultList.size(); i++)
			{
				ScriptConfig is = new ScriptConfig(getScriptEngine(), resultList.getObject(i));
				includeScripts.add(is);
			}
		}
		return includeScripts;
	}

	protected ScriptConfig getGlobalScript(String name) throws RedbackException
	{
		ScriptConfig scriptConfig = globalScripts.get(name);
		if(scriptConfig == null)
		{
			try
			{
				if(includeScripts == null)
					getIncludeScripts();
				scriptConfig = new ScriptConfig(getScriptEngine(), configClient.getConfig("rbo", "script", name));
				if(cacheConfigs)
					globalScripts.put(name, scriptConfig);
			}
			catch(Exception e)
			{
				logger.severe(e.getMessage());
				throw new RedbackException("Exception getting global script config", e);
			}
		}
		return scriptConfig;
	}
	
	protected ObjectConfig getObjectConfig(String object) throws RedbackException
	{
		ObjectConfig objectConfig = objectConfigs.get(object);
		if(objectConfig == null)
		{
			try
			{
				if(includeScripts == null)
					getIncludeScripts();
				objectConfig = new ObjectConfig(this, configClient.getConfig("rbo", "object", object));
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


	
	public void addRelatedBulk(Session session, List<RedbackElement> elements) throws RedbackException, ScriptException
	{
		if(elements != null  && elements.size() > 0)
		{
			ObjectConfig objectConfig = elements.get(0).getObjectConfig();
			Iterator<String> it = objectConfig.getAttributeNames().iterator();
			while(it.hasNext())
			{
				AttributeConfig attributeConfig = objectConfig.getAttributeConfig(it.next());
				String attributeName = attributeConfig.getName();
				if(attributeConfig.hasRelatedObject())
				{
					RelatedObjectConfig relatedObjectConfig = attributeConfig.getRelatedObjectConfig();
					DataList orList = new DataList();
					for(int j = 0; j < elements.size(); j++)
					{
						RedbackElement element = elements.get(j);
						Value linkValue = element.get(attributeName);
						if(linkValue != null && !linkValue.isNull())
						{
							DataMap findFilter = element.getRelatedFindFilter(attributeName);
							if(!orList.contains(findFilter))
								orList.add(findFilter);
						}
					}
					if(orList.size() > 0)
					{
						DataMap relatedObjectFilter = new DataMap();
						relatedObjectFilter.put("$or", orList);
						ArrayList<RedbackObject> result = listObjects(session, relatedObjectConfig.getObjectName(), relatedObjectFilter, null, false);
						
						String relatedObjectLinkAttributeName = relatedObjectConfig.getLinkAttributeName();
						for(int j = 0; j < elements.size(); j++)
						{
							RedbackElement element = elements.get(j);
							Value linkValue = element.get(attributeName);
							if(linkValue != null && !linkValue.isNull())
							{
								RedbackObject relatedObject = null;
								for(int k = 0; k < result.size(); k++)
								{
									RedbackObject resultObject = result.get(k);
									Value resultObjectLinkValue = resultObject.get(relatedObjectLinkAttributeName);
									if(linkValue != null  &&  linkValue.equalsIgnoreCase(resultObjectLinkValue))
										relatedObject = resultObject;
								}
								if(relatedObject == null) // Because of a broken link in the DB
								{
									ObjectConfig zombieObjectConfig = getObjectConfig(relatedObjectConfig.getObjectName());
									String zombieDBKey = (relatedObjectLinkAttributeName.equals("uid") ? zombieObjectConfig.getUIDDBKey() : zombieObjectConfig.getAttributeConfig(relatedObjectLinkAttributeName).getDBKey());
									relatedObject = new RedbackObject(session, this, zombieObjectConfig, new DataMap(zombieDBKey, linkValue.getObject()));
								}
								element.put(attributeName, relatedObject);							
							}
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
				DataMap dbResult = dataClient.getData(objectConfig.getCollection(), dbFilter);
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
	
	public ArrayList<RedbackObject> listObjects(Session session, String objectName, DataMap filterData, String searchText, boolean addRelated) throws RedbackException
	{
		return listObjects(session, objectName, filterData, searchText, addRelated, 0);
	}
	
	public ArrayList<RedbackObject> listObjects(Session session, String objectName, DataMap filterData, String searchText, boolean addRelated, int page) throws RedbackException
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
					objectFilter.merge(generateSearchFilter(session, objectName, searchText.trim()));
				DataMap dbFilter = generateDBFilter(session, objectConfig, objectFilter);
				if(objectConfig.getDomainDBKey() != null  &&  !session.getUserProfile().hasAllDomains())
					dbFilter.put(objectConfig.getDomainDBKey(), session.getUserProfile().getDBFilterDomainClause());
				DataMap dbResult = dataClient.getData(objectConfig.getCollection(), dbFilter, page);
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
				
				if(addRelated)
					addRelatedBulk(session, (List<RedbackElement>)(List<?>)objectList);
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
	
	public ArrayList<RedbackObject> listRelatedObjects(Session session, String objectName, String uid, String attributeName, DataMap filterData, String searchText, boolean addRelated) throws RedbackException
	{
		return listRelatedObjects(session, objectName, uid, attributeName, filterData, searchText, addRelated, 0);
	}
	
	public ArrayList<RedbackObject> listRelatedObjects(Session session, String objectName, String uid, String attributeName, DataMap filterData, String searchText, boolean addRelated, int page) throws RedbackException
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
	
	public RedbackObject createObject(Session session, String objectName, String uid, String domain, DataMap initialData) throws RedbackException, ScriptException
	{
		ObjectConfig objectConfig = getObjectConfig(objectName);
		RedbackObject object = new RedbackObject(session, this, objectConfig, uid, domain);
		putInCurrentTransaction(object);
		if(initialData != null)
		{
			for(String attributeName: initialData.keySet())
			{
				boolean isFilter = false;
				Object value = initialData.get(attributeName);
				if(value instanceof DataMap)
					for(String key: ((DataMap)value).keySet())
						if(key.startsWith("$"))
							isFilter = true;
				if(!isFilter)
					object.put(attributeName, new Value(value));
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
	
	public void executeFunction(Session session, String function) throws RedbackException, ScriptException
	{
		ScriptConfig scriptCfg = this.getGlobalScript(function);
		if(scriptCfg != null)
		{
			if(session.getUserProfile().canExecute("rb.scripts." + function))
				scriptCfg.execute(this.createScriptContext(session));
			else
				throw new RedbackException("No rights to execute global function " + function);
		}
	}	
	
	public List<RedbackAggregate> aggregateObjects(Session session, String objectName, DataMap filter, DataList tuple, DataList metrics, boolean addRelated) throws RedbackException
	{
		List<RedbackAggregate> list = new ArrayList<RedbackAggregate>();
		ObjectConfig objectConfig = getObjectConfig(objectName);
		if(objectConfig != null)
		{
			try
			{
				DataMap objectFilter = new DataMap();
				if(filter != null)
					objectFilter.merge(filter);
				DataMap dbFilter = generateDBFilter(session, objectConfig, objectFilter);
				if(objectConfig.getDomainDBKey() != null  &&  !session.getUserProfile().hasAllDomains())
					dbFilter.put(objectConfig.getDomainDBKey(), session.getUserProfile().getDBFilterDomainClause());
				DataList dbTuple = new DataList();
				for(int i = 0; i < tuple.size(); i++) {
					if(tuple.get(i) instanceof DataMap) {
						DataMap tupleItem = (DataMap)tuple.getObject(i).getCopy();
						tupleItem.put("attribute", objectConfig.getAttributeConfig(tupleItem.getString("attribute")).getDBKey());
						dbTuple.add(tupleItem);
					} else {
						dbTuple.add(objectConfig.getAttributeConfig(tuple.getString(i)).getDBKey());
					}
				}
				DataList dbMetrics = new DataList();
				for(int i = 0; i < metrics.size(); i++)
				{
					DataMap metric = metrics.getObject(i);
					String function = metric.getString("function");
					if(function.equals("count") || function.equals("sum") || function.equals("max") || function.equals("min"))
					{
						DataMap dbMetric = new DataMap();
						dbMetric.put("function", function);
						if(!function.equals("count"))
						{
							String attribute = metric.getString("attribute");
							if(attribute != null)
								dbMetric.put("field", objectConfig.getAttributeConfig(attribute).getDBKey());
						}
						dbMetric.put("name", metric.getString("name"));
						dbMetrics.add(metric);
					}
				}
				
				DataMap dbResult = dataClient.aggregateData(objectConfig.getCollection(), dbFilter, dbTuple, dbMetrics);
				DataList dbResultList = dbResult.getList("result");
				
				for(int i = 0; i < dbResultList.size(); i++)
				{
					DataMap dbData = dbResultList.getObject(i);
					RedbackAggregate aggregate = new RedbackAggregate(session, this, objectConfig, dbData);
					list.add(aggregate);
				}
				
				if(addRelated)
					addRelatedBulk(session, (List<RedbackElement>)(List<?>)list);
			}
			catch(Exception e)
			{
				logger.severe(e.getMessage());
				throw new RedbackException("Error aggregating objects", e);
			}
		}
		else
		{
			error("No object config is available for '" + objectName + "'");	
		}
		return list;		
	}
	
	public Value getNewID(String name) throws FunctionErrorException, FunctionTimeoutException
	{
		Payload response = firebus.requestService(idGeneratorServiceName, new Payload(name)); 
		String value = response.getString();
		return new Value(value);
	}

	
	public void initiateCurrentTransaction() 
	{
		long txId = Thread.currentThread().getId();
		synchronized(transactions)
		{
			transactions.put(txId, new HashMap<String, RedbackObject>());
		}
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
					ArrayList<RedbackObject> list = listObjects(session, nextObjectConfig.getName(), new DataMap(remainder, objectFilter.get(key)), null, false);
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
				else if(key.equals("domain"))
					attributeDBKey = objectConfig.getDomainDBKey();
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
		orList.add(new DataMap("uid", "*" + searchText + "*"));
		Iterator<String> it = config.getAttributeNames().iterator();
		while(it.hasNext())
		{
			AttributeConfig attributeConfig = config.getAttributeConfig(it.next());
			if(attributeConfig.getDBKey() != null)
			{
				if(attributeConfig.canBeSearched())
				{
					DataMap orTerm = new DataMap();
					orTerm.put(attributeConfig.getName(), "*" + searchText + "*");
					orList.add(orTerm);
				}
				if(attributeConfig.hasRelatedObject())
				{
					RelatedObjectConfig roc = attributeConfig.getRelatedObjectConfig();
					String relatedObejctName = roc.getObjectName();
					ObjectConfig relatedConfig = getObjectConfig(relatedObejctName);
					if(relatedConfig != null)
					{
						DataList relatedOrList = new DataList();
						Iterator<String> it2 = relatedConfig.getAttributeNames().iterator();
						while(it2.hasNext())
						{
							AttributeConfig relatedAttributeConfig = relatedConfig.getAttributeConfig(it2.next());
							if(relatedAttributeConfig.getDBKey() != null  &&  !relatedAttributeConfig.hasRelatedObject() && relatedAttributeConfig.canBeSearched())
							{
								DataMap orTerm = new DataMap();
								orTerm.put(relatedAttributeConfig.getName(), "*" + searchText + "*");
								relatedOrList.add(orTerm);
							}
						}
						if(relatedOrList.size() > 0)
						{
							DataMap relatedFilter = new DataMap("$or", relatedOrList);
							ArrayList<RedbackObject> result = listObjects(session, relatedObejctName, relatedFilter, null, false);
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
		}
		if(orList.size() == 1)
			filter = orList.getObject(0);
		if(orList.size() > 1)
			filter.put("$or", orList);
		return filter;
	}
	
	protected void commitData(String collection, DataMap key, DataMap data) throws RedbackException
	{
		dataClient.putData(collection, key, data);
	}
	
	protected void signal(String signal)
	{
		if(signalConsumerName != null) 
		{
			Payload payload = new Payload(signal);
			logger.finest("Publishing signal : " + signal);
			firebus.publish(signalConsumerName, payload);
			logger.finest("Published signal : " + signal);
		}
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

