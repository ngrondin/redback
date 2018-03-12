package com.nic.redback.services;

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
import com.nic.redback.security.Session;
import com.nic.redback.services.objectserver.ObjectConfig;
import com.nic.redback.services.objectserver.ObjectManager;
import com.nic.redback.services.objectserver.RedbackObject;

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
			Session session = null;
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
				session = authenticate(username, password);
				response.metadata.put("sessionid", session.getSessionId().toString());
			}
			else if(sessionId != null)
			{
				session = validateSession(sessionId);
			}

			if(session != null)
			{
				if(action != null)
				{
					if(objectName != null)
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
								RedbackObject object = objectManager.getObject(session.getUserProfile(), objectName, uid);
								objectManager.commitCurrentTransaction();
								responseData = object.getJSON(addValidation, addRelated);
							}
							else
							{
								responseData = new JSONObject("{\"requesterror\":\"A 'get' action requires a 'uid' attribute\"}");
							}
						}
						else if(action.equals("list"))
						{
							ArrayList<RedbackObject> objects = null;
							JSONObject filter = request.getObject("filter");
							String attribute = request.getString("attribute");
							String uid = request.getString("uid");
							if(filter == null)
								filter = new JSONObject();
							
							if(uid != null  &&  attribute != null)
								objects = objectManager.getObjectList(session.getUserProfile(), objectName, uid, attribute, filter);
							else
								objects = objectManager.getObjectList(session.getUserProfile(), objectName, filter);
							objectManager.commitCurrentTransaction();

							if(addRelated)
								objectManager.addRelatedBulk(session.getUserProfile(), objects);
							
							responseData = new JSONObject();
							JSONList list = new JSONList();
							for(int i = 0; i < objects.size(); i++)
								list.add(objects.get(i).getJSON(addValidation, addRelated));
							responseData.put("list", list);
						}
						else if(action.equals("update"))
						{
							String uid = request.getString("uid");
							JSONObject data = request.getObject("data");
							if(uid != null  &&  data != null)
							{
								RedbackObject object = objectManager.updateObject(session.getUserProfile(), objectName, uid, data);
								objectManager.commitCurrentTransaction();
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
							RedbackObject object = objectManager.createObject(session.getUserProfile(), objectName, data);
							objectManager.commitCurrentTransaction();
							responseData = object.getJSON(addValidation, addRelated);
						}
						else if(action.equals("execute"))
						{
							String uid = request.getString("uid");
							String function = request.getString("function");
							JSONObject data = request.getObject("data");
							if(uid != null)
							{
								RedbackObject object = objectManager.executeFunction(session.getUserProfile(), objectName, uid, function, data);
								objectManager.commitCurrentTransaction();
								responseData = object.getJSON(addValidation, addRelated);
							}
							else
							{
								responseData = new JSONObject("{\"requesterror\":\"A 'create' action requires a 'uid' and a 'function' attribute\"}");
							}
						}
						else
						{
							responseData = new JSONObject("{\"requesterror\":\"The '" + action + "' action is not valid as an object request\"}");
						}
					}
					else
					{
						if(action.equals("refreshconfig"))
						{
							objectManager.refreshAllConfigs();
							responseData = new JSONObject("{\"result\":\"Configs refreshed\"}");
						}
						else
						{
							responseData = new JSONObject("{\"requesterror\":\"The '" + action + "' action is not valid as an objectless request\"}");
						}
					}
				}
				else
				{
					responseData = new JSONObject("{\"requesterror\":\"Requests must have at least an 'action' attribute\"}");
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
			String errorMsg = buildErrorMessage(e);
			logger.severe(errorMsg);
			response.setData("{\"scripterror\":\"" + errorMsg.replace("\"", "'") + "\"}");
		}		
		catch(Exception e)
		{
			String errorMsg = buildErrorMessage(e);
			logger.severe(errorMsg);
			response.setData("{\"generalerror\":\"" + errorMsg + "\"}");
		}

		logger.info("Object service finish");
		return response;
	}
	
	protected String buildErrorMessage(Exception e)
	{
		String msg = "";
		Throwable t = e;
		while(t != null)
		{
			if(msg.length() > 0)
				msg += " : ";
			msg += t.getMessage();
			t = t.getCause();
		}
		return msg;
	}

	public ServiceInformation getServiceInformation()
	{
		// TODO Auto-generated method stub
		return null;
	}
	

	
}
