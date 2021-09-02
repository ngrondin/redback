package io.redback.services;

import java.util.Date;
import java.util.List;

import io.firebus.Firebus;
import io.firebus.Payload;
import io.firebus.information.ServiceInformation;
import io.firebus.utils.DataException;
import io.firebus.utils.DataList;
import io.firebus.utils.DataMap;
import io.redback.exceptions.RedbackException;
import io.redback.managers.processmanager.Notification;
import io.redback.managers.processmanager.ProcessInstance;
import io.redback.security.Session;
import io.redback.services.common.AuthenticatedServiceProvider;

public abstract class ProcessServer extends AuthenticatedServiceProvider
{

	public ProcessServer(String n, DataMap c, Firebus f)
	{
		super(n, c, f);
	}

	
	public Payload redbackUnauthenticatedService(Session session, Payload payload) throws RedbackException
	{
		throw new RedbackException("All requests need to be authenticated");
	}

	
	public Payload redbackAuthenticatedService(Session session, Payload payload) throws RedbackException
	{
		Payload response = new Payload();
		try
		{
			DataMap request = new DataMap(payload.getString());
			String action = request.getString("action");
			DataMap responseData = null;
			
			if(action != null)
			{
				if(action.equals("initiate"))
				{
					String process = request.getString("process");
					DataMap data = request.getObject("data");
					String domain = request.getString("domain");
					if(process != null)
					{
						ProcessInstance pi = initiate(session, process, domain, data);
						responseData = new DataMap("instance", pi.getId());
					}
					else
					{
						throw new RedbackException("A 'initiate' action requires a 'name' attribute");
					}
				}
				else if(action.equals("continueprocess"))
				{
					String pid = request.getString("pid");
					if(pid != null)
					{
						continueProcess(session, pid);
						responseData = new DataMap("result", "ok");
					}
					else
					{
						throw new RedbackException("A 'continueprocess' request requires 'pid'");
					}
				}				
				else if(action.equals("processaction") || action.equals("actionprocess"))
				{
					String pid = request.getString("pid");
					String processAction = request.getString("processaction");
					Date date = request.getDate("date");
					DataMap data = request.getObject("data");
					if(pid != null &&  processAction != null)
					{
						actionProcess(session, pid, processAction, date, data);
						responseData = new DataMap("result", "ok");
					}
					else
					{
						throw new RedbackException("A 'actionprocess' request requires 'pid' and 'processaction' attributes");
					}
				}
				else if(action.equals("interruptprocess"))
				{
					String pid = request.getString("pid");
					if(pid != null)
					{
						interruptProcess(session, pid);
						responseData = new DataMap("result", "ok");
					}
					else
					{
						throw new RedbackException("A 'interruptprocess' request requires 'pid'");
					}
				}
				else if(action.equals("interruptprocesses"))
				{
					DataMap filter = request.getObject("filter");
					if(filter != null)
					{
						interruptProcesses(session, filter);
						responseData = new DataMap("result", "ok");
					}
					else
					{
						throw new RedbackException("A 'interruptprocesses' request requires 'filter'");
					}
				}
				else if(action.equals("getassignments"))
				{
					DataMap filter = request.getObject("filter");
					DataList viewdata = request.getList("viewdata");
					DataList responseList = new DataList();
					int page = request.containsKey("page") ? request.getNumber("page").intValue() : 0;
					int pageSize = request.containsKey("pageSize") ? request.getNumber("pageSize").intValue() : 50;
					List<Notification> result = getAssignments(session, filter, viewdata, page, pageSize);
					if(result != null) 
					{
						for(int i = 0; i < result.size(); i++)
							responseList.add(result.get(i).getDataMap());
					}
					responseData = new DataMap();
					responseData.put("result", responseList);
				}
				else if(action.equals("getassignmentcount"))
				{
					DataMap filter = request.getObject("filter");
					int count = getAssignmentCount(session, filter);
					responseData = new DataMap();
					responseData.put("count", count);
				}
			}
			else
			{
				throw new RedbackException("Requests must have at least an 'action' attribute");
			}
			if(responseData != null)
				response.setData(responseData.toString());
		}
		catch(DataException e)
		{
			throw new RedbackException("Error in process server", e);
		}

		return response;	
	}



	public ServiceInformation getServiceInformation()
	{
		return null;
	}


	protected abstract ProcessInstance initiate(Session session, String process, String domain, DataMap data) throws RedbackException;
	
	protected abstract void continueProcess(Session session, String pid) throws RedbackException;
	
	protected abstract void actionProcess(Session session, String pid, String processAction, Date date, DataMap data) throws RedbackException;
	
	protected abstract void interruptProcess(Session session, String pid) throws RedbackException;

	protected abstract void interruptProcesses(Session session, DataMap filter) throws RedbackException;

	protected abstract List<Notification> getAssignments(Session session, DataMap filter, DataList viewdata, int page, int pageSize) throws RedbackException;
	
	protected abstract int getAssignmentCount(Session session, DataMap filter) throws RedbackException;
	
}
