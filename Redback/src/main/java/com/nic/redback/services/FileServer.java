package com.nic.redback.services;

import java.util.logging.Logger;

import com.nic.firebus.Firebus;
import com.nic.firebus.Payload;
import com.nic.firebus.exceptions.FunctionErrorException;
import com.nic.firebus.information.ServiceInformation;
import com.nic.firebus.utils.DataException;
import com.nic.firebus.utils.DataMap;
import com.nic.redback.RedbackException;
import com.nic.redback.security.Session;

public abstract class FileServer extends AuthenticatedService
{
	private Logger logger = Logger.getLogger("com.nic.redback");

	public FileServer(String n, DataMap c, Firebus f)
	{
		super(n, c, f);
	}
	
	public Payload unAuthenticatedService(Session session, Payload payload) throws FunctionErrorException
	{
		throw new FunctionErrorException("Redback File Service always needs to receive authenticated requests");
	}

	public Payload authenticatedService(Session session, Payload payload) throws FunctionErrorException
	{
		try
		{
			logger.info("Authenticated file service start");
			Payload response = null;
			if(payload.metadata.get("mime") != null  &&  payload.metadata.get("mime").startsWith("image/"))
			{
				response = putFile(payload);
			}
			else
			{
				DataMap request = new DataMap(payload.getString());
				String action = request.getString("action");
				if(action != null)
				{
					if(action.equals("get"))
					{
						response = getFile(request.getString("uid"));
					}
				}
			}
			if(response == null)
			{
				response = new Payload("{error:\"no action taken\"}");
			}
			logger.info("Authenticated file service finished");
			return response;
		}
		catch(RedbackException | DataException e)
		{
			logger.severe(e.getMessage());
			throw new FunctionErrorException(e.getMessage());
		}
		
	}

	public ServiceInformation getServiceInformation()
	{
		// TODO Auto-generated method stub
		return null;
	}

	public abstract Payload getFile(String uid) throws RedbackException;
	
	public abstract Payload putFile(Payload filePayload);

	
}
