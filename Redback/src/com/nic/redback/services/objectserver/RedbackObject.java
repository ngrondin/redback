package com.nic.redback.services.objectserver;

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
import com.nic.redback.RedbackException;
import com.nic.redback.security.UserProfile;

public class RedbackObject 
{
	private Logger logger = Logger.getLogger("com.nic.redback");
	protected UserProfile userProfile;
	protected ObjectManager objectManager;
	protected ObjectConfig config;
	protected Value uid;
	protected Value domain;
	protected boolean canRead;
	protected boolean canWrite;
	protected boolean canExecute;
	protected HashMap<String, Value> data;
	protected HashMap<String, RedbackObject> related;
	protected ArrayList<String> updatedAttributes;
	protected boolean isNewObject;

	// Initiate existing object from pre-loaded data
	public RedbackObject(UserProfile up, ObjectManager om, ObjectConfig cfg, JSONObject d, boolean loadRelated) throws RedbackException, ScriptException
	{
		userProfile = up;
		objectManager = om;
		config = cfg;
		isNewObject = false;
		init();
		if(canRead)
		{
			setDataFromDBData(d);
			if(loadRelated)
				loadRelated();
			executeScriptsForEvent("onload");
		}
		else
		{
			error("User does not have the right to read object " + config.getName());
		}
	}
	
	// Initiate existing object with Id and retreive data from database
	public RedbackObject(UserProfile up, ObjectManager om, ObjectConfig cfg, String id, boolean loadRelated) throws RedbackException
	{
		userProfile = up;
		objectManager = om;
		config = cfg;
		isNewObject = false;
		init();
		if(canRead)
		{
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
				error( "Problem initiating object : " + e.getMessage(), e);
			}
		}
		else
		{
			error("User does not have the right to read object " + config.getName());
		}
	}
	
	// Initiate new object
	public RedbackObject(UserProfile up, ObjectManager om, ObjectConfig cfg) throws RedbackException
	{
		userProfile = up;
		objectManager = om;
		config = cfg;
		isNewObject = true;
		init();		
		if(canWrite)
		{
			try
			{
				if(config.getUIDGeneratorName() != null)
				{
					uid = objectManager.getID(config.getUIDGeneratorName());
					domain = new Value(userProfile.getAttribute("rb.defaultdomain"));
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
					error("No UID Generator has been configured for object " + config.getName() , null);
				}
			}
			catch(Exception e)
			{
				error("Problem initiating object " + config.getName(), e);
			}
		}
		else
		{
			error("User does not have the right to create object " + config.getName());
		}
		
	}
	
	protected void init()
	{
		canRead = userProfile.canRead("rb.objects." + config.getName());
		canWrite = userProfile.canWrite("rb.objects." + config.getName());
		canExecute = userProfile.canExecute("rb.objects." + config.getName());
		data = new HashMap<String, Value>();
		related = new HashMap<String, RedbackObject>();
		updatedAttributes = new ArrayList<String>();
	}
	
	protected void setDataFromDBData(JSONObject dbData)
	{
		uid = new Value(dbData.getString(config.getUIDDBKey()));
		domain = new Value(dbData.getString(config.getDomainDBKey()));
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
	
	public Value getDomain()
	{
		return domain;
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
		else if(name.equals("domain"))
		{
			return getDomain();
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
	
	public void put(String name, Value value) throws ScriptException, RedbackException
	{
		if(config.getAttributeConfig(name) != null)
		{
			Value currentValue = get(name);
			if(!currentValue.equals(value))
			{
				if(canWrite  &&  (isEditable(name) || isNewObject))
				{
					data.put(name, value);
					updatedAttributes.add(name);	
					executeAttributeScriptsForEvent(name, "onupdate");
				}
				else
				{
					error("User does not have the right to update object " + config.getName() + " or the attribute " + name);
				}
			}
		}
		else
		{
			error("This attribute '" + name + "' does not exist");
		}
	}

	public void put(String name, String value) throws ScriptException, RedbackException
	{
		put(name, new Value(value));
	}
	
	public void put(String name, RedbackObject relatedObject) throws ScriptException, RedbackException
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
	
	public void save() throws ScriptException, RedbackException
	{
		if(updatedAttributes.size() > 0  ||  isNewObject == true)
		{
			if(canWrite)
			{
				executeScriptsForEvent("onsave");
				JSONObject dbData = new JSONObject();
				dbData.put(config.getUIDDBKey(), getUID().getObject());
				if(isNewObject)
					dbData.put(config.getDomainDBKey(), domain.getObject());
				
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
				updatedAttributes.clear();
				objectManager.publishData(config.getCollection(), dbData);
				isNewObject = false;
			}
			else
			{
				error("User does not have the right to update object " + config.getName());
			}
		}
	}
	
	public void execute(String eventName) throws ScriptException, RedbackException
	{
		if(canExecute)
		{
			executeScriptsForEvent(eventName);
		}
		else
		{
			error("User does not have the right to execute functions in object " + config.getName());
		}
	}
	
	public JSONObject getJSON(boolean addValidation, boolean addRelated)
	{
		JSONObject object = new JSONObject();
		object.put("objectname", config.getName());
		object.put("uid", uid.getObject());
		object.put("domain", domain.getObject());
		
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
			expression = "var returnValue = (" + expression.substring(2, expression.length() - 2) + ");";
			Iterator<String> it = data.keySet().iterator();
			Bindings context = objectManager.jsEngine.createBindings();
			context.put("self", this);
			while(it.hasNext())
			{	
				String key = it.next();
				context.put(key, data.get(key).getObject());
			}
			try
			{
				objectManager.jsEngine.eval(expression, context);
			} 
			catch (ScriptException e)
			{
				logger.severe(e.getMessage());
			}
			returnValue = new Value(context.get("returnValue"));
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
		ArrayList<ScriptConfig> scripts = config.getScriptsForEvent(event);
		if(scripts != null)
			for(int i = 0; i < scripts.size(); i++)
				executeScript(scripts.get(i));
	}

	protected void executeAttributeScriptsForEvent(String attributeName, String event) throws ScriptException
	{
		ArrayList<ScriptConfig> scripts = config.getAttributeConfig(attributeName).getScriptsForEvent(event);
		if(scripts != null)
			for(int i = 0; i < scripts.size(); i++)
				executeScript(scripts.get(i));
	}
	
	protected void executeScript(ScriptConfig scriptConfig) throws ScriptException
	{
		Bindings context = objectManager.jsEngine.createBindings();
		context.put("self", this);
		context.put("om", objectManager);
		context.put("up", userProfile);
		Iterator<String> it = data.keySet().iterator();
		while(it.hasNext())
		{	
			String key = it.next();
			context.put(key, data.get(key));
		}
		try
		{
			scriptConfig.getScript().eval(context);
		} 
		catch (ScriptException e)
		{
			logger.severe(e.getMessage());
			throw e;
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

	public String toString()
	{
		return getJSON(false, false).toString();
	}

}
