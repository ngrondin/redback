package com.nic.redback.services;

import java.util.logging.Logger;

import com.nic.firebus.Firebus;
import com.nic.firebus.Payload;
import com.nic.firebus.exceptions.FunctionErrorException;
import com.nic.firebus.information.ServiceInformation;
import com.nic.firebus.utils.DataException;
import com.nic.firebus.utils.DataList;
import com.nic.firebus.utils.DataMap;
import com.nic.redback.RedbackException;
import com.nic.redback.security.Session;

public abstract class ConfigDevelopmentServer extends AuthenticatedService
{
	private Logger logger = Logger.getLogger("com.nic.redback");

	public ConfigDevelopmentServer(DataMap c, Firebus f)
	{
		super(c, f);
	}

	public Payload authenticatedService(Session session, Payload payload) throws FunctionErrorException
	{
		logger.info("Configuration service start");
		Payload response = new Payload();
		try
		{
			DataMap request = new DataMap(payload.getString());
			String action = request.getString("action");
			DataMap responseData = null;
			
			if(action != null)
			{
				if(action.equals("createobject"))
				{
					String name = request.getString("name");
					DataMap newObject = new DataMap("{name:\"" + name + "\"}");
					putDevConfig("rbo", "object", name, newObject);
					responseData = newObject;
				}
				else if(action.equals("getobject"))
				{
					String name = request.getString("name");
					if(name != null)
						responseData = getDevConfig("rbo", "object", name);
					else
						throw new FunctionErrorException("A 'getobject' action requires a '_id' attribute");
				}
				else if(action.equals("listobjects"))
				{
					DataList list = getDevConfigList("rbo", "object", null);
					responseData = new DataMap();
					responseData.put("result", list);
				}
				else if(action.equals("updateobject"))
				{
					DataMap cfg = request.getObject("config");
					if(cfg != null)
					{
						String name = cfg.getString("name");
						putDevConfig("rbo", "object", name, cfg);
						responseData = new DataMap("{result:\"ok\"}");
					}
					else
					{
						throw new FunctionErrorException("An 'updateobject' action requires a 'config' attribute");
					}
				}				
				else if(action.equals("getprocess"))
				{
					String name = request.getString("name");
					String version = request.getString("version");
					if(name != null  &&  version != null)
					{
						responseData = getDevConfig("rbpm", "process", name);
					}
					else
					{
						throw new FunctionErrorException("A 'getprocess' action requires a 'name' and 'version' attribute");
					}
				}
				else if(action.equals("listprocesses"))
				{
					DataList list = getDevConfigList("rbpm", "process", null);
					responseData = new DataMap();
					responseData.put("result", list);
				}
				else if(action.equals("updateprocess"))
				{
					DataMap cfg = request.getObject("config");
					if(cfg != null)
					{
						String name = cfg.getString("name");
						this.putDevConfig("rbpm", "process", name, cfg);
						responseData = new DataMap("{result:\"ok\"}");
					}
					else
					{
						throw new FunctionErrorException("An 'updateprocess' action requires a 'config' attribute");
					}
				}
				else
				{
					throw new FunctionErrorException("The '" + action + "' action is not valid as an object request");
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

		logger.info("Configuration service finish");
		return response;	
	}

	public Payload unAuthenticatedService(Session session, Payload payload)	throws FunctionErrorException
	{
		throw new FunctionErrorException("All requests need to be authenticated");
	}
	


	public ServiceInformation getServiceInformation()
	{
		// TODO Auto-generated method stub
		return null;
	}

	
	protected abstract DataMap getDevConfig(String service, String category, String name) throws RedbackException;
	
	protected abstract DataList getDevConfigList(String service, String category, DataMap filter) throws RedbackException;

	protected abstract void putDevConfig(String service, String category, String name, DataMap obj) throws RedbackException;
}
