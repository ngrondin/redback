package com.nic.redback.security;

import java.util.UUID;


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
}
