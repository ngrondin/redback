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
import com.nic.redback.services.processserver.ProcessInstance;
import com.nic.redback.services.processserver.ProcessManager;

public class ProcessServer extends RedbackAuthenticatedService
{
	private Logger logger = Logger.getLogger("com.nic.redback");
	protected ProcessManager processManager;


	public ProcessServer(JSONObject c)
	{
		super(c);
		processManager = new ProcessManager(config);
	}

	public void setFirebus(Firebus fb)
	{
		super.setFirebus(fb);
		processManager.setFirebus(fb);
	}
	
	public Payload service(Payload payload) throws FunctionErrorException
	{
		logger.info("Process service start");
		Payload response = new Payload();
		try
		{
			JSONObject request = new JSONObject(payload.getString());
			Session session = null;
			String sessionId = payload.metadata.get("sessionid");
			String username = request.getString("username");
			String password = request.getString("password");
			String action = request.getString("action");
			String processId = request.getString("pid");
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
					if(action.equals("initiate"))
					{
						String name = request.getString("name");
						JSONObject data = request.getObject("data");
						if(name != null)
						{
							ProcessInstance pi = processManager.initiateProcess(session.getUserProfile(), name, data);
							responseData = new JSONObject("{\"pid\":\"" + pi.getId() + "\"}");
						}
						else
						{
							responseData = new JSONObject("{\"requesterror\":\"A 'initiate' action requires a 'name' attribute\"}");
						}
					}
					else if(action.equals("event"))
					{
						String pid = request.getString("pid");
						String event = request.getString("event");
						JSONObject data = request.getObject("data");
						if(pid != null)
						{
							ProcessInstance pi = processManager.processEvent(session.getUserProfile(), pid, event, data);
							responseData = new JSONObject("{\"result\":\"OK\"}");
						}
						else
						{
							responseData = new JSONObject("{\"requesterror\":\"An 'event' action requires a 'pid' attribute\"}");
						}
					}
					else if(action.equals("getactions"))
					{
						String pid = request.getString("pid");
						if(pid != null)
						{
							ArrayList<String[]> actions = processManager.getActions(session.getUserProfile(), pid);
							responseData = new JSONObject();
							JSONList list = new JSONList();
							for(int i = 0; i < actions.size(); i++)
							{
								JSONObject entry = new JSONObject();
								entry.put("key", actions.get(i)[0]);
								entry.put("name", actions.get(i)[1]);
								list.add(entry);
							}
							responseData.put("list", list);
						}
						else
						{
							responseData = new JSONObject("{\"requesterror\":\"A 'getactions' action requires a 'pid' attribute\"}");
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
		catch(Exception e)
		{
			String errorMsg = buildErrorMessage(e);
			logger.severe(errorMsg);
			response.setData("{\"generalerror\":\"" + errorMsg + "\"}");
		}

		logger.info("Process service finish");
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
