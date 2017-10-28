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

import com.nic.firebus.Payload;
import com.nic.firebus.exceptions.FunctionErrorException;
import com.nic.firebus.information.ServiceInformation;
import com.nic.firebus.utils.JSONEntity;
import com.nic.firebus.utils.JSONException;
import com.nic.firebus.utils.JSONList;
import com.nic.firebus.utils.JSONLiteral;
import com.nic.firebus.utils.JSONObject;

public class ObjectServer extends RedbackService
{
	private Logger logger = Logger.getLogger("com.nic.redback");
	protected String dataService;
	protected String idGeneratorService;
	protected HashMap<String, ObjectConfig> objectConfigs;
	// ScriptEngine js;
	//protected Bindings jsBindings;



	public ObjectServer(JSONObject c)
	{
		super(c);
		configService = config.getString("configservice");
		dataService = config.getString("dataservice");
		idGeneratorService = config.getString("idgeneratorservice");
		objectConfigs = new HashMap<String, ObjectConfig>();
		//js = new ScriptEngineManager().getEngineByName("javascript");
		//jsBindings = js.getBindings(ScriptContext.ENGINE_SCOPE);
	}

	public Payload service(Payload payload) throws FunctionErrorException
	{
		Payload response = new Payload();
		try
		{
			JSONObject request = new JSONObject(payload.getString());
			String action = request.getString("action");
			String objectName = request.getString("object");
			JSONObject options = request.getObject("options");
			JSONObject responseData = null;
			boolean addValidation = false;
			boolean addRelated = false;

			if(options != null)
			{
				String addValidationStr = options.getString("addvalidation");
				String addRelatedStr = options.getString("addrelated");
				if(addValidationStr != null  &&  addValidationStr.equals("true"))
					addValidation = true;
				if(addRelatedStr != null  &&  addRelatedStr.equals("true"))
					addRelated = true;
			}
			
			if(action.equals("get"))
			{
				String uid = request.getString("uid");
				if(uid != null)
				{
					RedbackObject object = getObject(objectName, uid); 
					if(addRelated)
						addRelated(object);
					responseData = object.getJSON(addValidation, addRelated);
				}
				else
				{
					responseData = new JSONObject("{error:\"A 'get' action requires a 'uid' attribute\"}");
				}
			}
			else if(action.equals("list"))
			{
				JSONObject filter = request.getObject("filter");
				if(filter != null)
				{
					ArrayList<RedbackObject> objects = getObjectList(objectName, filter);
					if(addRelated)
						addRelated(objects);
					responseData = new JSONObject();
					JSONList list = new JSONList();
					for(int i = 0; i < objects.size(); i++)
						list.add(objects.get(i).getJSON(addValidation, addRelated));
					responseData.put("list", list);
				}
				else
				{
					responseData = new JSONObject("{error:\"A 'list' action requires a 'filter' attribute\"}");
				}
			}
			else if(action.equals("update"))
			{
				String uid = request.getString("uid");
				JSONObject data = request.getObject("data");
				if(uid != null  &&  data != null)
				{
					RedbackObject object = updateObject(objectName, uid, data);
					if(addRelated)
						addRelated(object);
					responseData = object.getJSON(addValidation, addRelated);
				}
				else
				{
					responseData = new JSONObject("{error:\"An 'update' action requires a 'uid' and a 'data' attribute\"}");
				}
			}
			else if(action.equals("create"))
			{
				RedbackObject object = createObject(objectName);
				if(addRelated)
					addRelated(object);
				responseData = object.getJSON(addValidation, addRelated);
			}

			response.setData(responseData.toString());
		}
		catch(Exception e)
		{
			logger.severe(e.getMessage());
			throw new FunctionErrorException(e.getMessage());
		}
		return response;
	}

	public ServiceInformation getServiceInformation()
	{
		// TODO Auto-generated method stub
		return null;
	}
	
	protected ObjectConfig getObjectConfig(String object) throws JSONException, FunctionErrorException
	{
		ObjectConfig objectConfig = objectConfigs.get(object);
		if(objectConfig == null)
		{
			JSONObject configList = request(configService, "{object:rbo_config,filter:{name:" + object + "}}");
			if(configList.getList("result").size() > 0)
			{
				objectConfig = new ObjectConfig(configList.getObject("result.0"));
				objectConfigs.put(object, objectConfig);
			}
		}
		return objectConfig;
	}
	
