package com.nic.redback;

import java.util.HashMap;
import java.util.logging.Logger;

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
	protected String configService;
	protected String dataService;
	protected HashMap<String, JSONObject> objectConfigs;

	public ObjectServer(JSONObject c)
	{
		super(c);
		configService = config.getString("configservice");
		dataService = config.getString("dataservice");
		objectConfigs = new HashMap<String, JSONObject>();
	}

	public Payload service(Payload payload) throws FunctionErrorException
	{
		Payload response = new Payload();
		try
		{
			JSONObject request = new JSONObject(payload.getString());
			String action = request.getString("action");
			String object = request.getString("object");
			JSONObject options = request.getObject("options");
			JSONObject responseData = null;
			
			if(action.equals("get"))
			{
				String id = request.getString("id");
				if(id != null)
					responseData = getObject(object, id, options);
				else
					responseData = new JSONObject("{error:\"A 'get' action requires an 'id' attribute\"}");
			}
			if(action.equals("list"))
			{
				JSONObject filter = request.getObject("filter");
				if(filter != null)
					responseData = getObjectList(object, filter, options);
				else
					responseData = new JSONObject("{error:\"A 'list' action requires a 'filter' attribute\"}");
			}
			if(action.equals("update"))
			{
				String id = request.getString("id");
				JSONObject data = request.getObject("data");
				if(id != null  &&  data != null)
					responseData = updateObject(object, id, data, options);
				else
					responseData = new JSONObject("{error:\"An 'update' action requires an 'id' and a 'data' attribute\"}");
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
	
	protected JSONObject request(String service, String request) throws JSONException, FunctionErrorException
	{
		Payload reqPayload = new Payload(request);
		Payload respPayload = firebus.requestService(configService, reqPayload);
		String respStr = respPayload.getString();
		JSONObject result = new JSONObject(respStr);
		return result;
	}

	protected JSONObject getObjectConfig(String object) throws JSONException, FunctionErrorException
	{
		JSONObject objectConfig = objectConfigs.get(object);
		if(objectConfig == null)
		{
			JSONObject configList = request(configService, "{object:rbo_config,filter:{name:" + object + "}}");
			if(configList.getList("result").size() > 0)
			{
				objectConfig = configList.getObject("result.0");
				objectConfigs.put(object, objectConfig);
			}
		}
		return objectConfig;
	}
	
	protected JSONObject generateDBFilter(String objectName, JSONObject objectFilter) throws JSONException, FunctionErrorException
	{
		JSONObject objectConfig = getObjectConfig(objectName);
		JSONObject dbFilter = new JSONObject();

		JSONList anyFilterList = new JSONList();
		JSONEntity anyFilterDBValue = null;
		if(objectFilter.get("_any") != null)
			anyFilterDBValue =	generateDBAttributeFilterValue(objectFilter.get("_any"));

		if(objectFilter.get("id") != null)
			dbFilter.put(objectConfig.getString("uid"), generateDBAttributeFilterValue(objectFilter.get("id")));
		
		JSONList attributes = objectConfig.getList("attributes");
		for(int i = 0; i < attributes.size(); i++)
		{
			JSONObject attribute = attributes.getObject(i);
			String attrName = attribute.getString("name");
			String attrDBKey = attribute.getString("key");
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
	}

	protected JSONObject getObject(String objectName, String id, JSONObject options) throws FunctionErrorException, JSONException
	{
		JSONObject object = null;
		JSONObject objectConfig = getObjectConfig(objectName);
		String dbCollectionName = objectConfig.getString("collection");
		JSONObject dbFilter = new JSONObject("{" + objectConfig.getString("uid") + ":" + id +"}");
		JSONObject dbResult = request(dataService, "{object:" + dbCollectionName + ",filter:" + dbFilter + "}");
		JSONList dbResultList = dbResult.getList("result");
		if(dbResultList.size() > 0)
		{
			JSONObject dbObject = dbResultList.getObject(0);
			object = processDBObject(objectName, dbObject, options);
			
			if(options != null  &&  options.get("addrelated") != null  &&  options.getString("addrelated").equals("true"))
				addRelatedValues(objectName, object);
		}
		return object;
	}
	
	protected JSONObject getObjectList(String objectName, JSONObject filterData, JSONObject options) throws FunctionErrorException, JSONException
	{
		JSONObject objectConfig = getObjectConfig(objectName);
		String dbCollectionName = objectConfig.getString("collection");
		JSONObject dbFilter = generateDBFilter(objectName, filterData);
		JSONObject dbResult = request(dataService, "{object:" + dbCollectionName + ",filter:" + dbFilter + "}");
		JSONList dbResultList = dbResult.getList("result");
		
		JSONObject response = new JSONObject();
		JSONList objectList = new JSONList();
		response.put("list", objectList);
		for(int i = 0; i < dbResultList.size(); i++)
		{
			JSONObject dbObject = dbResultList.getObject(i);
			JSONObject object = processDBObject(objectName, dbObject, options);
			objectList.add(object);
		}
		
		if(options != null  &&  options.get("addrelated") != null  &&  options.getString("addrelated").equals("true"))
				addRelatedValues(objectName, objectList);
		
		return response;
	}
	
	protected JSONObject processDBObject(String objectName, JSONObject dbObject, JSONObject options) throws JSONException, FunctionErrorException
	{
		boolean addValidation = true;
		JSONObject objectConfig = getObjectConfig(objectName);
		JSONObject object = new JSONObject();
		JSONObject data = new JSONObject();
		JSONObject validaton = new JSONObject();
		
		if(options != null  &&  options.get("addvalidation") != null  &&  options.getString("addvalidation").equals("false"))
			addValidation = false;
		
		String uidKey = objectConfig.getString("uid");
		String uid = dbObject.getString(uidKey);
		object.put("id", uid);
		object.put("objectname", objectName);
		
		JSONList attributeList = objectConfig.getList("attributes");
		for(int j = 0; j < attributeList.size(); j++)
		{
			JSONObject attributeConfig = attributeList.getObject(j);
			String attrDBKey = attributeConfig.getString("key");
			String attrValue = dbObject.getString(attrDBKey);
			String attrName = attributeConfig.getString("name");
			String attrEditable = attributeConfig.getString("editable");
			JSONList attrLOV = attributeConfig.getList("listofvalues");
			JSONObject attrRelatedObject = attributeConfig.getObject("relatedobject");
			
			data.put(attrName, attrValue);

			JSONObject attributeControl = new JSONObject();
			attributeControl.put("editable", attrEditable);
			if(attrLOV != null)
				attributeControl.put("listofvalues", attrLOV);
			if(attrRelatedObject != null)
				attributeControl.put("relatedobject", attrRelatedObject);
			validaton.put(attrName, attributeControl);
		}
		object.put("data", data);
		if(addValidation)
			object.put("validation", validaton);
		return object;
	}
	
	protected void addRelatedValues(String objectName, JSONEntity entity) throws JSONException, FunctionErrorException
	{
		JSONList objectList = null;
		if(entity instanceof JSONList)
			objectList = (JSONList)entity;
		else
		{
			objectList = new JSONList();
			objectList.add(entity);
		}
		
		JSONObject objectConfig = getObjectConfig(objectName);
		JSONList attributesList = objectConfig.getList("attributes");
		for(int i = 0; i < attributesList.size(); i++)
		{
			JSONObject attributeConfig = attributesList.getObject(i);
			String attributeName = attributeConfig.getString("name");
			String relatedObjectName = attributeConfig.getString("relatedobject.name");
			if(relatedObjectName != null)
			{
				JSONObject relatedObjectConfig = getObjectConfig(relatedObjectName);
				String relatedObjectUIDKey = relatedObjectConfig.getString("uid");
				JSONList orClause = new JSONList();
				for(int j = 0; j < objectList.size(); j++)
				{
					JSONObject object = objectList.getObject(j);
					String relatedObjectId = object.getString("data." + attributeName);
					if(relatedObjectId != null)
					{
						JSONObject idClause = new JSONObject();
						idClause.put(relatedObjectUIDKey, relatedObjectId);
						orClause.add(idClause);
					}
				}
				JSONObject relatedObjectDBFilter = new JSONObject();
				relatedObjectDBFilter.put("$or", orClause);
				//JSONObject dbResult = request(dataService, "{object:" + relatedObjectDBCollectionName + ",filter:" + relatedObjectDBFilter + "}");
				JSONObject result = getObjectList(relatedObjectName, new JSONObject(), new JSONObject("{addrelated:false, addvalidation:false}"));
				JSONList resultList = result.getList("list"); 
				for(int k = 0; k < resultList.size(); k++)
				{
					JSONObject resultObject = resultList.getObject(k);
					String dbResultId = resultObject.getString("id");
					for(int j = 0; j < objectList.size(); j++)
					{
						JSONObject object = objectList.getObject(j);
						String relatedObjectId = object.getString("data." + attributeName);
						if(relatedObjectId != null  &&  relatedObjectId.equals(dbResultId))
						{
							if(object.getObject("related") == null)
								object.put("related", new JSONObject());
							object.getObject("related").put(attributeName, resultObject);
						}
					}
				}
			}
		}
	}

	protected JSONObject updateObject(String objectName, String id, JSONObject updateData, JSONObject options) throws JSONException, FunctionErrorException
	{
		JSONObject object = getObject(objectName, id, null);
		JSONObject objectConfig = getObjectConfig(objectName);
		JSONObject dbUpdateData = new JSONObject();
		dbUpdateData.put(objectConfig.getString("uid"), id);
		if(object != null)
		{
			boolean doUpdate = true;
			JSONList attributeList = objectConfig.getList("attributes");
			for(int j = 0; j < attributeList.size(); j++)
			{
				JSONObject attributeConfig = attributeList.getObject(j);
				String attrKey = attributeConfig.getString("key");
				String attrName = attributeConfig.getString("name");
				String oldValue = object.getString("data." + attrName);
				String newValue = updateData.getString(attrName);
				if(oldValue != null  &&  newValue != null)
				{
					boolean doAttributeUpdate = true;
					if(oldValue.equals(newValue))
						doAttributeUpdate = false;
					if(doAttributeUpdate)
					{
						dbUpdateData.put(attrKey, newValue);
						object.put("data." + attrName, newValue);
					}
				}
			}
			
			if(doUpdate)
				firebus.publish(dataService, new Payload("{object:" + objectName + ",data:" + dbUpdateData + "}"));
			
			if(options != null  &&  options.get("addrelated") != null  &&  options.getString("addrelated").equals("true"))
				addRelatedValues(objectName, object);
		}
		return object;
	}
}
