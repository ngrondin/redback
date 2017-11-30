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

public class RedbackObject 
{
	private Logger logger = Logger.getLogger("com.nic.redback");
	protected ObjectManager objectManager;
	protected ObjectConfig config;
	protected String uid;
	protected HashMap<String, String> data;
	protected HashMap<String, RedbackObject> related;
	protected ArrayList<String> updatedAttributes;
	protected ScriptEngine js;
	protected Bindings jsBindings;

	
	public RedbackObject(ObjectManager om, ObjectConfig cfg, JSONObject d, boolean loadRelated) throws RedbackException, ScriptException
	{
		objectManager = om;
		config = cfg;
		init();
		setDataFromDBData(d);
		if(loadRelated)
			loadRelated();
		executeScriptsForEvent("onload");
	}
	
	public RedbackObject(ObjectManager om, ObjectConfig cfg, String i, boolean loadRelated) throws RedbackException
	{
		objectManager = om;
		config = cfg;
		init();
		try
		{
			JSONObject dbFilter = new JSONObject("{" + config.getUIDDBKey() + ":" + i +"}");
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
	
	public RedbackObject(ObjectManager om, ObjectConfig cfg) throws RedbackException
	{
		objectManager = om;
		config = cfg;
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
		data = new HashMap<String, String>();
		related = new HashMap<String, RedbackObject>();
		updatedAttributes = new ArrayList<String>();
		js = new ScriptEngineManager().getEngineByName("javascript");
		jsBindings = js.getBindings(ScriptContext.ENGINE_SCOPE);
		jsBindings.put("objectManager", objectManager);
	}
	
	protected void setDataFromDBData(JSONObject dbData)
	{
		uid = dbData.getString(config.getUIDDBKey());
		Iterator<String> it = config.getAttributeNames().iterator();
		while(it.hasNext())
		{
			AttributeConfig attributeConfig = config.getAttributeConfig(it.next());
			String dbKey = attributeConfig.getDBKey();
			if(dbKey != null)
				data.put(attributeConfig.getName(), dbData.getString(dbKey));
		}
	}
	
	
	public JSONObject getRelatedObjectListFilter(String attributeName)
	{
		JSONObject filter = null;
		RelatedObjectConfig roc = config.getAttributeConfig(attributeName).getRelatedObjectConfig();
		if(roc != null)
		{
			JSONObject relationship = roc.getRelationship();
			filter = new JSONObject();
			Iterator<String> it = relationship.keySet().iterator();
			while(it.hasNext())
			{
				String key = it.next();
				filter.put(key, evaluateExpression(relationship.getString(key)));
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
			String linkValue = getString(attributeName);
			filter.put(linkAttribute, linkValue);
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
				ArrayList<RedbackObject> resultList = objectManager.getObjectList(relatedObjectConfig.getObjectName(), getRelatedObjectFindFilter(attributeName), false);
				if(resultList.size() > 0)
					related.put(attributeName, resultList.get(0));
			}
		}		
	}
	
	public ObjectConfig getObjectConfig()
	{
		return config;
	}

	public String getUID()
	{
		return  uid;
	}
	
	public String getString(String name)
	{
		if(name.equals("uid"))
		{
			return uid;
		}
		else
		{
			AttributeConfig attributeConfig = config.getAttributeConfig(name);
			if(attributeConfig != null)
			{
				if(attributeConfig.getExpression() != null)
					return evaluateExpression(attributeConfig.getExpression());
				else if(data.containsKey(name))
					return data.get(name);
			}
		}
		return null;
	}
	
	public void put(String name, String value) throws ScriptException
	{
		if(config.getAttributeConfig(name) != null)
		{
			String currentValue = getString(name);
			if((currentValue != null  &&  value == null) || (currentValue == null  &&  value != null) || (currentValue!= null  && !currentValue.equals(value)))
			{
				data.put(name, value);
				updatedAttributes.add(name);	
				executeAttributeScriptsForEvent(name, "onupdate");
			}
		}
	}

	public void put(String name, RedbackObject relatedObject) throws ScriptException
	{
		if(config.getAttributeConfig(name).hasRelatedObject())
		{
			RelatedObjectConfig roc = config.getAttributeConfig(name).getRelatedObjectConfig();
			if(relatedObject.getObjectConfig().getName().equals(roc.getObjectName()))
			{
				String relatedObjectLinkAttribute = roc.getLinkAttributeName();
				String linkValue = relatedObject.getString(relatedObjectLinkAttribute);
				put(name, linkValue);
				related.put(name, relatedObject);
			}			
		}
	}
	
	public void save() throws ScriptException
	{
		executeScriptsForEvent("onsave");
		JSONObject dbData = new JSONObject();
		dbData.put(config.getUIDDBKey(), uid);
		for(int i = 0; i < updatedAttributes.size(); i++)
		{
			AttributeConfig attributeConfig = config.getAttributeConfig(updatedAttributes.get(i));
			String attributeName = attributeConfig.getName();
			String attributeDBKey = attributeConfig.getDBKey();
			if(attributeDBKey != null)
			{
				dbData.put(attributeDBKey, getString(attributeName));
			}
		}
		updatedAttributes.clear();
		objectManager.publishData(config.getCollection(), dbData);
	}
	
	public void execute(String eventName) throws ScriptException
	{
		executeScriptsForEvent(eventName);
	}
	
	public JSONObject getJSON(boolean addValidation, boolean addRelated)
	{
		JSONObject object = new JSONObject();
		object.put("uid", uid);
		object.put("objectname", config.getName());

		JSONObject dataNode = new JSONObject();
		JSONObject validatonNode = new JSONObject();
		JSONObject relatedNode = new JSONObject();

		Iterator<String> it = config.getAttributeNames().iterator();
		while(it.hasNext())
		{
			AttributeConfig attributeConfig = config.getAttributeConfig(it.next());
			String attrName = attributeConfig.getName();
			String attrValue = getString(attrName);
			String attrEditable = attributeConfig.getEditableExpression();

			JSONObject attributeValidation = new JSONObject();			
			attributeValidation.put("editable", evaluateExpression(attrEditable));
			
			/*if(attrLOV != null)
				attributeValidation.put("listofvalues", attrLOV);*/
			if(attributeConfig.hasRelatedObject())
			{
				attributeValidation.put("relatedobject", attributeConfig.getRelatedObjectConfig().getJSON());
				RedbackObject relatedObject = related.get(attrName);
				if(relatedObject != null)
					relatedNode.put(attrName, relatedObject.getJSON(false, false));
			}
			
			validatonNode.put(attrName, attributeValidation);
			dataNode.put(attrName, attrValue);
		}
		object.put("data", dataNode);
		
		if(addValidation)
			object.put("validation", validatonNode);
		if(addRelated)
			object.put("related", relatedNode);
		
		return object;
	}
	
	protected String evaluateExpression(String expression)
	{
		String returnValue = null;
		if(expression.startsWith("{{")  &&  expression.endsWith("}}"))
		{
			expression = "returnValue = (" + expression.substring(2, expression.length() - 2) + ");";
			Iterator<String> it = data.keySet().iterator();
			while(it.hasNext())
			{	
				String key = it.next();
				jsBindings.put(key, data.get(key));
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
			Object returnObject = jsBindings.get("returnValue");
			if(returnObject instanceof Boolean)
				returnValue = (Boolean)returnObject ? "true" : "false";
			else if(returnObject instanceof Integer)
				returnValue = String.valueOf((int)returnObject);
			else if(returnObject instanceof Double)
				returnValue = String.valueOf((double)returnObject);
			else
				returnValue = (String)returnObject;				
		}
		else
		{
			returnValue = expression;
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
