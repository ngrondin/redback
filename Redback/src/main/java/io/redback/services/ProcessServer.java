package io.redback.services;

import java.util.List;
import java.util.logging.Logger;

import io.firebus.Firebus;
import io.firebus.Payload;
import io.firebus.exceptions.FunctionErrorException;
import io.firebus.information.ServiceInformation;
import io.firebus.utils.DataException;
import io.firebus.utils.DataList;
import io.firebus.utils.DataMap;
import io.redback.RedbackException;
import io.redback.managers.processmanager.Assignment;
import io.redback.security.Session;

public abstract class ProcessServer extends AuthenticatedService
{
	private Logger logger = Logger.getLogger("io.redback");

	public ProcessServer(String n, DataMap c, Firebus f)
	{
		super(n, c, f);
	}

	
	public Payload unAuthenticatedService(Session session, Payload payload) throws FunctionErrorException
	{
		throw new FunctionErrorException("All requests need to be authenticated");
	}

	
	public Payload authenticatedService(Session session, Payload payload) throws FunctionErrorException
	{
		logger.finer("Process service start");
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
					if(process != null)
					{
						responseData = initiate(session, process, data);
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
					DataMap data = request.getObject("data");
					if(pid != null &&  processAction != null)
					{
						responseData = processAction(session, extpid, pid, processAction, data);
					}
					else
					{
						throw new FunctionErrorException("A 'processaction' request requires 'pid' and 'processaction' attributes");
					}
				}
				else if(action.equals("getassignments"))
				{
					String extpid = request.getString("extpid");
					DataMap filter = request.getObject("filter");
					DataList viewdata = request.getList("viewdata");
					List<Assignment> result = getAssignments(session, extpid, filter, viewdata);
					DataList responseList = new DataList();
					for(int i = 0; i < result.size(); i++)
						responseList.add(result.get(i).getDataMap());
					responseData = new DataMap();
					responseData.put("result", responseList);
				}
			}
			else
			{
				throw new FunctionErrorException("Requests must have at least an 'action' attribute");
			}					

			response.setData(responseData.toString());
		}
		catch(DataException | RedbackException e)
		{
			String errorMsg = buildErrorMessage(e);
			logger.severe(errorMsg);
			logger.severe(getStackTrace(e));
			throw new FunctionErrorException(errorMsg);
		}

		logger.finer("Process service finish");
		return response;	
	}



	public ServiceInformation getServiceInformation()
	{
		// TODO Auto-generated method stub
		return null;
	}


	protected abstract DataMap initiate(Session session, String process, DataMap data) throws RedbackException;
	
	protected abstract DataMap processAction(Session session, String extpid, String pid, String processAction, DataMap data) throws RedbackException;
	
	protected abstract List<Assignment> getAssignments(Session session, String extpid, DataMap filter, DataList viewdata) throws RedbackException;
	
}
