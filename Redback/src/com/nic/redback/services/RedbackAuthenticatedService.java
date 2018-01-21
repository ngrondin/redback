package com.nic.redback.services;

import com.nic.firebus.exceptions.FunctionErrorException;
import com.nic.firebus.exceptions.FunctionTimeoutException;
import com.nic.firebus.utils.JSONException;
import com.nic.firebus.utils.JSONObject;
import com.nic.redback.RedbackException;
import com.nic.redback.RedbackService;
import com.nic.redback.security.Session;

public abstract class RedbackAuthenticatedService extends RedbackService
{

	protected String accessManagementService;

	public RedbackAuthenticatedService(JSONObject c)
	{
		super(c);
		accessManagementService = config.getString("accessmanagementservice");
	}

	
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
