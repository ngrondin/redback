package io.redback.managers.objectmanager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

import javax.script.ScriptException;

import io.firebus.data.DataMap;
import io.redback.exceptions.RedbackException;
import io.redback.managers.objectmanager.js.ObjectManagerJSWrapper;
import io.redback.managers.objectmanager.js.RedbackAggregateJSWrapper;
import io.redback.security.Session;
import io.redback.security.js.SessionRightsJSFunction;
import io.redback.security.js.UserProfileJSWrapper;
import io.redback.utils.js.FirebusJSWrapper;
import io.redback.utils.js.LoggerJSFunction;

public class RedbackAggregate extends RedbackElement
{
	private Logger logger = Logger.getLogger("io.redback");
	protected boolean canRead;
	protected HashMap<String, Value> dimensions;
	protected HashMap<String, Value> metrics;
	protected HashMap<String, RedbackObject> related;
	protected ArrayList<String> updatedAttributes;
	protected boolean isNewObject;

	// Initiate existing object from pre-loaded data
	protected RedbackAggregate(Session s, ObjectManager om, ObjectConfig cfg, DataMap dbData) throws RedbackException, ScriptException
	{
		init(s, om, cfg);		
		isNewObject = false;
		if(canRead)
		{
			DataMap data = (DataMap)dbData.getCopy();
			Iterator<String> it = config.getAttributeNames().iterator();
			while(it.hasNext())
			{
				AttributeConfig attributeConfig = config.getAttributeConfig(it.next());
				String dbKey = attributeConfig.getDBKey();
				if(dbKey != null && data.containsKey(dbKey))
				{
					Value val = new Value(data.get(dbKey));
					dimensions.put(attributeConfig.getName(), val);
				}
				data.remove(dbKey);
			}
			it = data.keySet().iterator();
			while(it.hasNext()) 
			{
				String dbKey = it.next();
				Value val = new Value(data.get(dbKey));
				metrics.put(dbKey, val);
			}	
			updateScriptContext();
		}
		else
		{
			error("User does not have the right to read object " + config.getName());
		}
	}
		
	protected void init(Session s, ObjectManager om, ObjectConfig cfg)
	{
		session = s;
		objectManager = om;
		config = cfg;
		canRead = session.getUserProfile().canRead("rb.objects." + config.getName());
		dimensions = new HashMap<String, Value>();
		metrics = new HashMap<String, Value>();
		related = new HashMap<String, RedbackObject>();
		updatedAttributes = new ArrayList<String>();
		scriptContext = new HashMap<String, Object>();
		scriptContext.put("self", new RedbackAggregateJSWrapper(this));
		scriptContext.put("om", new ObjectManagerJSWrapper(objectManager, session));
		scriptContext.put("userprofile", new UserProfileJSWrapper(session.getUserProfile()));
		scriptContext.put("firebus", new FirebusJSWrapper(objectManager.getFirebus(), session));
		//scriptContext.put("global", objectManager.getGlobalVariables());
		scriptContext.put("log", new LoggerJSFunction());
		scriptContext.put("canRead", new SessionRightsJSFunction(session, "read"));
		scriptContext.put("canWrite", new SessionRightsJSFunction(session, "write"));
		scriptContext.put("canExecute", new SessionRightsJSFunction(session, "execute"));		
	}
	
	protected void updateScriptContext() throws RedbackException 
	{
		Iterator<String> it = getAttributeNames().iterator();
		while(it.hasNext())
		{	
			String key = it.next();
			if(getObjectConfig().getAttributeConfig(key).getExpression() == null)
				scriptContext.put(key, get(key).getObject());
		}
	}	
	
	public Set<String> getAttributeNames() 
	{
		Set<String> set = new HashSet<String>();
		Iterator<String> it = config.getAttributeNames().iterator();
		while(it.hasNext()) 
		{
			String attr = it.next();
			if(get(attr) != null)
				set.add(attr);
		}
		return set;			
	}
	
	public Value get(String name) 
	{
		AttributeConfig attributeConfig = config.getAttributeConfig(name);
		if(attributeConfig != null && dimensions.containsKey(name))
			return dimensions.get(name);
		else
			return null;
	}
	
	public String getString(String name) throws RedbackException
	{
		Value val = get(name);
		if(val != null)
			return val.getString();
		else
			return null;
	}
	
