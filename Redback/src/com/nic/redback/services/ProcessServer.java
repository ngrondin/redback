package com.nic.redback.services;

import java.util.ArrayList;
import java.util.logging.Logger;

import javax.script.ScriptException;

import com.nic.firebus.Firebus;
import com.nic.firebus.Payload;
import com.nic.firebus.exceptions.FunctionErrorException;
import com.nic.firebus.information.ServiceInformation;
import com.nic.firebus.interfaces.Consumer;
import com.nic.firebus.utils.JSONException;
import com.nic.firebus.utils.JSONList;
import com.nic.firebus.utils.JSONObject;
import com.nic.redback.RedbackException;
import com.nic.redback.security.Session;
import com.nic.redback.services.processserver.ProcessInstance;
import com.nic.redback.services.processserver.ProcessManager;

public class ProcessServer extends RedbackAuthenticatedService implements Consumer
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
	
	public Payload unAuthenticatedService(Session session, Payload payload) throws FunctionErrorException
	{
		throw new FunctionErrorException("All requests need to be authenticated");
	}

	
	public Payload authenticatedService(Session session, Payload payload) throws FunctionErrorException
	{
		logger.info("Process service start");
		Payload response = new Payload();
		try
		{
			JSONObject request = new JSONObject(payload.getString());
			String action = request.getString("action");
			JSONObject responseData = null;
			
			if(action != null)
			{
				if(action.equals("initiate"))
				{
					String name = request.getString("name");
					JSONObject data = request.getObject("data");
					if(name != null)
					{
						ProcessInstance pi = processManager.initiateProcess(session, name, data);
						processManager.commitCurrentTransaction();
						responseData = new JSONObject("{\"pid\":\"" + pi.getId() + "\"}");
					}
					else
					{
						throw new FunctionErrorException("A 'initiate' action requires a 'name' attribute");
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
						processManager.processAction(session, extpid, pid, processAction, data);
						processManager.commitCurrentTransaction();
						responseData = new JSONObject("{\"result\":\"OK\"}");
					}
					else
					{
						throw new FunctionErrorException("A 'processaction' request requires 'pid' and 'processaction' attributes");
					}
				}
				else if(action.equals("getnotifications"))
				{
					String extpid = request.getString("extpid");
					JSONObject filter = request.getObject("filter");
					JSONList viewdata = request.getList("viewdata");
					if(filter != null)
					{
						ArrayList<JSONObject> result = processManager.getNotifications(session, extpid, filter, viewdata);
						JSONList responseList = new JSONList();
						for(int i = 0; i < result.size(); i++)
							responseList.add(result.get(i));
						responseData = new JSONObject();
						responseData.put("result", responseList);
					}
					else
					{
						throw new FunctionErrorException("A 'getnotifications' action requires a 'filter' attribute");
					}
				}
				/*
				else if(action.equals("findprocesses"))
				{
					JSONObject filter = request.getObject("filter");
					if(filter != null)
					{
						ArrayList<ProcessInstance> result = processManager.findProcesses(session, filter);
						JSONList responseList = new JSONList();
						for(int i = 0; i < result.size(); i++)
							responseList.add(result.get(i).getId());
						responseData = new JSONObject();
						responseData.put("result", responseList);
					}
					else
					{
						throw new FunctionErrorException("A 'getactions' action requires a 'pid' attribute");
						//responseData = new JSONObject("{\"requesterror\":\"A 'getactions' action requires a 'pid' attribute\"}");
					}
				}
				else if(action.equals("notifyprocess"))
				{
					String extpid = request.getString("extpid");
					String pid = request.getString("pid");
					JSONObject notification = request.getObject("notification");
					if(pid != null)
					{
						processManager.notifyProcess(session, extpid, pid, notification);
						processManager.commitCurrentTransaction();
						responseData = new JSONObject("{\"result\":\"OK\"}");
					}
					else
					{
						throw new FunctionErrorException("A 'processAction' request requires 'pid' and 'processaction' attributes");
						//responseData = new JSONObject("{\"requesterror\":\"A 'processAction' request requires 'pid' and 'processaction' attributes\"}");
					}
				}	
				*/				
			}
			else
			{
				throw new FunctionErrorException("Requests must have at least an 'action' attribute");
			}					

			response.setData(responseData.toString());
		}
		catch(ScriptException | JSONException | RedbackException e)
		{
			String errorMsg = buildErrorMessage(e);
			logger.severe(errorMsg);
			logger.severe(getStackTrace(e));
			throw new FunctionErrorException(errorMsg);
		}

		logger.info("Process service finish");
		return response;	
	}



	public ServiceInformation getServiceInformation()
	{
		// TODO Auto-generated method stub
		return null;
	}

	public void consume(Payload payload)
	{
		String msg = payload.getString();
		if(msg.equals("refreshconfig"))
			processManager.refreshAllConfigs();		
	}

	
}
