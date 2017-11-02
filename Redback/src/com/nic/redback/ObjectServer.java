package com.nic.redback;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Logger;


import com.nic.firebus.Firebus;
import com.nic.firebus.Payload;
import com.nic.firebus.exceptions.FunctionErrorException;
import com.nic.firebus.information.ServiceInformation;
import com.nic.firebus.utils.JSONList;
import com.nic.firebus.utils.JSONObject;

public class ObjectServer extends RedbackService
{
	private Logger logger = Logger.getLogger("com.nic.redback");
	protected ObjectManager objectManager;
	protected String dataService;
	protected String idGeneratorService;
	protected HashMap<String, ObjectConfig> objectConfigs;


	public ObjectServer(JSONObject c)
	{
		super(c);
		objectManager = new ObjectManager(config.getString("configservice"), config.getString("dataservice"), config.getString("idgeneratorservice"));
	}

	public void setFirebus(Firebus fb)
	{
		objectManager.setFirebus(fb);
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
					RedbackObject object = objectManager.getObject(objectName, uid, addRelated); 
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
					ArrayList<RedbackObject> objects = objectManager.getObjectList(objectName, filter, addRelated);
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
					RedbackObject object = objectManager.updateObject(objectName, uid, data, addRelated);
					responseData = object.getJSON(addValidation, addRelated);
				}
				else
				{
					responseData = new JSONObject("{error:\"An 'update' action requires a 'uid' and a 'data' attribute\"}");
				}
			}
			else if(action.equals("create"))
			{
				RedbackObject object = objectManager.createObject(objectName, addRelated);
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
	

	
}
