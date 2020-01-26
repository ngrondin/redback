package io.redback.services;


import io.firebus.Firebus;
import io.firebus.exceptions.FunctionErrorException;
import io.firebus.exceptions.FunctionTimeoutException;
import io.firebus.utils.DataException;
import io.firebus.utils.DataMap;
import io.redback.RedbackException;

public abstract class ConfigurableService extends Service 
{
	protected String configService;
	
	public ConfigurableService(String n, DataMap c, Firebus f)
	{
		super(n, c, f);
		configService = config.getString("configservice");
	}
	
	public void clearCaches() 
	{
		
	}
	
	protected DataMap getConfig(String service, String category, String name) throws DataException, FunctionErrorException, FunctionTimeoutException, RedbackException
	{
		return request(configService, "{action:\"get\", service:\"" + service + "\", category:\"" + category + "\", name:\"" + name + "\"}");
	}
	
	protected DataMap listConfigs(String service, String category, DataMap filter) throws DataException, FunctionErrorException, FunctionTimeoutException, RedbackException
	{
		return request(configService, "{action:\"list\", service:\"" + service + "\", category:\"" + category + "\", filter:" + filter.toString() + "}");
	}

	protected DataMap listConfigs(String service, String category) throws DataException, FunctionErrorException, FunctionTimeoutException, RedbackException
	{
		return request(configService, "{action:\"list\", service:\"" + service + "\", category:\"" + category + "\"}");
	}
}
