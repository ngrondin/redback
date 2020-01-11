package com.nic.redback.services;

import com.nic.firebus.Firebus;
import com.nic.firebus.Payload;
import com.nic.firebus.exceptions.FunctionErrorException;
import com.nic.firebus.exceptions.FunctionTimeoutException;
import com.nic.firebus.utils.DataException;
import com.nic.firebus.utils.DataMap;
import com.nic.redback.RedbackException;

public abstract class DataService extends ConfigurableService
{
	protected String dataService;
	
	public DataService(String n, DataMap c, Firebus f)
	{
		super(n, c, f);
		dataService = config.getString("dataservice");
	}
	
	protected DataMap getData(String object, DataMap filter) throws DataException, FunctionErrorException, FunctionTimeoutException, RedbackException
	{
		if(dataService != null)
			return request(dataService, "{object:\"" + object + "\", filter:" + filter.toString() + "}");
		else
			return null;
	}

	protected void publishData(String object, DataMap key, DataMap data)
	{
		if(dataService != null)
			firebus.publish(dataService, new Payload("{object:\"" + object + "\", key:" + key.toString() + ", data:" + data.toString() + "}"));
	}
	
}
