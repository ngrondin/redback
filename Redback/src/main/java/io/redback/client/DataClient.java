package io.redback.client;

import io.firebus.Firebus;
import io.firebus.exceptions.FunctionErrorException;
import io.firebus.exceptions.FunctionTimeoutException;
import io.firebus.utils.DataException;
import io.firebus.utils.DataList;
import io.firebus.utils.DataMap;
import io.redback.RedbackException;

public class DataClient extends Client
{

	public DataClient(Firebus fb, String sn) 
	{
		super(fb, sn);
	}

	public DataMap getData(String object, DataMap filter, DataMap sort, int page, int pageSize) throws DataException, FunctionErrorException, FunctionTimeoutException, RedbackException
	{
		DataMap req = new DataMap();
		req.put("object", object);
		req.put("filter", filter);
		if(sort != null)
			req.put("sort", sort);
		req.put("page", page);
		req.put("pagesize", pageSize);
		return request(req);
	}

	public DataMap getData(String object, DataMap filter, DataMap sort) throws DataException, FunctionErrorException, FunctionTimeoutException, RedbackException
	{
		return getData(object, filter, sort, 0, 50);
	}	

	public DataMap putData(String object, DataMap key, DataMap data) throws RedbackException
	{
		DataMap req = new DataMap();
		req.put("object", object);
		req.put("key", key);
		req.put("data", data);
		return request(req);
	}

	public DataMap aggregateData(String object, DataMap filter, DataList tuple, DataList metrics, DataMap sort) throws DataException, FunctionErrorException, FunctionTimeoutException, RedbackException
	{
		DataMap req = new DataMap();
		req.put("object", object);
		req.put("filter", filter);
		req.put("tuple", tuple);
		req.put("metrics", metrics);
		if(sort != null)
			req.put("sort", sort);
		return request(req);
	}

	public void publishData(String object, DataMap key, DataMap data) throws RedbackException
	{
		DataMap req = new DataMap();
		req.put("object", object);
		req.put("key", key);
		req.put("data", data);
		publish(req);
	}
}
