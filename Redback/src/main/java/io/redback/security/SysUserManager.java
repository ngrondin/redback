package io.redback.security;

import io.firebus.data.DataMap;
import io.redback.client.AccessManagementClient;
import io.redback.exceptions.RedbackException;

public class SysUserManager {
	protected DataMap config;
	protected AccessManagementClient accessManagementClient;
	protected UserProfile profile;
	protected String token;
	protected long expiry;

	public SysUserManager(AccessManagementClient amClient, DataMap c) {
		config = c;
		accessManagementClient = amClient;
	}
	
	public String getUsername() {
		if(profile != null)
			return profile.getUsername();
		else 
			return null;
	}
	
	public UserProfile getProfile(Session session) throws RedbackException  {
		retreive(session);
		return profile;
	}
	
	public Session getSession() throws RedbackException {
		return getSession(null);
	}
	
	public Session getSession(String sessionId) throws RedbackException {
		Session session = new Session(sessionId);
		retreive(session);
		session.setUserProfile(profile);
		session.setToken(token);
		return session;
	}
	
	private synchronized void retreive(Session session) throws RedbackException {
		long now = System.currentTimeMillis();
		if(profile == null || token == null || expiry < now + 60000) {
			token = accessManagementClient.getSysUserToken(session);
			profile = accessManagementClient.validate(session, token);
			expiry = profile.getExpiry();
		}
	}
	

}
