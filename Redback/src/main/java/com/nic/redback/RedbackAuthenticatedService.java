package com.nic.redback;

import java.util.logging.Logger;

import com.nic.firebus.Firebus;
import com.nic.firebus.Payload;
import com.nic.firebus.exceptions.FunctionErrorException;
import com.nic.firebus.exceptions.FunctionTimeoutException;
import com.nic.firebus.utils.DataException;
import com.nic.firebus.utils.DataMap;
import com.nic.redback.security.Session;

public abstract class RedbackAuthenticatedService extends RedbackDataService
{
	private Logger logger = Logger.getLogger("com.nic.redback");
	protected String accessManagementService;

	public RedbackAuthenticatedService(Firebus f, DataMap c)
	{
		super(f, c);
		accessManagementService = config.getString("accessmanagementservice");
	}
	
	public Payload service(Payload payload) throws FunctionErrorException
	{
		logger.finer("Authenticated service start");
		try
		{
			Payload response = null;
			Session session = null;
			String sessionId = payload.metadata.get("sessionid");
			String username = null;//equest.getString("username");
			String password = null;//request.getString("password");
			
			if(payload.getString().length() > 0)
			{
				try
				{
					DataMap request = new DataMap(payload.getString());
					username = request.getString("username");
					password = request.getString("password");
				}
				catch(Exception e)	{}
			}

			
			if(username != null  &&  password != null)
			{
				session = authenticate(username, password);
			}
			else if(sessionId != null)
			{
				session = validateSession(sessionId);
			}

			if(session != null)
			{			
				response = authenticatedService(session, payload);
				response.metadata.put("sessionid", session.getSessionId().toString());
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
	
	protected Session authenticate(String username, String password) throws DataException, FunctionErrorException, FunctionTimeoutException, RedbackException
	{
		Session session = null;
		DataMap result = request(accessManagementService, "{action:authenticate, username:\"" + username + "\", password:\"" + password + "\"}");
		if(result != null  &&  result.getString("result").equals("ok"))
			session = new Session(result.getObject("session"));
		return session;
	}
	
	protected Session validateSession(String sessionid) throws DataException, FunctionErrorException, FunctionTimeoutException, RedbackException
	{
		Session session = null;
		DataMap result = request(accessManagementService, "{action:validate, sessionid:\"" + sessionid + "\"}");
		if(result != null  &&  result.getString("result").equals("ok"))
			session = new Session(result.getObject("session"));
		return session;
	}
	
	protected void logout(Session session) throws  DataException, FunctionErrorException, FunctionTimeoutException, RedbackException
	{
		DataMap result = request(accessManagementService, "{action:logout, sessionid:\"" + session.getSessionId().toString() + "\"}");
		if(result == null  ||  (result != null  &&  !result.getString("result").equals("ok")))
			throw new RedbackException("Counld not log out the session");		
	}
}
