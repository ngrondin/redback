package io.redback.security;

import io.firebus.data.DataMap;

public class CachedUserProfile extends UserProfile {

	public long expiry;
	
	public CachedUserProfile(DataMap p, long exp) {
		super(p);
		expiry = exp;
	}

}
