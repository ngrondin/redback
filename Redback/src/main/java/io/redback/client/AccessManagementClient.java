package io.redback.client;

import io.firebus.Firebus;
import io.firebus.utils.DataMap;
import io.redback.RedbackException;
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

	public UserProfile validate(Session session, String token) throws RedbackException
	{
		UserProfile userProfile = cachedUserProfiles.get(token);
		
		if(userProfile == null) {
			try {
				DataMap req = new DataMap();
				req.put("action", "validate");
				req.put("token", token);
				DataMap resp = request(session, req);
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
}
