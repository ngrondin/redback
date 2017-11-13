package com.nic.redback;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Logger;

import javax.script.ScriptException;


import com.nic.firebus.Firebus;
import com.nic.firebus.Payload;
import com.nic.firebus.exceptions.FunctionErrorException;
import com.nic.firebus.information.ServiceInformation;
import com.nic.firebus.utils.JSONException;
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
		objectManager = new ObjectManager(config);
	}

	public void setFirebus(Firebus fb)
	{
		super.setFirebus(fb);
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

			if(action != null  &&  objectName != null)
			{
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
						responseData = new JSONObject("{requesterror:\"A 'get' action requires a 'uid' attribute\"}");
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
						responseData = new JSONObject("{requesterror:\"A 'list' action requires a 'filter' attribute\"}");
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
						responseData = new JSONObject("{requesterror:\"An 'update' action requires a 'uid' and a 'data' attribute\"}");
					}
				}
				else if(action.equals("create"))
				{
					JSONObject data = request.getObject("data");
					RedbackObject object = objectManager.createObject(objectName, data, addRelated);
					responseData = object.getJSON(addValidation, addRelated);
				}
				else if(action.equals("execute"))
				{
					String uid = request.getString("uid");
					String function = request.getString("function");
					JSONObject data = request.getObject("data");
					if(uid != null)
					{
						RedbackObject object = objectManager.executeFunction(objectName, uid, function, data, addRelated);
						responseData = object.getJSON(addValidation, addRelated);
					}
					else
					{
						responseData = new JSONObject("{requesterror:\"An 'create' action requires a 'uid' and a 'function' attribute\"}");
					}
				}
			}
			else
			{
				responseData = new JSONObject("{requesterror:\"Requests must have at least an 'action' and an 'object' attribute\"}");
			}
			response.setData(responseData.toString());
		}
		catch(ScriptException e)
		{
			logger.severe(e.getMessage());
			try
			{
				JSONObject responseData = new JSONObject("{scripterror:\"" + e.getCause().getMessage() + "\"}");
				response.setData(responseData.toString());
			}
			catch(JSONException e2)
			{
				logger.severe(e2.getMessage());
			}
		}		
		catch(Exception e)
		{
			logger.severe(e.getMessage());
			try
			{
				JSONObject responseData = new JSONObject("{generalerror:\"" + e.getMessage() + "\"}");
				response.setData(responseData.toString());
			}
			catch(JSONException e2)
			{
				logger.severe(e2.getMessage());
			}
		}
		return response;
	}

	public ServiceInformation getServiceInformation()
	{
		// TODO Auto-generated method stub
		return null;
	}
	

	
}
