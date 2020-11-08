package io.redback.client;

import java.util.HashMap;
import java.util.Map;

import io.firebus.Firebus;
import io.firebus.utils.DataMap;
import io.redback.RedbackException;
import io.redback.security.Session;
import io.redback.security.UserProfile;
import io.redback.utils.CacheEntry;

public class AccessManagementClient extends Client
{
	protected Map<String, CacheEntry<UserProfile>> cachedUserProfiles;


	public AccessManagementClient(Firebus fb, String sn) 
	{
		super(fb, sn);
		cachedUserProfiles = new HashMap<String, CacheEntry<UserProfile>>();
	}

	public UserProfile validate(Session session, String token) throws RedbackException
	{
		CacheEntry<UserProfile> ce = cachedUserProfiles.get(token);
		if(ce != null && ce.hasExpired()) {
			cachedUserProfiles.remove(token);
			ce = null;
		}
		
		if(ce == null) {
			try {
				DataMap req = new DataMap();
				req.put("action", "validate");
				req.put("token", token);
				DataMap resp = request(session, req);
				if(resp != null  &&  resp.getString("result").equals("ok")) {
					UserProfile up = new UserProfile(resp.getObject("userprofile"));
					cachedUserProfiles.put(token, new CacheEntry<UserProfile>(up, System.currentTimeMillis() + 120000));
					return up;
				} else {
					throw new RedbackException("Token cannot be validated");
				}
			} catch(Exception e) {
				throw new RedbackException("Error validating token with access manager", e);
			}
		} else {
			return ce.get();
		}
	}


}
