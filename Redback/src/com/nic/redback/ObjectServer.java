package com.nic.redback;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Logger;

import javax.script.ScriptException;


import com.nic.firebus.Firebus;
import com.nic.firebus.Payload;
import com.nic.firebus.exceptions.FunctionErrorException;
import com.nic.firebus.information.ServiceInformation;
import com.nic.firebus.utils.JSONList;
import com.nic.firebus.utils.JSONObject;
import com.nic.redback.security.UserProfile;

public class ObjectServer extends RedbackAuthenticatedService
{
	private Logger logger = Logger.getLogger("com.nic.redback");
	protected ObjectManager objectManager;
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
		logger.info("Object service start");
		Payload response = new Payload();
		try
		{
			JSONObject request = new JSONObject(payload.getString());
			UserProfile userProfile = null;
			String sessionId = payload.metadata.get("sessionid");
			String username = request.getString("username");
			String password = request.getString("password");
			String action = request.getString("action");
			String objectName = request.getString("object");
			JSONObject options = request.getObject("options");
			JSONObject responseData = null;
			boolean addValidation = false;
			boolean addRelated = false;
			
			if(username != null  &&  password != null)
			{
				userProfile = authenticate(username, password);
				response.metadata.put("sessionid", userProfile.getSessionId());
			}
			else if(sessionId != null)
			{
				userProfile = validateSession(sessionId);
			}

			if(userProfile != null)
			{
				if(action != null  &&  objectName != null)
				{
					if(options != null)
					{
						addValidation = options.getBoolean("addvalidation");
						addRelated = options.getBoolean("addrelated");
					}
					
					if(action.equals("get"))
					{
						String uid = request.getString("uid");
						if(uid != null)
						{
							RedbackObject object = objectManager.getObject(userProfile, objectName, uid, addRelated); 
							responseData = object.getJSON(addValidation, addRelated);
						}
						else
						{
							responseData = new JSONObject("{\"requesterror\":\"A 'get' action requires a 'uid' attribute\"}");
						}
					}
					else if(action.equals("list"))
					{
						JSONObject filter = request.getObject("filter");
						if(filter != null)
						{
							ArrayList<RedbackObject> objects = objectManager.getObjectList(userProfile, objectName, filter, addRelated);
							responseData = new JSONObject();
							JSONList list = new JSONList();
							for(int i = 0; i < objects.size(); i++)
								list.add(objects.get(i).getJSON(addValidation, addRelated));
							responseData.put("list", list);
						}
						else
						{
							responseData = new JSONObject("{\"requesterror\":\"A 'list' action requires a 'filter' attribute\"}");
						}
					}
					else if(action.equals("update"))
					{
						String uid = request.getString("uid");
						JSONObject data = request.getObject("data");
						if(uid != null  &&  data != null)
						{
							RedbackObject object = objectManager.updateObject(userProfile, objectName, uid, data, addRelated);
							responseData = object.getJSON(addValidation, addRelated);
						}
						else
						{
							responseData = new JSONObject("{\"requesterror\":\"An 'update' action requires a 'uid' and a 'data' attribute\"}");
						}
					}
					else if(action.equals("create"))
					{
						JSONObject data = request.getObject("data");
						RedbackObject object = objectManager.createObject(userProfile, objectName, data, addRelated);
						responseData = object.getJSON(addValidation, addRelated);
					}
					else if(action.equals("execute"))
					{
						String uid = request.getString("uid");
						String function = request.getString("function");
						JSONObject data = request.getObject("data");
						if(uid != null)
						{
							RedbackObject object = objectManager.executeFunction(userProfile, objectName, uid, function, data, addRelated);
							responseData = object.getJSON(addValidation, addRelated);
						}
						else
						{
							responseData = new JSONObject("{\"requesterror\":\"An 'create' action requires a 'uid' and a 'function' attribute\"}");
						}
					}
				}
				else
				{
					responseData = new JSONObject("{\"requesterror\":\"Requests must have at least an 'action' and an 'object' attribute\"}");
				}
			}
			else
			{
				responseData = new JSONObject("{\"authenticationerror\":\"Not logged in or invalid username or password\"}");
			}
			response.setData(responseData.toString());
		}
		catch(ScriptException e)
		{
			String errorMsg = e.getCause().getMessage();
			logger.severe(errorMsg);
			response.setData("{\"scripterror\":\"" + errorMsg.replace("\"", "'") + "\"}");
		}		
		catch(Exception e)
		{
			String errorMsg = e.getMessage();
			logger.severe(errorMsg);
			response.setData("{\"generalerror\":\"" + errorMsg + "\"}");
		}

		logger.info("Object service finish");
		return response;
	}

	public ServiceInformation getServiceInformation()
	{
		// TODO Auto-generated method stub
		return null;
	}
	

	
}
