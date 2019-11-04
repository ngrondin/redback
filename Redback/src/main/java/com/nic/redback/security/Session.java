package com.nic.redback.security;

import com.nic.firebus.utils.DataMap;


public class Session
{
	//public UUID sessionId;
	public String token;
	public UserProfile userProfile;
	public long expiry;

	public Session(String t, UserProfile up, long e)
	{
		token = t;
		userProfile = up;
		expiry = e;
	}
	
	/*
	public Session(UUID si, UserProfile up, long e)
	{
		sessionId = si;
		userProfile = up;
		expiry = e;
	}
	*/
	
	public Session(DataMap json)
	{
		//sessionId = UUID.fromString(json.getString("sessionid"));
		token = json.getString("token");
		expiry = ((Double)json.getNumber("expiry")).longValue();
		userProfile = new UserProfile(json.getObject("userprofile"));
	}
	
	
	/*
	public UUID getSessionId()
	{
		return sessionId;
	}
	*/
	
	public String getToken()
	{
		return token;
	}
	
	public UserProfile getUserProfile()
	{
		return userProfile;
	}
	
	public DataMap getJSON()
	{
		DataMap resp = new DataMap();
		//resp.put("sessionid", sessionId.toString());
		resp.put("token", token);
		resp.put("expiry", expiry);
		resp.put("userprofile", userProfile.getJSON());
		return resp;
	}
}
