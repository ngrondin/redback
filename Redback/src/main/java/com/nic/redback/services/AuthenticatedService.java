package com.nic.redback.services;

import java.util.logging.Logger;

import com.nic.firebus.Firebus;
import com.nic.firebus.Payload;
import com.nic.firebus.exceptions.FunctionErrorException;
import com.nic.firebus.exceptions.FunctionTimeoutException;
import com.nic.firebus.utils.DataException;
import com.nic.firebus.utils.DataMap;
import com.nic.redback.RedbackException;
import com.nic.redback.security.Session;

public abstract class AuthenticatedService extends DataService
{
	private Logger logger = Logger.getLogger("com.nic.redback");
	protected String accessManagementService;

	public AuthenticatedService(String n, DataMap c, Firebus f)
	{
		super(n, c, f);
		accessManagementService = config.getString("accessmanagementservice");
	}
	
	public Payload service(Payload payload) throws FunctionErrorException
	{
		Payload response = null;
		Session session = null;
		String token = payload.metadata.get("token");
		
		logger.finer("Authenticated service start (token: " + token + ")");
		try
		{
			if(token != null)
			{
				session = validateToken(token);
			}

			if(session != null)
			{			
				payload.metadata.remove("token");
				response = authenticatedService(session, payload);
			}
			else
			{
				response = unAuthenticatedService(session, payload);
			}
			logger.finer("Authenticated service finish");
			return response;
		}
		catch(RedbackException | DataException | FunctionTimeoutException e)
		{
			String errorMsg = buildErrorMessage(e);
			logger.severe(errorMsg);
			logger.severe(getStackTrace(e));
			throw new FunctionErrorException(errorMsg);
		}

	}

	public abstract Payload authenticatedService(Session session, Payload payload) throws FunctionErrorException;

	public abstract Payload unAuthenticatedService(Session session, Payload payload) throws FunctionErrorException;

	
	protected Session validateToken(String token) throws DataException, FunctionErrorException, FunctionTimeoutException, RedbackException
	{
		Session session = null;
		DataMap result = request(accessManagementService, "{action:validate, token:\"" + token + "\"}");
		if(result != null  &&  result.getString("result").equals("ok"))
			session = new Session(result.getObject("session"));
		return session;
	}
	
}
