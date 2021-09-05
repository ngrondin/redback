package io.redback.client;

import io.firebus.Firebus;
import io.firebus.data.DataMap;
import io.redback.exceptions.RedbackException;
import io.redback.security.Session;

public class ConfigurationClient extends Client
{

	public ConfigurationClient(Firebus fb, String sn) 
	{
		super(fb, sn);
	}
	
	public DataMap getConfig(Session session, String service, String category, String name) throws RedbackException
	{
		DataMap req = new DataMap();
		req.put("action", "get");
		req.put("service", service);
		req.put("category", category);
		req.put("name", name);
		return request(session, req);
	}
	
	public DataMap listConfigs(Session session, String service, String category, DataMap filter) throws RedbackException
	{
		DataMap req = new DataMap();
		req.put("action", "list");
		req.put("service", service);
		req.put("category", category);
		req.put("filter", filter);
		return request(session, req);
	}

	public DataMap listConfigs(Session session, String service, String category) throws RedbackException
	{
		DataMap req = new DataMap();
		req.put("action", "list");
		req.put("service", service);
		req.put("category", category);
		return request(session, req);
	}
	

}
