package io.redback.client;

import io.firebus.Firebus;
import io.firebus.Payload;
import io.firebus.data.DataMap;
import io.redback.exceptions.RedbackException;
import io.redback.security.Session;
import io.redback.security.UserProfile;
import io.redback.utils.Cache;

public class AccessManagementClient extends Client
{
	protected Cache<UserProfile> cachedUserProfiles;

	public AccessManagementClient(Firebus fb, String sn) 
	{
		super(fb, sn);
		cachedUserProfiles = new Cache<UserProfile>(120000);
	}

	public Session getSession(Payload payload) throws RedbackException
	{
		Session session = new Session(payload.metadata.get("session"));
		String token = payload.metadata.get("token");
		if(token != null) {
			UserProfile up = validate(session, token);
			if(up != null) {
				session.setUserProfile(up);
				session.setToken(token);
				session.setTimezone(payload.metadata.get("timezone"));
			}
		}
		return session;
	}
	
	public UserProfile validate(Session session, String token) throws RedbackException
	{
		UserProfile userProfile = cachedUserProfiles.get(token);
		
		if(userProfile == null) {
			try {
				DataMap req = new DataMap();
				req.put("action", "validate");
				req.put("token", token);
				DataMap resp = requestDataMap(session, req);
				if(resp != null  &&  resp.getString("result").equals("ok")) {
					userProfile = new UserProfile(resp.getObject("userprofile"));
					cachedUserProfiles.put(token,  userProfile);
				}
			} catch(Exception e) {
				throw new RedbackException("Error validating token with access manager", e);
			}
		}
		return userProfile;
	}
	
	public String getSysUserToken(Session session) throws RedbackException
	{
		try {
			DataMap req = new DataMap();
			req.put("action", "getsysuser");
			DataMap resp = requestDataMap(session, req);
			String token = resp.getString("token");
			return token;
		} catch(Exception e) {
			throw new RedbackException("Error validating token with access manager", e);
		}
	}
	
}
