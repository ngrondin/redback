package io.redback.services;

import io.firebus.Firebus;
import io.firebus.Payload;
import io.firebus.information.ServiceInformation;
import io.firebus.data.DataList;
import io.firebus.data.DataMap;
import io.redback.exceptions.RedbackException;
import io.redback.security.Session;
import io.redback.services.common.ServiceProvider;

public abstract class ConfigServer extends ServiceProvider
{
	public ConfigServer(String n, DataMap c, Firebus f) 
	{
		super(n, c, f);
	}

	public Payload redbackService(Session session, Payload payload) throws RedbackException
	{
		Payload response = new Payload();
		try
		{
			DataMap request = payload.getDataMap();
			String action = request.getString("action");
			String service = request.getString("service");
			String category = request.getString("category");
			String name = request.getString("name");
			String domain = request.getString("domain");
			DataMap filter = request.getObject("filter");
			
			if(action.equals("get"))
			{
				DataMap config = null;
				if(domain == null)
					config = getConfig(service, category, name);
				else
					config = getDomainConfig(service, category, name, domain);
				if(config != null)
					response.setData(config);
			} 
			else if(action.equals("list"))
			{
				DataList list = null;
				if(domain == null)
					list = getConfigList(service, category, filter);
				else
					list = getDomainConfigList(service, category, domain, filter);
				if(list == null) 
					list = new DataList();
				response.setData(new DataMap("result", list));
			}
			else 
			{
				throw new RedbackException("Action '" + action + "' is unknown");
			}
		}
		catch(Exception e)
		{
			throw new RedbackException("Exception in config service", e);
		}
		
		return response;
	}
	
	protected abstract DataMap getConfig(String service, String category, String name) throws RedbackException;

	protected abstract DataMap getDomainConfig(String service, String category, String name, String domain) throws RedbackException;

	protected abstract DataList getConfigList(String service, String category, DataMap filter) throws RedbackException;

	protected abstract DataList getDomainConfigList(String service, String category, String domain, DataMap filter) throws RedbackException;

	
	public ServiceInformation getServiceInformation() 
	{
		return null;
	}
	
}
