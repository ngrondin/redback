package com.nic.redback;

import com.nic.firebus.Firebus;
import com.nic.firebus.Payload;
import com.nic.firebus.exceptions.FunctionErrorException;
import com.nic.firebus.exceptions.FunctionTimeoutException;
import com.nic.firebus.utils.DataException;
import com.nic.firebus.utils.DataMap;

public abstract class RedbackDataService extends RedbackConfigurableService
{
	protected String dataService;
	
	public RedbackDataService(DataMap c, Firebus f)
	{
		super(c, f);
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
