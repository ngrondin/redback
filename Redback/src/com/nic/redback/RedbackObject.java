package com.nic.redback;

import java.util.HashMap;
import java.util.Iterator;
import java.util.logging.Logger;

import javax.script.Bindings;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import com.nic.firebus.utils.JSONObject;

public class RedbackObject 
{
	private Logger logger = Logger.getLogger("com.nic.redback");
	protected ObjectConfig config;
	protected HashMap<String, String> data;
	protected HashMap<String, RedbackObject> related;
	protected ScriptEngine js;
	protected Bindings jsBindings;

	
	public RedbackObject(ObjectConfig cfg, JSONObject d)
	{
		config = cfg;
		data = new HashMap<String, String>();
		related = new HashMap<String, RedbackObject>();
		Iterator<String> it = config.getAttributeNames().iterator();
		while(it.hasNext())
		{
			String attrName = it.next();
			data.put(attrName, d.getString(attrName));
		}
		js = new ScriptEngineManager().getEngineByName("javascript");
		jsBindings = js.getBindings(ScriptContext.ENGINE_SCOPE);
	}
	
	public ObjectConfig getObjectConfig()
	{
		return config;
	}

	public String getUID()
	{
		return  data.get(config.getUIDDBKey());
	}
	
	public String getString(String name)
	{
		String dbKey = null;
		if(name.equals("uid"))
			dbKey = config.getUIDDBKey();
		else
			dbKey = config.getAttributeConfig(name).getDBKey();
		return data.get(dbKey);
	}
	
	public void put(String name, String value)
	{
		data.put(name, value);
	}

	public void put(String name, RedbackObject obj)
	{
		if(config.getAttributeConfig(name).hasRelatedObject())
		{
			String relatedObjectName = config.getAttributeConfig(name).getRelatedObjectName();
			if(obj.getObjectConfig().getName().equals(relatedObjectName))
			{
				String relatedObjectValueAttribute = config.getAttributeConfig(name).getRelatedObjectValueAttribute();
				String linkValue = obj.getString(relatedObjectValueAttribute);
				data.put(name, linkValue);
				related.put(name, obj);
			}			
		}
	}
	
	public JSONObject getJSON(boolean addValidation, boolean addRelated)
	{
		JSONObject object = new JSONObject();
		String uidKey = config.getUIDDBKey();
		String uid = data.get(uidKey);
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
			String attrDBKey = attributeConfig.getDBKey();
			String attrValue = data.get(attrDBKey);
			String attrEditable = attributeConfig.getEditableExpression();
			JSONObject attrRelatedObject = attributeConfig.getRelatedObject();

			JSONObject attributeValidation = new JSONObject();
			
			if(attrEditable.equalsIgnoreCase("true") || attrEditable.equalsIgnoreCase("false"))
				attributeValidation.put("editable", attrEditable.toLowerCase());
			else if(attrEditable.startsWith("{{")  &&  attrEditable.endsWith("}}"))
				attributeValidation.put("editable", evaluateExpression(attrEditable));
			
			/*if(attrLOV != null)
				attributeValidation.put("listofvalues", attrLOV);*/
			if(attrRelatedObject != null)
			{
				attributeValidation.put("relatedobject", attrRelatedObject);
				relatedNode.put(attrName, related.get(attrName).getJSON(false, false));
			}
			validatonNode.put(attrName, attributeValidation);

			dataNode.put(attrName, attrValue);
		}
		object.put("data", dataNode);
		
		if(addValidation)
			object.put("validation", validatonNode);
		return object;
	}
	
	protected String evaluateExpression(String expression)
	{
		String returnValue = null;
		if(expression.startsWith("{{")  &&  expression.endsWith("}}"))
		{
			expression = "returnValue = (" + expression.substring(2, expression.length() - 2) + ");";
			Iterator<String> it = config.getAttributeNames().iterator();
			while(it.hasNext())
			{	
				String key = it.next();
				jsBindings.put(key, getString(key));
			}
			jsBindings.put("object", this);
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
			else 
				returnValue = (String)returnObject;				
		}
		return returnValue;
	}
	
	

}
