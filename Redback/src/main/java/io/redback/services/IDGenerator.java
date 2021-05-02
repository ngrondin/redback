package io.redback.services;

import io.firebus.Firebus;
import io.firebus.Payload;
import io.firebus.information.ServiceInformation;
import io.firebus.utils.DataMap;
import io.redback.RedbackException;
import io.redback.security.Session;
import io.redback.services.common.ServiceProvider;

public abstract class IDGenerator extends ServiceProvider
{
	public IDGenerator(String n, DataMap c, Firebus f) 
	{
		super(n, c, f);
	}

	public Payload redbackService(Session session, Payload payload) throws RedbackException 
	{
		Payload response = new Payload();
		String idName = payload.getString();
		try
		{
			String id = getNextId(session, idName);
			response.setData(id);
		}
		catch(Exception e)
		{
			throw new RedbackException("Exception in id generator service", e);
		}
		return response;
	}

	public ServiceInformation getServiceInformation() 
	{
		// TODO Auto-generated method stub
		return null;
	}

	protected abstract String getNextId(Session session, String name) throws RedbackException;

}
