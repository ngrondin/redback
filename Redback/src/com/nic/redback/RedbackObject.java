package com.nic.redback;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.logging.Logger;

import javax.script.Bindings;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import com.nic.firebus.utils.JSONList;
import com.nic.firebus.utils.JSONObject;
import com.nic.redback.security.UserProfile;

public class RedbackObject 
{
	private Logger logger = Logger.getLogger("com.nic.redback");
	protected UserProfile userProfile;
	protected ObjectManager objectManager;
	protected ObjectConfig config;
	protected Value uid;
	protected HashMap<String, Value> data;
	protected HashMap<String, RedbackObject> related;
	protected ArrayList<String> updatedAttributes;
	protected ScriptEngine js;
	protected Bindings jsBindings;
	protected boolean isNewObject;

	
	public RedbackObject(UserProfile up, ObjectManager om, ObjectConfig cfg, JSONObject d, boolean loadRelated) throws RedbackException, ScriptException
	{
		userProfile = up;
		objectManager = om;
		config = cfg;
		isNewObject = false;
		init();
		setDataFromDBData(d);
		if(loadRelated)
			loadRelated();
		executeScriptsForEvent("onload");
	}
	
	public RedbackObject(UserProfile up, ObjectManager om, ObjectConfig cfg, String id, boolean loadRelated) throws RedbackException
	{
		userProfile = up;
		objectManager = om;
		config = cfg;
		isNewObject = false;
		init();
		try
		{
			JSONObject dbFilter = new JSONObject("{\"" + config.getUIDDBKey() + "\":\"" + id +"\"}");
			dbFilter.put("domain", userProfile.getDBFilterDomainClause());
			JSONObject dbResult = objectManager.requestData(config.getCollection(), dbFilter);
			JSONList dbResultList = dbResult.getList("result");
			if(dbResultList.size() > 0)
			{
				JSONObject dbData = dbResultList.getObject(0);
				setDataFromDBData(dbData);
				if(loadRelated)
					loadRelated();
				executeScriptsForEvent("onload");
			}
		}
		catch(Exception e)
		{
			String msg = "Problem initiating object : " + e.getMessage();
			logger.severe(msg);
			throw new RedbackException(msg, e);
		}
	}
	
	public RedbackObject(UserProfile up, ObjectManager om, ObjectConfig cfg) throws RedbackException
	{
		userProfile = up;
		objectManager = om;
		config = cfg;
		isNewObject = true;
		init();		
		try
		{
			if(config.getUIDGeneratorName() != null)
			{
				uid = objectManager.getID(config.getUIDGeneratorName());
				Iterator<String> it = config.getAttributeNames().iterator();
				while(it.hasNext())
				{
					AttributeConfig attributeConfig = config.getAttributeConfig(it.next());
					String attributeName = attributeConfig.getName();
					String idGeneratorName = attributeConfig.getIdGeneratorName();
					String defaultValue = attributeConfig.getDefaultValue();
					if(idGeneratorName != null)
						put(attributeName, objectManager.getID(idGeneratorName));
					else if(defaultValue != null)
						put(attributeName, defaultValue);
				}
				executeScriptsForEvent("oncreate");
			}
			else
			{
				String msg = "No UID Generator has been configured for object " + config.getName();
				logger.severe(msg);
				throw new RedbackException(msg);
			}
		}
		catch(Exception e)
		{
			String msg = "Problem initiating object : " + e.getMessage();
			logger.severe(msg);
			throw new RedbackException(msg, e);
		}
	}
	
	protected void init()
	{
		data = new HashMap<String, Value>();
		related = new HashMap<String, RedbackObject>();
		updatedAttributes = new ArrayList<String>();
		js = new ScriptEngineManager().getEngineByName("javascript");
		jsBindings = js.getBindings(ScriptContext.ENGINE_SCOPE);
		jsBindings.put("objectManager", objectManager);
	}
	
