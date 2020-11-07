package io.redback.services;

import java.util.logging.Logger;

import io.firebus.Firebus;
import io.firebus.Payload;
import io.firebus.exceptions.FunctionErrorException;
import io.firebus.information.ServiceInformation;
import io.firebus.interfaces.ServiceProvider;
import io.firebus.utils.DataList;
import io.firebus.utils.DataMap;
import io.redback.RedbackException;
import io.redback.utils.StringUtils;

public abstract class ConfigServer extends Service implements ServiceProvider
{
	private Logger logger = Logger.getLogger("io.redback");

	public ConfigServer(String n, DataMap c, Firebus f) 
	{
		super(n, c, f);
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
			logger.severe(StringUtils.getStackTrace(e));
			throw new FunctionErrorException("Exception in config service", e);
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
