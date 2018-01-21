package com.nic.redback.security;

import java.util.UUID;

import com.nic.firebus.utils.JSONObject;


public class Session
{
	public UUID sessionId;
	public UserProfile userProfile;
	public long expiry;
	
	public Session(UUID si, UserProfile up, long e)
	{
		sessionId = si;
		userProfile = up;
		expiry = e;
	}
	
	public Session(JSONObject json)
	{
		sessionId = UUID.fromString(json.getString("sessionid"));
		expiry = ((Double)json.getNumber("expiry")).longValue();
		userProfile = new UserProfile(json.getObject("userprofile"));
	}
	
	public UUID getSessionId()
	{
		return sessionId;
	}
	
	public UserProfile getUserProfile()
	{
		return userProfile;
	}
	
	public JSONObject getJSON()
	{
		JSONObject resp = new JSONObject();
		resp.put("sessionid", sessionId.toString());
		resp.put("expiry", expiry);
		resp.put("userprofile", userProfile.getJSON());
		return resp;
	}
}