	protected void setDataFromDBData(JSONObject dbData)
	{
		uid = new Value(dbData.getString(config.getUIDDBKey()));
		Iterator<String> it = config.getAttributeNames().iterator();
		while(it.hasNext())
		{
			AttributeConfig attributeConfig = config.getAttributeConfig(it.next());
			String dbKey = attributeConfig.getDBKey();
			if(dbKey != null)
			{
				Value val = new Value(dbData.get(dbKey));
				data.put(attributeConfig.getName(), val);
			}
		}
	}
	
	
	public JSONObject getRelatedObjectListFilter(String attributeName)
	{
		JSONObject filter = null;
		RelatedObjectConfig roc = config.getAttributeConfig(attributeName).getRelatedObjectConfig();
		if(roc != null)
		{
			filter = new JSONObject();
			JSONObject listFilter = roc.getListFilter();
			if(listFilter != null)
			{
				Iterator<String> it = listFilter.keySet().iterator();
				while(it.hasNext())
				{
					String key = it.next();
					filter.put(key, evaluateExpression(listFilter.getString(key)).getObject());
				}
			}
		}
		return filter;
	}
	
	public JSONObject getRelatedObjectFindFilter(String attributeName)
	{
		JSONObject filter = getRelatedObjectListFilter(attributeName);
		if(filter != null)
		{
			RelatedObjectConfig roc = config.getAttributeConfig(attributeName).getRelatedObjectConfig();
			String linkAttribute = roc.getLinkAttributeName();
			Value linkValue = get(attributeName);
			filter.put(linkAttribute, linkValue.getObject());
		}
		return filter;
	}
	

	public void loadRelated() throws RedbackException
	{
		Iterator<String> it = config.getAttributeNames().iterator();
		while(it.hasNext())
		{
			AttributeConfig attributeConfig = config.getAttributeConfig(it.next());
			String attributeName = attributeConfig.getName();
			if(attributeConfig.hasRelatedObject()  &&  getString(attributeName) != null)
			{
				RelatedObjectConfig relatedObjectConfig = attributeConfig.getRelatedObjectConfig();
				String relatedObjectName = relatedObjectConfig.getObjectName();
				JSONObject relatedObjectFindFilter = getRelatedObjectFindFilter(attributeName);
				ArrayList<RedbackObject> resultList = objectManager.getObjectList(userProfile, relatedObjectName, relatedObjectFindFilter, false);
				if(resultList.size() > 0)
					related.put(attributeName, resultList.get(0));
			}
		}		
	}
	
	public ObjectConfig getObjectConfig()
	{
		return config;
	}

	public Value getUID()
	{
		return  uid;
	}
	
	public String getString(String name)
	{
		return get(name).getString();
	}
	
	public Value get(String name)
	{
		AttributeConfig attributeConfig = config.getAttributeConfig(name);
		if(name.equals("uid"))
		{
			return getUID();
		}
		else if(attributeConfig != null)
		{
			String expression = attributeConfig.getExpression();
			if(expression != null)
				return evaluateExpression(expression);
			else if(data.containsKey(name))
				return data.get(name);
			else
				return new Value(null);
		}		
		return null;
	}
	
	public void put(String name, Value value) throws ScriptException
	{
		if(config.getAttributeConfig(name) != null)
		{
			if(isEditable(name) || isNewObject)
			{
				Value currentValue = get(name);
				if(!currentValue.equals(value))
				{
					data.put(name, value);
					updatedAttributes.add(name);	
					executeAttributeScriptsForEvent(name, "onupdate");
				}
			}
		}
	}

	public void put(String name, String value) throws ScriptException
	{
		put(name, new Value(value));
	}
	
	public void put(String name, RedbackObject relatedObject) throws ScriptException
	{
		if(config.getAttributeConfig(name).hasRelatedObject())
		{
			RelatedObjectConfig roc = config.getAttributeConfig(name).getRelatedObjectConfig();
			if(relatedObject.getObjectConfig().getName().equals(roc.getObjectName()))
			{
				String relatedObjectLinkAttribute = roc.getLinkAttributeName();
				Value linkValue = relatedObject.get(relatedObjectLinkAttribute);
				put(name, linkValue);
				related.put(name, relatedObject);
			}			
		}
	}
	
	public boolean isEditable(String name)
	{
		return evaluateExpression(config.getAttributeConfig(name).getEditableExpression()).getBoolean();
	}
	
