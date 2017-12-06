package com.nic.redback;

import com.nic.firebus.exceptions.FunctionErrorException;
import com.nic.firebus.exceptions.FunctionTimeoutException;
import com.nic.firebus.utils.JSONException;
import com.nic.firebus.utils.JSONObject;
import com.nic.redback.security.UserProfile;

public abstract class RedbackAuthenticatedService extends RedbackService
{

	protected String accessManagementService;

	public RedbackAuthenticatedService(JSONObject c)
	{
		super(c);
		accessManagementService = config.getString("accessmanagementservice");
	}

	
	protected UserProfile authenticate(String username, String password) throws JSONException, FunctionErrorException, FunctionTimeoutException, RedbackException
	{
		UserProfile userProfile = null;
		JSONObject userProfileJSON = request(accessManagementService, "{action:authenticate, username:\"" + username + "\", password:\"" + password + "\"}");
		if(userProfileJSON != null  &&  userProfileJSON.getString("result").equals("ok"))
			userProfile = new UserProfile(userProfileJSON);
		return userProfile;
	}
	
	protected UserProfile validateSession(String sessionid) throws JSONException, FunctionErrorException, FunctionTimeoutException, RedbackException
	{
		//TODO: Can speed up with caching
		UserProfile userProfile = null;
		JSONObject userProfileJSON = request(accessManagementService, "{action:validate, sessionid:\"" + sessionid + "\"}");
		if(userProfileJSON != null  &&  userProfileJSON.getString("result").equals("ok"))
			userProfile = new UserProfile(userProfileJSON);
		return userProfile;
	}
}