	/*protected JSONObject getAttributeConfig(String object, String attribute) throws JSONException, FunctionErrorException
	{
		JSONObject objectConfig = getObjectConfig(object);
		JSONList attributeList = objectConfig.getList("attributes");
		for(int i = 0; i < attributeList.size(); i++)
			if(attributeList.getObject(i).getString("name").equals(attribute))
				return attributeList.getObject(i);
		return null;
	}*/
	
	/*protected String evaluateExpression(String expression, JSONObject object)
	{
		String returnValue = null;
		if(expression.startsWith("{{")  &&  expression.endsWith("}}"))
		{
			expression = "returnValue = (" + expression.substring(2, expression.length() - 2) + ");";
			Iterator<String> it = object.getObject("data").keySet().iterator();
			while(it.hasNext())
			{	
				String key = it.next();
				jsBindings.put(key, object.getString("data." + key));
			}
			jsBindings.put("object", object);
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
	}*/
	
	/*protected JSONObject generateDBFilter(String objectName, JSONObject objectFilter) throws JSONException, FunctionErrorException
	{
		ObjectConfig objectConfig = getObjectConfig(objectName);
		JSONObject dbFilter = new JSONObject();

		JSONList anyFilterList = new JSONList();
		JSONEntity anyFilterDBValue = null;
		if(objectFilter.get("_any") != null)
			anyFilterDBValue =	generateDBAttributeFilterValue(objectFilter.get("_any"));

		if(objectFilter.get("uid") != null)
			dbFilter.put(objectConfig.getUIDDBKey(), generateDBAttributeFilterValue(objectFilter.get("uid")));
		
		Iterator<String> it = objectConfig.getAttributeNames().iterator();
		while(it.hasNext())
		{
			AttributeConfig attributeConfig = objectConfig.getAttributeConfig(it.next());
			String attrName = attributeConfig.getName();
			String attrDBKey = attributeConfig.getDBKey();
			JSONEntity attrFilter = objectFilter.get(attrName);
			if(attrFilter != null)
			{
				dbFilter.put(attrDBKey, generateDBAttributeFilterValue(attrFilter));
			}
			else if(anyFilterDBValue != null)
			{
				JSONObject orTerm = new JSONObject();
				orTerm.put(attrDBKey, anyFilterDBValue);
				anyFilterList.add(orTerm);
			}
		}
		if(anyFilterList.size() > 0)
			dbFilter.put("$or", anyFilterList);
		return dbFilter;
	}
	
	protected JSONEntity generateDBAttributeFilterValue(JSONEntity attrFilterValue) throws JSONException
	{
		JSONEntity dbAttributeFilterValue = null;
		if(attrFilterValue != null)
		{
			if(attrFilterValue instanceof JSONLiteral)
			{
				String filterValueStr = ((JSONLiteral)attrFilterValue).getString();
				if(filterValueStr.startsWith("*")  &&  filterValueStr.endsWith("*")  &&  filterValueStr.length() >= 2)
					dbAttributeFilterValue = new JSONObject("{$regex:\"" + filterValueStr.substring(1, filterValueStr.length() - 1) + "\"}");
				else
					dbAttributeFilterValue = attrFilterValue;
			}
			if(attrFilterValue instanceof JSONList)
			{
				JSONList attrValueList = (JSONList)attrFilterValue;
				dbAttributeFilterValue = new JSONObject();
				((JSONObject)dbAttributeFilterValue).put("$in", attrValueList);
			}
		}
		return dbAttributeFilterValue;
	}*/

