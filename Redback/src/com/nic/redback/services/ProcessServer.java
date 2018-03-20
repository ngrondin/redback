package com.nic.redback.services;

import java.util.ArrayList;
import java.util.logging.Logger;

import com.nic.firebus.Firebus;
import com.nic.firebus.Payload;
import com.nic.firebus.exceptions.FunctionErrorException;
import com.nic.firebus.information.ServiceInformation;
import com.nic.firebus.utils.JSONList;
import com.nic.firebus.utils.JSONObject;
import com.nic.redback.security.Session;
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
			//JSONObject options = request.getObject("options");
			JSONObject responseData = null;
			
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
							processManager.commitCurrentTransaction();
							responseData = new JSONObject("{\"pid\":\"" + pi.getId() + "\"}");
						}
						else
						{
							responseData = new JSONObject("{\"requesterror\":\"A 'initiate' action requires a 'name' attribute\"}");
						}
					}
					else if(action.equals("processaction"))
					{
						String extpid = request.getString("extpid");
						String pid = request.getString("pid");
						String processAction = request.getString("processaction");
						JSONObject data = request.getObject("data");
						if(pid != null &&  processAction != null)
						{
							processManager.processAction(session.getUserProfile(), extpid, pid, processAction, data);
							processManager.commitCurrentTransaction();
							responseData = new JSONObject("{\"result\":\"OK\"}");
						}
						else
						{
							responseData = new JSONObject("{\"requesterror\":\"A 'processAction' request requires 'pid' and 'processaction' attributes\"}");
						}
					}
					else if(action.equals("getactions"))
					{
						String pid = request.getString("pid");
						if(pid != null)
						{
							JSONList actions = processManager.getActions(session.getUserProfile(), pid);
							responseData = new JSONObject();
							responseData.put("actions", actions);
						}
						else
						{
							responseData = new JSONObject("{\"requesterror\":\"A 'getactions' action requires a 'pid' attribute\"}");
						}
					}
					else if(action.equals("findprocesses"))
					{
						JSONObject filter = request.getObject("filter");
						if(filter != null)
						{
							ArrayList<ProcessInstance> result = processManager.findProcesses(session.getUserProfile(), filter);
							JSONList responseList = new JSONList();
							for(int i = 0; i < result.size(); i++)
								responseList.add(result.get(i).getId());
							responseData = new JSONObject();
							responseData.put("result", responseList);
						}
						else
						{
							responseData = new JSONObject("{\"requesterror\":\"A 'getactions' action requires a 'pid' attribute\"}");
						}
					}
					else if(action.equals("notifyprocess"))
					{
						String extpid = request.getString("extpid");
						String pid = request.getString("pid");
						JSONObject notification = request.getObject("notification");
						if(pid != null)
						{
							processManager.notifyProcess(session.getUserProfile(), extpid, pid, notification);
							processManager.commitCurrentTransaction();
							responseData = new JSONObject("{\"result\":\"OK\"}");
						}
						else
						{
							responseData = new JSONObject("{\"requesterror\":\"A 'processAction' request requires 'pid' and 'processaction' attributes\"}");
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
