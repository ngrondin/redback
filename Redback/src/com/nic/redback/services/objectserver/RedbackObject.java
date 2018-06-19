package com.nic.redback.services.objectserver;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.logging.Logger;

import javax.script.Bindings;
import javax.script.CompiledScript;
import javax.script.ScriptEngine;
import javax.script.ScriptException;

import jdk.nashorn.api.scripting.JSObject;

import com.nic.firebus.utils.FirebusDataUtil;
import com.nic.firebus.utils.JSONObject;
import com.nic.redback.RedbackException;
import com.nic.redback.security.Session;
import com.nic.redback.security.js.UserProfileJSWrapper;
import com.nic.redback.services.objectserver.js.ObjectManagerJSWrapper;
import com.nic.redback.services.objectserver.js.RedbackObjectJSWrapper;
import com.nic.redback.utils.FirebusJSWrapper;
import com.nic.redback.utils.LoggerJSFunction;

public class RedbackObject 
{
	private Logger logger = Logger.getLogger("com.nic.redback");
	protected Session session;
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
	protected RedbackObject(Session s, ObjectManager om, ObjectConfig cfg, JSONObject d) throws RedbackException, ScriptException
	{
		init(s, om, cfg);		
		isNewObject = false;
		if(canRead)
		{
			setDataFromDBData(d);
			executeScriptsForEvent("onload");
		}
		else
		{
			error("User does not have the right to read object " + config.getName());
		}
	}
	
	
	// Initiate new object
	protected RedbackObject(Session s, ObjectManager om, ObjectConfig cfg) throws RedbackException
	{
		init(s, om, cfg);		
		isNewObject = true;
		if(canWrite)
		{
			try
			{
				if(config.getUIDGeneratorName() != null)
				{
					uid = objectManager.getNewID(config.getUIDGeneratorName());
					domain = new Value(session.getUserProfile().getAttribute("rb.defaultdomain"));
					Iterator<String> it = config.getAttributeNames().iterator();
					while(it.hasNext())
					{
						AttributeConfig attributeConfig = config.getAttributeConfig(it.next());
						String attributeName = attributeConfig.getName();
						String idGeneratorName = attributeConfig.getIdGeneratorName();
						String defaultValue = attributeConfig.getDefaultValue();
						if(idGeneratorName != null)
							put(attributeName, objectManager.getNewID(idGeneratorName));
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
	
	protected void init(Session s, ObjectManager om, ObjectConfig cfg)
	{
		session = s;
		objectManager = om;
		config = cfg;
		canRead = session.getUserProfile().canRead("rb.objects." + config.getName());
		canWrite = session.getUserProfile().canWrite("rb.objects." + config.getName());
		canExecute = session.getUserProfile().canExecute("rb.objects." + config.getName());
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
	

	public ObjectConfig getObjectConfig()
	{
		return config;
	}
	
	public Session getUserSession()
	{
		return session;
	}

	public Value getUID()
	{
		return  uid;
	}
	
	public Value getDomain()
	{
		return domain;
	}
	
	public Value get(String name) throws RedbackException
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
			Expression expression = attributeConfig.getExpression();
			if(expression != null)
				return expression.eval(this);
			else if(data.containsKey(name))
				return data.get(name);
			else
				return new Value(null);
		}		
		return null;
	}
	
	public String getString(String name) throws RedbackException
	{
		return get(name).getString();
	}
	
	public RedbackObject getRelated(String name)
	{
		AttributeConfig attributeConfig = config.getAttributeConfig(name);
		 if(attributeConfig != null)
		 {
			if(attributeConfig.hasRelatedObject()  &&  data.get(name) != null  &&   !data.get(name).isNull())
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
							ArrayList<RedbackObject> resultList = objectManager.getObjectList(session, roc.getObjectName(), getRelatedFindFilter(name));
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
	
	public ArrayList<RedbackObject> getRelatedList(String attributeName, JSONObject additionalFilter) throws RedbackException
	{
		ArrayList<RedbackObject> relatedObjectList = null;
		RelatedObjectConfig roc = config.getAttributeConfig(attributeName).getRelatedObjectConfig();
		if(roc != null)
		{
			if(roc.getListScript() != null)
			{
				CompiledScript script = roc.getListScript();
				Bindings context = executeScript(script);
				JSObject jsList = (JSObject)context.get("list");
				int count = (Integer)jsList.getMember("length");
				relatedObjectList = new ArrayList<RedbackObject>();
				for(int i = 0; i < count; i++)
				{
					JSObject jso = (JSObject)jsList.getSlot(i);
					RedbackObject rbo = objectManager.getObject(session, (String)jso.getMember("objectname"), (String)jso.getMember("uid"));
					relatedObjectList.add(rbo);
				}
			}
			else
			{
				JSONObject relatedObjectListFilter = getRelatedListFilter(attributeName);
				if(relatedObjectListFilter == null)
					relatedObjectListFilter = new JSONObject();
				Iterator<String> it = additionalFilter.keySet().iterator();
				while(it.hasNext())
				{
					String key = it.next();
					relatedObjectListFilter.put(key, additionalFilter.get(key));
				}
				relatedObjectList = objectManager.getObjectList(session, roc.getObjectName(), relatedObjectListFilter);
			}
		}
		return relatedObjectList;		
	}
	
	protected JSONObject getRelatedListFilter(String attributeName) throws RedbackException
	{
		JSONObject filter = null;
		RelatedObjectConfig roc = config.getAttributeConfig(attributeName).getRelatedObjectConfig();
		if(roc != null)
		{
			FilterConfig fc = roc.getListFilterConfig();
			if(fc != null)
				filter = fc.generateFilter(this);
		}
		return filter;
	}
	
	public JSONObject getRelatedFindFilter(String attributeName) throws RedbackException
	{
		JSONObject filter = null;
		RelatedObjectConfig roc = config.getAttributeConfig(attributeName).getRelatedObjectConfig();
		if(roc != null)
		{
			filter = getRelatedListFilter(attributeName);
			if(filter == null)
				filter = new JSONObject();
			filter.put(roc.getLinkAttributeName(), get(attributeName).getObject());
		}
		return filter;
	}
	
	public void put(String name, Value value) throws RedbackException
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

	public void put(String name, String value) throws RedbackException
	{
		put(name, new Value(value));
	}
	
	public void put(String name, RedbackObject relatedObject) throws RedbackException
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
	
	public void clear(String name) throws ScriptException, RedbackException
	{
		put(name, new Value(null));
	}
	
	public boolean isEditable(String name) throws RedbackException
	{
		return config.getAttributeConfig(name).getEditableExpression().eval(this).getBoolean();
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
				executeScriptsForEvent("aftersave");
				if(isNewObject)
				{
					executeScriptsForEvent("aftercreate");
					isNewObject = false;
				}
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
	
	public JSONObject getJSON(boolean addValidation, boolean addRelated) throws RedbackException
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
			attributeValidation.put("updatescript", attributeConfig.getScriptForEvent("onupdate") != null);
			if(attributeConfig.hasRelatedObject())
			{
				JSONObject relatedObjectValidation = new JSONObject();
				relatedObjectValidation.put("object",  attributeConfig.getRelatedObjectConfig().getObjectName());
				relatedObjectValidation.put("link",  attributeConfig.getRelatedObjectConfig().getLinkAttributeName());
				attributeValidation.put("related", relatedObjectValidation);
			}
			validatonNode.put(attrName, attributeValidation);
			
			if(addRelated  &&  attributeConfig.hasRelatedObject())
			{
				RedbackObject relatedObject = getRelated(attrName);
				if(relatedObject != null)
					relatedNode.put(attrName, relatedObject.getJSON(false, false));
			}
			
			dataNode.put(attrName, attrValue.getObject());
		}
		object.put("data", dataNode);
		
		if(addValidation)
			object.put("validation", validatonNode);
		if(addRelated)
			object.put("related", relatedNode);
		
		return object;
	}
	

	protected void executeScriptsForEvent(String event) throws RedbackException
	{
		CompiledScript script  = config.getScriptForEvent(event);
		if(script != null)
				executeScript(script);
	}

	protected void executeAttributeScriptsForEvent(String attributeName, String event) throws RedbackException
	{
		CompiledScript script  = config.getAttributeConfig(attributeName).getScriptForEvent(event);
		if(script != null)
				executeScript(script);
	}
	
	protected Bindings executeScript(CompiledScript script) throws RedbackException
	{
		String fileName = (String)script.getEngine().get(ScriptEngine.FILENAME);
		logger.info("Start executing script : " + fileName);
		Bindings context = script.getEngine().createBindings();
		context.put("self", new RedbackObjectJSWrapper(this));
		context.put("om", new ObjectManagerJSWrapper(objectManager, session));
		context.put("userprofile", new UserProfileJSWrapper(session.getUserProfile()));
		context.put("firebus", new FirebusJSWrapper(objectManager.getFirebus(), session.getSessionId().toString()));
		context.put("global", FirebusDataUtil.convertDataObjectToJSObject(objectManager.getGlobalVariables()));
		context.put("log", new LoggerJSFunction());
		try
		{
			script.eval(context);
		} 
		catch (ScriptException e)
		{
			error("Problem occurred executing a script", e);
		}		
		catch(NullPointerException e)
		{
			error("Null pointer exception in script " + fileName, e);
		}
		catch(RuntimeException e)
		{
			error("Problem occurred executing a script", e);
		}
		logger.info("Finish executing script : " + fileName);
		return context;
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
			return getJSON(false, false).toString();
		}
		catch(RedbackException e)
		{
			return e.getMessage();
		}
	}

}