	/*protected JSONObject convertDBDataToObject(String objectName, JSONObject dbData) throws JSONException, FunctionErrorException
	{
		JSONObject objectConfig = getObjectConfig(objectName);
		JSONObject object = new JSONObject();
		JSONObject data = new JSONObject();
		
		String uidKey = objectConfig.getString("uid");
		String uid = dbData.getString(uidKey);
		object.put("uid", uid);
		object.put("objectname", objectName);
		
		JSONList attributeList = objectConfig.getList("attributes");
		for(int j = 0; j < attributeList.size(); j++)
		{
			JSONObject attributeConfig = attributeList.getObject(j);
			String attrDBKey = attributeConfig.getString("key");
			String attrValue = dbData.getString(attrDBKey);
			String attrName = attributeConfig.getString("name");
			data.put(attrName, attrValue);
		}
		object.put("data", data);
		return object;
	}
	
	protected void addValidations(String objectName, JSONObject entity) throws JSONException, FunctionErrorException
	{
		JSONList objectList = null;
		if(entity.getList("list") != null)
			objectList = entity.getList("list");
		else
		{
			objectList = new JSONList();
			objectList.add(entity);
		}
		
		JSONObject objectConfig = getObjectConfig(objectName);
		JSONList attributesList = objectConfig.getList("attributes");
		for(int i = 0; i < objectList.size(); i++)
		{
			JSONObject object = objectList.getObject(i);
			JSONObject validaton = new JSONObject();
			for(int j = 0; j < attributesList.size(); j++)
			{
				JSONObject attributeConfig = attributesList.getObject(j);
				String attrName = attributeConfig.getString("name");
				String attrEditable = attributeConfig.getString("editable");
				JSONList attrLOV = attributeConfig.getList("listofvalues");
				JSONObject attrRelatedObject = attributeConfig.getObject("relatedobject");

				JSONObject attributeValidation = new JSONObject();
				
				if(attrEditable.equalsIgnoreCase("true") || attrEditable.equalsIgnoreCase("false"))
					attributeValidation.put("editable", attrEditable.toLowerCase());
				else if(attrEditable.startsWith("{{")  &&  attrEditable.endsWith("}}"))
					attributeValidation.put("editable", evaluateExpression(attrEditable, object));
				
				if(attrLOV != null)
					attributeValidation.put("listofvalues", attrLOV);
				if(attrRelatedObject != null)
					attributeValidation.put("relatedobject", attrRelatedObject);
				validaton.put(attrName, attributeValidation);
			}		
			object.put("validation", validaton);
		}
	}*/
	
	protected void addRelated(RedbackObject object) throws JSONException, FunctionErrorException
	{
		ArrayList<RedbackObject> objects = new ArrayList<RedbackObject>();
		objects.add(object);
		addRelated(objects);
	}
	
