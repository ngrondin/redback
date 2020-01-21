package com.nic.redback.services;


import com.nic.firebus.Firebus;
import com.nic.firebus.Payload;
import com.nic.firebus.exceptions.FunctionErrorException;
import com.nic.firebus.exceptions.FunctionTimeoutException;
import com.nic.firebus.interfaces.Consumer;
import com.nic.firebus.utils.DataException;
import com.nic.firebus.utils.DataMap;
import com.nic.redback.RedbackException;

public abstract class ConfigurableService extends Service 
{
	protected String configService;
	
	public ConfigurableService(String n, DataMap c, Firebus f)
	{
		super(n, c, f);
		configService = config.getString("configservice");
		firebus.registerConsumer("_rb_config_cache_clear", new Consumer() {
			public void consume(Payload payload) {
				clearCaches();
			}}, 1);
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
