package com.nic.redback;

import java.util.HashMap;
import java.util.logging.Logger;

import com.nic.firebus.Payload;
import com.nic.firebus.exceptions.FunctionErrorException;
import com.nic.firebus.information.ServiceInformation;
import com.nic.firebus.utils.JSONException;
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
		JSONObject response = null;
		JSONObject objectConfig = getObjectConfig(object);
		String collection = objectConfig.getString("collection");
		Payload dataPayload = firebus.requestService(objectDB, new Payload("{object:" + collection + ",filter:" + requestData + "}"));
		JSONObject data = new JSONObject(dataPayload.getString());
		response = data;
		return response;
	}
}
