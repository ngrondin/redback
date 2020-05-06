package io.redback.client;

import io.firebus.Firebus;
import io.firebus.utils.DataMap;
import io.redback.RedbackException;

public class ConfigurationClient extends Client
{

	public ConfigurationClient(Firebus fb, String sn) 
	{
		super(fb, sn);
	}
	
	public DataMap getConfig(String service, String category, String name) throws RedbackException
	{
		DataMap req = new DataMap();
		req.put("action", "get");
		req.put("service", service);
		req.put("category", category);
		req.put("name", name);
		return request(req);
	}
	
	public DataMap listConfigs(String service, String category, DataMap filter) throws RedbackException
	{
		DataMap req = new DataMap();
		req.put("action", "list");
		req.put("service", service);
		req.put("category", category);
		req.put("filter", filter);
		return request(req);
	}

	public DataMap listConfigs(String service, String category) throws RedbackException
	{
		DataMap req = new DataMap();
		req.put("action", "list");
		req.put("service", service);
		req.put("category", category);
		return request(req);
	}
	

}