	public void save() throws ScriptException
	{
		if(updatedAttributes.size() > 0  ||  isNewObject == true)
		{
			executeScriptsForEvent("onsave");
			JSONObject dbData = new JSONObject();
			dbData.put(config.getUIDDBKey(), getUID().getObject());
			for(int i = 0; i < updatedAttributes.size(); i++)
			{
				AttributeConfig attributeConfig = config.getAttributeConfig(updatedAttributes.get(i));
				String attributeName = attributeConfig.getName();
				String attributeDBKey = attributeConfig.getDBKey();
				if(attributeDBKey != null)
				{
					dbData.put(attributeDBKey, get(attributeName).getObject());
				}
			}
			if(isNewObject)
				dbData.put("domain", userProfile.getDefaultDomain());
			updatedAttributes.clear();
			objectManager.publishData(config.getCollection(), dbData);
			isNewObject = false;
		}
	}
	
	public void execute(String eventName) throws ScriptException
	{
		executeScriptsForEvent(eventName);
	}
	
	public JSONObject getJSON(boolean addValidation, boolean addRelated)
	{
		JSONObject object = new JSONObject();
		object.put("uid", uid.getObject());
		object.put("objectname", config.getName());

		JSONObject dataNode = new JSONObject();
		JSONObject validatonNode = new JSONObject();
		JSONObject relatedNode = new JSONObject();

		Iterator<String> it = config.getAttributeNames().iterator();
		while(it.hasNext())
		{
			AttributeConfig attributeConfig = config.getAttributeConfig(it.next());
			String attrName = attributeConfig.getName();
			Value attrValue = get(attrName);

			JSONObject attributeValidation = new JSONObject();			
			attributeValidation.put("editable", isEditable(attrName));
			
			if(attributeConfig.hasRelatedObject())
			{
				attributeValidation.put("relatedobject", attributeConfig.getRelatedObjectConfig().getJSON());
				RedbackObject relatedObject = related.get(attrName);
				if(relatedObject != null)
					relatedNode.put(attrName, relatedObject.getJSON(false, false));
			}
			
			validatonNode.put(attrName, attributeValidation);
			dataNode.put(attrName, attrValue.getObject());
		}
		object.put("data", dataNode);
		
		if(addValidation)
			object.put("validation", validatonNode);
		if(addRelated)
			object.put("related", relatedNode);
		
		return object;
	}
	
	protected Value evaluateExpression(String expression)
	{
		Value returnValue = null;
		if(expression.startsWith("{{")  &&  expression.endsWith("}}"))
		{
			expression = "returnValue = (" + expression.substring(2, expression.length() - 2) + ");";
			Iterator<String> it = data.keySet().iterator();
			while(it.hasNext())
			{	
				String key = it.next();
				jsBindings.put(key, data.get(key).getObject());
			}
			jsBindings.put("self", this);
			try
			{
				js.eval(expression);
			} 
			catch (ScriptException e)
			{
				logger.severe(e.getMessage());
			}
			returnValue = new Value(jsBindings.get("returnValue"));
		}
		else
		{
			if(expression.matches("[-+]?\\d*\\.?\\d+"))
				returnValue = new Value(Double.parseDouble(expression));
			else if(expression.equalsIgnoreCase("true") ||  expression.equalsIgnoreCase("false"))
				returnValue = new Value(expression.equalsIgnoreCase("true") ? true : false);
			else
				returnValue = new Value(expression);
		}
		return returnValue;
	}
	
	protected void executeScriptsForEvent(String event) throws ScriptException
	{
		ArrayList<Script> scripts = config.getScriptsForEvent(event);
		if(scripts != null)
			for(int i = 0; i < scripts.size(); i++)
				executeScript(scripts.get(i));
	}

	protected void executeAttributeScriptsForEvent(String attributeName, String event) throws ScriptException
	{
		ArrayList<Script> scripts = config.getAttributeConfig(attributeName).getScriptsForEvent(event);
		if(scripts != null)
			for(int i = 0; i < scripts.size(); i++)
				executeScript(scripts.get(i));
	}
	
	protected void executeScript(Script script) throws ScriptException
	{
		Iterator<String> it = data.keySet().iterator();
		while(it.hasNext())
		{	
			String key = it.next();
			jsBindings.put(key, data.get(key));
		}
		jsBindings.put("self", this);
		jsBindings.put("om", objectManager);
		try
		{
			js.put(ScriptEngine.FILENAME, script.getObjectName() + (script.getAttributeName() != null ? "." + script.getAttributeName() : "") + "!" + script.getEventName());
			js.eval(script.getSource());
		} 
		catch (ScriptException e)
		{
			logger.severe(e.getMessage());
			throw e;
		}		
	}

	public String toString()
	{
		return getJSON(false, false).toString();
	}

}
