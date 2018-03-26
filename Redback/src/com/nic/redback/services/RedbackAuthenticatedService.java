package com.nic.redback.services;

import java.util.logging.Logger;

import com.nic.firebus.Payload;
import com.nic.firebus.exceptions.FunctionErrorException;
import com.nic.firebus.exceptions.FunctionTimeoutException;
import com.nic.firebus.utils.JSONException;
import com.nic.firebus.utils.JSONObject;
import com.nic.redback.RedbackException;
import com.nic.redback.RedbackService;
import com.nic.redback.security.Session;

public abstract class RedbackAuthenticatedService extends RedbackService
{
	private Logger logger = Logger.getLogger("com.nic.redback");
	protected String accessManagementService;

	public RedbackAuthenticatedService(JSONObject c)
	{
		super(c);
		accessManagementService = config.getString("accessmanagementservice");
	}
	
	public Payload service(Payload payload) throws FunctionErrorException
	{
		logger.info("Authenticated service start");
		try
		{
			Payload response = null;
			//JSONObject responseData = null;
			Session session = null;
			String sessionId = payload.metadata.get("sessionid");
			String username = null;//equest.getString("username");
			String password = null;//request.getString("password");
			
			if(payload.getString().length() > 0)
			{
				try
				{
					JSONObject request = new JSONObject(payload.getString());
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
				//responseData = new JSONObject("{\"authenticationerror\":\"Not logged in or invalid username or password\"}");
				//response.setData(responseData.toString());
			}
			logger.info("Authenticated service finish");
			return response;
		}
		catch(RedbackException | JSONException | FunctionTimeoutException e)
		{
			String errorMsg = buildErrorMessage(e);
			logger.severe(errorMsg);
			logger.severe(getStackTrace(e));
			throw new FunctionErrorException(errorMsg);
			//response.setData("{\r\n\t\"generalerror\":\"" + errorMsg + "\"\r\n}");
		}

	}

	public abstract Payload authenticatedService(Session session, Payload payload) throws FunctionErrorException;

	public abstract Payload unAuthenticatedService(Session session, Payload payload) throws FunctionErrorException;
	
	protected Session authenticate(String username, String password) throws JSONException, FunctionErrorException, FunctionTimeoutException, RedbackException
	{
		Session session = null;
		JSONObject result = request(accessManagementService, "{action:authenticate, username:\"" + username + "\", password:\"" + password + "\"}");
		if(result != null  &&  result.getString("result").equals("ok"))
			session = new Session(result.getObject("session"));
		return session;
	}
	
	protected Session validateSession(String sessionid) throws JSONException, FunctionErrorException, FunctionTimeoutException, RedbackException
	{
		Session session = null;
		JSONObject result = request(accessManagementService, "{action:validate, sessionid:\"" + sessionid + "\"}");
		if(result != null  &&  result.getString("result").equals("ok"))
			session = new Session(result.getObject("session"));
		return session;
	}
}