	protected void addRelated(ArrayList<RedbackObject> objects) throws JSONException, FunctionErrorException
	{
		ObjectConfig objectConfig = objects.get(0).getObjectConfig();
		Iterator<String> it = objectConfig.getAttributeNames().iterator();
		while(it.hasNext())
		{
			AttributeConfig attributeConfig = objectConfig.getAttributeConfig(it.next());
			String attributeName = attributeConfig.getName();
			String relatedObjectName = attributeConfig.getRelatedObjectName();
			String relatedObjectValueAttribute = attributeConfig.getRelatedObjectValueAttribute();
			if(relatedObjectName != null  &&  relatedObjectValueAttribute != null)
			{
				JSONList inList = new JSONList();
				for(int j = 0; j < objects.size(); j++)
				{
					RedbackObject object = objects.get(j);
					String linkValue = object.getString(attributeName);
					if(linkValue != null)
						inList.add(new JSONLiteral(linkValue));
				}
				JSONObject relatedObjectFilter = new JSONObject();
				relatedObjectFilter.put(relatedObjectValueAttribute, inList);
				ArrayList<RedbackObject> result = getObjectList(relatedObjectName, relatedObjectFilter);
				for(int k = 0; k < result.size(); k++)
				{
					RedbackObject resultObject = result.get(k);
					String resultObjectLinkValue = resultObject.getString(relatedObjectValueAttribute);
					for(int j = 0; j < objects.size(); j++)
					{
						RedbackObject object = objects.get(j);
						String linkValue = object.getString(attributeName);
						if(linkValue != null  &&  linkValue.equals(resultObjectLinkValue))
							object.put(attributeName, resultObject);
					}
				}
			}
		}
	}

	
	protected RedbackObject getObject(String objectName, String id) throws FunctionErrorException, JSONException
	{
		RedbackObject object = null;
		ObjectConfig objectConfig = getObjectConfig(objectName);
		JSONObject dbFilter = new JSONObject("{" + objectConfig.getUIDDBKey() + ":" + id +"}");
		JSONObject dbResult = request(dataService, "{object:" + objectConfig.getCollection() + ",filter:" + dbFilter + "}");
		JSONList dbResultList = dbResult.getList("result");
		if(dbResultList.size() > 0)
		{
			JSONObject dbData = dbResultList.getObject(0);
			object = new RedbackObject(objectConfig, dbData);
		}
		return object;
	}
	
	
	protected ArrayList<RedbackObject> getObjectList(String objectName, JSONObject filterData) throws FunctionErrorException, JSONException
	{
		ObjectConfig objectConfig = getObjectConfig(objectName);
		JSONObject dbFilter = objectConfig.generateDBFilter(filterData);
		JSONObject dbResult = request(dataService, "{object:" + objectConfig.getCollection() + ",filter:" + dbFilter + "}");
		JSONList dbResultList = dbResult.getList("result");
		
		ArrayList<RedbackObject> objectList = new ArrayList<RedbackObject>();
		for(int i = 0; i < dbResultList.size(); i++)
		{
			JSONObject dbData = dbResultList.getObject(i);
			RedbackObject object = new RedbackObject(objectConfig, dbData);
			objectList.add(object);
		}
		return objectList;
	}
	
	
	protected RedbackObject updateObject(String objectName, String id, JSONObject updateData) throws JSONException, FunctionErrorException
	{
		RedbackObject object = getObject(objectName, id);
		ObjectConfig objectConfig = getObjectConfig(objectName);
		JSONObject dbUpdateData = new JSONObject();
		dbUpdateData.put(object.getUID(), id);
		if(object != null)
		{
			boolean doUpdate = true;
			Iterator<String> it = objectConfig.getAttributeNames().iterator();
			while(it.hasNext())
			{
				AttributeConfig attributeConfig = objectConfig.getAttributeConfig(it.next());
				String attrKey = attributeConfig.getDBKey();
				String attrName = attributeConfig.getName();
				String oldValue = object.getString(attrName);
				String newValue = updateData.getString(attrName);
				if(oldValue != null  &&  newValue != null)
				{
					boolean doAttributeUpdate = true;
					if(oldValue.equals(newValue))
						doAttributeUpdate = false;
					if(doAttributeUpdate)
					{
						dbUpdateData.put(attrKey, newValue);
						object.put(attrName, newValue);
					}
				}
			}
			
			if(doUpdate)
				firebus.publish(dataService, new Payload("{object:" + objectName + ",data:" + dbUpdateData + "}"));
		}
		return object;
	}
	
	protected RedbackObject createObject(String objectName) throws JSONException, FunctionErrorException
	{
		RedbackObject object = null;
		JSONObject dbData = new JSONObject();
		ObjectConfig objectConfig = getObjectConfig(objectName);
		String uid = firebus.requestService(idGeneratorService, new Payload(objectConfig.getUIDGeneratorName())).getString();
		dbData.put(objectConfig.getUIDDBKey(), uid);
		
		Iterator<String> it = objectConfig.getAttributeNames().iterator();
		while(it.hasNext())
		{
			AttributeConfig attributeConfig = objectConfig.getAttributeConfig(it.next());
			String attrDBKey = attributeConfig.getDBKey();
			String value = "";
			String idGeneratorName = attributeConfig.getIdGeneratorName();
			if(idGeneratorName != null)
			{
				value = firebus.requestService(idGeneratorService, new Payload(idGeneratorName)).getString();
				dbData.put(attrDBKey, value);
			}
			String defaultValue = attributeConfig.getDefaultValue();
			if(defaultValue != null)
				value = defaultValue;
			dbData.put(attrDBKey, value);
		}
		
		firebus.publish(dataService, new Payload("{object:" + objectConfig.getCollection() + ",data:" + dbData + "}"));
		object = new RedbackObject(objectConfig, dbData);
		return object;
	}
	
}
