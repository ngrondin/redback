package com.nic.redback.services;

import java.util.logging.Logger;

import com.nic.firebus.Firebus;
import com.nic.firebus.Payload;
import com.nic.firebus.exceptions.FunctionErrorException;
import com.nic.firebus.information.ServiceInformation;
import com.nic.firebus.utils.DataList;
import com.nic.firebus.utils.DataMap;
import com.nic.redback.RedbackException;

public abstract class ConfigServer extends Service
{
	private Logger logger = Logger.getLogger("com.nic.redback");

	public ConfigServer(DataMap c, Firebus f) 
	{
		super(c, f);
	}

	public Payload service(Payload payload) throws FunctionErrorException 
	{
		logger.finer("Config service start");
		Payload response = new Payload();
		try
		{
			DataMap request = new DataMap(payload.getString());
			String action = request.getString("action");
			String service = request.getString("service");
			String category = request.getString("category");
			String name = request.getString("name");
			DataMap filter = request.getObject("filter");
			
			if(action.equals("get"))
			{
				response.setData(getConfig(service, category, name).toString());
			} 
			else if(action.equals("list"))
			{
				response.setData("{\"result\":" + getConfigList(service, category, filter).toString() + "}");
			}
			else 
			{
				throw new FunctionErrorException("Action '" + action + "' is unknown");
			}
		}
		catch(Exception e)
		{
			logger.severe(e.getMessage());
			throw new FunctionErrorException(e.getMessage());
		}
		
		logger.finer("Config service finish");
		return response;
	}
	
	protected abstract DataMap getConfig(String service, String category, String name) throws RedbackException;
	
	protected abstract DataList getConfigList(String service, String category, DataMap filter) throws RedbackException;
	
	public ServiceInformation getServiceInformation() 
	{
		return null;
	}
	
}
