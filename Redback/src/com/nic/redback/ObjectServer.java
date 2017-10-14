package com.nic.redback;

import java.util.HashMap;
import java.util.Iterator;
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
			JSONObject responseData = null;
			
			if(action.equals("get"))
			{
				String id = request.getString("id");
				responseData = getObject(object, id);
			}
			if(action.equals("list"))
			{
				JSONObject filter = request.getObject("filter");
				responseData = getObjectList(object, filter);
			}
			if(action.equals("update"))
			{
				String id = request.getString("id");
				JSONObject data = request.getObject("data");
				responseData = updateObject(object, id, data);
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
		String anyFilterValue = objectFilter.getString("_any");
		JSONObject anyFilterDBValue = null;
		if(anyFilterValue != null)
			anyFilterDBValue =	new JSONObject("{$regex:\"" + anyFilterValue + "\"}");

		JSONList attributes = objectConfig.getList("attributes");
		for(int i = 0; i < attributes.size(); i++)
		{
			JSONObject attribute = attributes.getObject(i);
			String attrName = attribute.getString("name");
			String attrDBKey = attribute.getString("key");
			JSONEntity attrFilter = objectFilter.get(attrName);
			if(anyFilterDBValue != null)
			{
				JSONObject orTerm = new JSONObject();
				orTerm.put(attrDBKey, anyFilterDBValue);
				anyFilterList.add(orTerm);
			}
			if(attrFilter != null)
			{
				if(attrFilter instanceof JSONLiteral)
				{
					String filterValue = ((JSONLiteral)attrFilter).getString();
					if(filterValue.startsWith("*")  &&  filterValue.endsWith("*")  &&  filterValue.length() >= 2)
						dbFilter.put(attrDBKey, new JSONObject("{$regex:\"" + filterValue.substring(1, filterValue.length() - 1) + "\"}"));
					else
						dbFilter.put(attrDBKey, filterValue);
				}
			}
		}
		if(anyFilterList.size() > 0)
			dbFilter.put("$or", anyFilterList);
		return dbFilter;
	}

	protected JSONObject getObject(String objectName, String id) throws FunctionErrorException, JSONException
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
			object = processDBObject(objectName, dbObject);
		}
		return object;
	}
	
	protected JSONObject getObjectList(String objectName, JSONObject filterData) throws FunctionErrorException, JSONException
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
			JSONObject object = processDBObject(objectName, dbObject);
			objectList.add(object);
		}
		return response;
	}
	
	protected JSONObject processDBObject(String objectName, JSONObject dbObject) throws JSONException, FunctionErrorException
	{
		JSONObject objectConfig = getObjectConfig(objectName);
		JSONObject object = new JSONObject();
		JSONObject data = new JSONObject();
		JSONObject control = new JSONObject();
		
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
			control.put(attrName, attributeControl);
		}
		object.put("data", data);
		object.put("ctrl", control);
		return object;
	}
	

	protected JSONObject updateObject(String objectName, String id, JSONObject updateData) throws JSONException, FunctionErrorException
	{
		JSONObject object = getObject(objectName, id);
		JSONObject objectConfig = getObjectConfig(objectName);
		JSONObject dbUpdateData = new JSONObject();
		dbUpdateData.put("_id", id);
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
		}
		return object;
	}
}
