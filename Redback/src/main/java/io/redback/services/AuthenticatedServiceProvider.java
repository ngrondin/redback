package io.redback.services;

import java.util.logging.Logger;

import io.firebus.Firebus;
import io.firebus.Payload;
import io.firebus.exceptions.FunctionErrorException;
import io.firebus.exceptions.FunctionTimeoutException;
import io.firebus.interfaces.ServiceProvider;
import io.firebus.utils.DataException;
import io.firebus.utils.DataMap;
import io.redback.RedbackException;
import io.redback.client.AccessManagementClient;
import io.redback.security.Session;
import io.redback.utils.StringUtils;

public abstract class AuthenticatedServiceProvider extends Service implements ServiceProvider
{
	private Logger logger = Logger.getLogger("io.redback");
	protected String accessManagementService;
	protected AccessManagementClient accessManagementClient;

	public AuthenticatedServiceProvider(String n, DataMap c, Firebus f)
	{
		super(n, c, f);
		accessManagementService = config.getString("accessmanagementservice");
		accessManagementClient = new AccessManagementClient(firebus, accessManagementService);
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
				session = accessManagementClient.validate(token);
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
			//String errorMsg = buildErrorMessage(e);
			//logger.severe(errorMsg);
			logger.severe(StringUtils.getStackTrace(e));
			throw new FunctionErrorException("Exception in authenticated service '" + serviceName + "'", e);
			//throw new FunctionErrorException(errorMsg);
		}

	}

	public abstract Payload authenticatedService(Session session, Payload payload) throws RedbackException;

	public abstract Payload unAuthenticatedService(Session session, Payload payload) throws RedbackException;

	/*
	protected Session validateToken(String token) throws DataException, FunctionErrorException, FunctionTimeoutException, RedbackException
	{
		Session session = null;
		session = accessManagementClient.validate(token);
		//DataMap result = request(accessManagementService, "{action:validate, token:\"" + token + "\"}");
		//if(result != null  &&  result.getString("result").equals("ok"))
		//	session = new Session(result.getObject("session"));
		return session;
	}
	*/
	
}
