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
	protected String configDB;
	protected String objectDB;
	protected HashMap<String, JSONObject> objectConfigs;

	public ObjectServer(JSONObject c)
	{
		super(c);
		configDB = config.getString("configdb");
		objectDB = config.getString("objectdb");
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
			JSONObject requestData = request.getObject("data");
			JSONObject responseData = null;
			
			if(action.equals("get"))
				responseData = getObject(object, requestData);
			if(action.equals("getlist"))
				responseData = getObjectList(object, requestData);

			response.setData(responseData.toString());
		}
		catch(Exception e)
		{
			throw new FunctionErrorException(e.getMessage());
		}
		return response;
	}

	public ServiceInformation getServiceInformation()
	{
		// TODO Auto-generated method stub
		return null;
	}

	protected JSONObject getObjectConfig(String object) throws JSONException, FunctionErrorException
	{
		JSONObject objectConfig = objectConfigs.get(object);
		if(objectConfig == null)
		{
			JSONObject configList = new JSONObject(firebus.requestService(configDB, new Payload("{object:rbo_config,filter:{name:" + object + "}}")).getString());
			if(configList.getList("result").size() > 0)
			{
				objectConfig = configList.getObject("result.0");
				objectConfigs.put(object, objectConfig);
			}
		}
		return objectConfig;
	}
	
	protected JSONObject getObject(String object, JSONObject requestData) throws FunctionErrorException, JSONException
	{
		JSONObject response = null;
		JSONObject data = getObjectList(object, requestData);
		if(data.getList("result").size() > 0)
			response = data.getObject("result.0");
		return response;
	}
	

	protected JSONObject getObjectList(String object, JSONObject requestData) throws FunctionErrorException, JSONException
	{
		JSONObject objectConfig = getObjectConfig(object);
		String collection = objectConfig.getString("collection");
		Payload dataPayload = firebus.requestService(objectDB, new Payload("{object:" + collection + ",filter:" + requestData + "}"));
		JSONObject dbResult = new JSONObject(dataPayload.getString());
		JSONList dbResultList = dbResult.getList("result");
		
		JSONObject response = new JSONObject();
		JSONList list = new JSONList();
		response.put("result", list);
		for(int i = 0; i < dbResultList.size(); i++)
		{
			JSONObject dbObj = dbResultList.getObject(i);
			JSONObject obj = new JSONObject();
			JSONList attributeList = objectConfig.getList("attributes");
			for(int j = 0; j < attributeList.size(); j++)
			{
				JSONObject attributeConfig = attributeList.getObject(j);
				String attrKey = attributeConfig.getString("key");
				String attrName = attributeConfig.getString("name");
				String attrEditable = attributeConfig.getString("editable");
				JSONList attrLOV = attributeConfig.getList("listofvalues");
				JSONObject attributeObject = new JSONObject();
				attributeObject.put("value", dbObj.getString(attrKey));
				attributeObject.put("editable", attrEditable);
				if(attrLOV != null)
					attributeObject.put("listofvalues", attrLOV);
				obj.put(attrName, attributeObject);
			}
			list.add(obj);
		}
		return response;
	}
	
	/*
	protected JSONObject processDBObject(JSONObject dbObj)
	{
		JSONObject obj = new JSONObject();
		Iterator<String> it = dbObj.keySet().iterator();
		while(it.hasNext())
		{
			String key = it.next();
			JSONEntity dbEntity = dbObj.get(key);
			if(dbEntity instanceof JSONLiteral)
			{
				JSONLiteral literal = (JSONLiteral)dbEntity;
				JSONObject subObj = new JSONObject();
				subObj.put("value", literal);
				obj.put(key, subObj);
			}
			else if(dbEntity instanceof JSONObject)
			{
				JSONObject subObj = processDBObject((JSONObject)dbEntity);
				obj.put(key, subObj);
			}
			else if(dbEntity instanceof JSONList)
			{
				JSONList dbList = (JSONList)dbEntity;
				JSONList list = new JSONList();
				for(int i = 0; i < dbList.size(); i++)
				{
					JSONObject subObj = processDBObject(list.getObject(i));
					list.add(subObj);
				}
				obj.put(key, list);
			}
		}
		return obj;		
	}
	*/
}
