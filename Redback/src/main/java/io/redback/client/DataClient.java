package io.redback.client;

import io.firebus.Firebus;
import io.firebus.data.DataList;
import io.firebus.data.DataMap;
import io.redback.exceptions.RedbackException;

public class DataClient extends Client
{

	public DataClient(Firebus fb, String sn) 
	{
		super(fb, sn);
	}

	public DataMap getData(String object, DataMap filter, DataMap sort, int page, int pageSize) throws RedbackException
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

	public DataMap getData(String object, DataMap filter, DataMap sort) throws RedbackException
	{
		return getData(object, filter, sort, 0, 50);
	}	

	public DataMap getData(String object, DataMap filter) throws RedbackException
	{
		return getData(object, filter, null, 0, 50);
	}	
	
	public DataMap putData(String object, DataMap key, DataMap data, boolean replace) throws RedbackException
	{
		DataMap req = new DataMap();
		req.put("object", object);
		req.put("key", key);
		req.put("data", data);
		if(replace) 
			req.put("operation", "replace");
		return request(req);
	}

	public DataMap putData(String object, DataMap key, DataMap data) throws RedbackException
	{
		return putData(object, key, data, false);
	}
	
	public DataMap aggregateData(String object, DataMap filter, DataList tuple, DataList metrics, DataMap sort, int page, int pageSize) throws RedbackException
	{
		DataMap req = new DataMap();
		req.put("object", object);
		req.put("filter", filter);
		req.put("tuple", tuple);
		req.put("metrics", metrics);
		if(sort != null)
			req.put("sort", sort);
		req.put("page", page);
		req.put("pagesize", pageSize);
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
	
	public DataMap deleteData(String object, DataMap key) throws RedbackException
	{
		DataMap req = new DataMap();
		req.put("object", object);
		req.put("key", key);
		req.put("operation", "delete");
		return request(req);
	}	
}
