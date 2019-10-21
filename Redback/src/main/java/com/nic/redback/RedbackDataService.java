package com.nic.redback;

import com.nic.firebus.Payload;
import com.nic.firebus.exceptions.FunctionErrorException;
import com.nic.firebus.exceptions.FunctionTimeoutException;
import com.nic.firebus.utils.DataException;
import com.nic.firebus.utils.DataMap;

public abstract class RedbackDataService extends RedbackConfigurableService
{
	protected String dataService;
	
	public RedbackDataService(DataMap c)
	{
		super(c);
		dataService = config.getString("dataservice");
	}
	
	protected DataMap getData(String object, DataMap filter) throws DataException, FunctionErrorException, FunctionTimeoutException, RedbackException
	{
		return request(dataService, "{object:\"" + object + "\", filter:" + filter.toString() + "}");
	}

	protected DataMap getData(String object, String filter) throws DataException, FunctionErrorException, FunctionTimeoutException, RedbackException
	{
		return request(dataService, "{object:\"" + object + "\", filter:" + filter + "}");
	}

	protected void publishData(String object, DataMap data)
	{
		firebus.publish(dataService, new Payload("{object:\"" + object + "\", data:" + data.toString() + "}"));
	}

	protected void publishData(String object, String data)
	{
		firebus.publish(dataService, new Payload("{object:\"" + object + "\", data:" + data + "}"));
	}


}