	public Value getMetric(String name) throws RedbackException
	{
		if(metrics.containsKey(name))
			return metrics.get(name);
		else 
			return new Value(null);
	}
	
	public Number getMetricNumber(String name) throws RedbackException
	{
		return getMetric(name).getNumber();
	}	
	
	public RedbackObject getRelated(String name)
	{
		AttributeConfig attributeConfig = config.getAttributeConfig(name);
		 if(attributeConfig != null)
		 {
			if(attributeConfig.hasRelatedObject()  &&  dimensions.get(name) != null  &&   !dimensions.get(name).isNull())
			{
				if(related.get(name) == null)
				{
					try
					{
						RelatedObjectConfig roc = attributeConfig.getRelatedObjectConfig();
						if(roc.getLinkAttributeName().equals("uid"))
						{
							related.put(name, objectManager.getObject(session, roc.getObjectName(), get(name).getString()));
						}
						else
						{
							List<RedbackObject> resultList = objectManager.listObjects(session, roc.getObjectName(), getRelatedFindFilter(name), null, null, false, 0, 1000);
							if(resultList.size() > 0)
								related.put(name, resultList.get(0));
						}
					}
					catch(RedbackException e)
					{
						//TODO: Handle somehow!
					}
				}
				return related.get(name);
			}
		}
		 return null;
	}
	
	public void setRelated(String name, RedbackObject relatedObject) throws RedbackException
	{
		if(config.getAttributeConfig(name).hasRelatedObject())
		{
			RelatedObjectConfig roc = config.getAttributeConfig(name).getRelatedObjectConfig();
			if(relatedObject.getObjectConfig().getName().equals(roc.getObjectName()))
				related.put(name, relatedObject);
		}
	}
	

	public DataMap getRelatedFindFilter(String attributeName) throws RedbackException
	{
		DataMap filter = null;
		RelatedObjectConfig roc = config.getAttributeConfig(attributeName).getRelatedObjectConfig();
		if(roc != null)
		{
			String linkAttribute = roc.getLinkAttributeName();
			if(linkAttribute.equals("uid")) 
			{
				filter = new DataMap("uid", get(attributeName).getObject());
			}
			else
			{
				filter = getRelatedListFilter(attributeName);
				if(filter == null)
					filter = new DataMap();
				filter.put(roc.getLinkAttributeName(), get(attributeName).getObject());
			}
		}
		return filter;
	}
	
	protected DataMap getRelatedListFilter(String attributeName) throws RedbackException
	{
		DataMap filter = null;
		RelatedObjectConfig roc = config.getAttributeConfig(attributeName).getRelatedObjectConfig();
		if(roc != null)
		{
			filter = roc.generateFilter(this);
		}
		return filter;
	}
	

	
	public DataMap getDataMap(boolean addRelated) throws RedbackException
	{
		DataMap object = new DataMap();
		object.put("objectname", config.getName());
		
		DataMap dimensionsNode = new DataMap();
		DataMap metricsNode = new DataMap();
		DataMap relatedNode = new DataMap();

		Iterator<String> it = config.getAttributeNames().iterator();
		while(it.hasNext())
		{
			AttributeConfig attributeConfig = config.getAttributeConfig(it.next());
			String attrName = attributeConfig.getName();
			Value attrValue = get(attrName);
			if(attrValue != null && !attrValue.isNull()) 
			{
				if(addRelated  &&  attributeConfig.hasRelatedObject())
				{
					RedbackObject relatedObject = getRelated(attrName);
					if(relatedObject != null)
						relatedNode.put(attrName, relatedObject.getDataMap(false, false));
				}
				dimensionsNode.put(attrName, attrValue.getObject());				
			}
		}
		
		it = metrics.keySet().iterator();
		while(it.hasNext()) 
		{
			String key = it.next();
			Value metricValue = this.getMetric(key);
			metricsNode.put(key, metricValue.getNumber());
		}
		
		object.put("dimensions", dimensionsNode);
		object.put("metrics", metricsNode);
		
		if(addRelated)
			object.put("related", relatedNode);
		
		return object;
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

	public String toString()
	{
		try
		{
			return getDataMap(false).toString();
		}
		catch(RedbackException e)
		{
			return e.getMessage();
		}
	}

}
