package io.redback.security;

import io.firebus.utils.DataMap;

public class CachedUserProfile extends UserProfile {

	public long expiry;
	
	public CachedUserProfile(DataMap p, long exp) {
		super(p);
		expiry = exp;
	}

}
