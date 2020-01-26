package io.redback.services;

import java.util.logging.Logger;

import io.firebus.Firebus;
import io.firebus.Payload;
import io.firebus.exceptions.FunctionErrorException;
import io.firebus.information.ServiceInformation;
import io.firebus.utils.DataException;
import io.firebus.utils.DataMap;
import io.redback.RedbackException;
import io.redback.security.Session;

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
