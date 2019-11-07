package com.nic.redback.services;


import com.nic.firebus.Firebus;
import com.nic.firebus.exceptions.FunctionErrorException;
import com.nic.firebus.exceptions.FunctionTimeoutException;
import com.nic.firebus.utils.DataException;
import com.nic.firebus.utils.DataMap;
import com.nic.redback.RedbackException;

public abstract class ConfigurableService extends Service
{
	protected String configService;
	
	public ConfigurableService(DataMap c, Firebus f)
	{
		super(c, f);
		configService = config.getString("configservice");
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
