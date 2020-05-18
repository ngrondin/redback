package io.redback.services;

import io.firebus.Firebus;
import io.firebus.Payload;
import io.firebus.exceptions.FunctionErrorException;
import io.firebus.exceptions.FunctionTimeoutException;
import io.firebus.utils.DataException;
import io.firebus.utils.DataList;
import io.firebus.utils.DataMap;
import io.redback.RedbackException;

public abstract class DataService// extends ConfigurableService
{
	/*
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

	protected DataMap aggregateData(String object, DataMap filter, DataList tuple, DataList metrics) throws DataException, FunctionErrorException, FunctionTimeoutException, RedbackException
	{
		if(dataService != null)
			return request(dataService, "{object:\"" + object + "\", filter:" + filter.toString() + ", tuple:" + tuple + ", metrics:" + metrics +"}");
		else
			return null;
	}

	protected void publishData(String object, DataMap key, DataMap data)
	{
		if(dataService != null)
			firebus.publish(dataService, new Payload("{object:\"" + object + "\", key:" + key.toString() + ", data:" + data.toString() + "}"));
	}
	*/
}
