package io.redback.client;

import io.firebus.Firebus;
import io.firebus.data.DataMap;
import io.redback.exceptions.RedbackException;
import io.redback.security.Session;
import io.redback.security.UserProfile;
import io.redback.utils.Cache;

public class ConfigClient extends Client
{
	protected Cache<UserProfile> cachedUserProfiles;

	
	public ConfigClient(Firebus fb, String sn) 
	{
		super(fb, sn);
	}
	
	public DataMap getConfig(Session session, String service, String category, String name) throws RedbackException
	{
		DataMap req = new DataMap("action", "get", "service", service, "category", category, "name", name);
		return requestDataMap(session, req);
	}
	
	public DataMap getDomainConfig(Session session, String service, String category, String name, String domain) throws RedbackException
	{
		DataMap req = new DataMap("action", "getdomain", "service", service, "category", category, "name", name, "domain", domain);
		return requestDataMap(session, req);
	}
	
	public DataMap listConfigs(Session session, String service, String category, DataMap filter) throws RedbackException
	{
		DataMap req = new DataMap("action", "list", "service", service, "category", category, "filter", filter);
		return requestDataMap(session, req);
	}

	public DataMap listConfigs(Session session, String service, String category) throws RedbackException
	{
		DataMap req = new DataMap("action", "list", "service", service, "category", category);
		return requestDataMap(session, req);

	}
	
	public DataMap listDomainConfigs(Session session, String service, String category, DataMap filter) throws RedbackException
	{
		DataMap req = new DataMap("action", "listdomain", "service", service, "category", category, "filter", filter);
		return requestDataMap(session, req);
	}


}
