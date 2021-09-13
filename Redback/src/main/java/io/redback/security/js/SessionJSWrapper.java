package io.redback.security.js;

import io.redback.exceptions.RedbackException;
import io.redback.security.Session;
import io.redback.utils.js.CallableJSWrapper;
import io.redback.utils.js.ObjectJSWrapper;

public class SessionJSWrapper extends ObjectJSWrapper
{
	protected Session session;
	
	public SessionJSWrapper(Session s)
	{
		super(new String[] {"userProfile", "getToken", "expiry", "getUserProfile", "timezone"});
		session = s;
	}

	public Object get(String key) {
		if(key.equals("getUserProfile")) {
			return new CallableJSWrapper() {
				public Object call(Object... arguments) throws RedbackException {
					return new UserProfileJSWrapper(session.getUserProfile());
				}
			};
		} else if(key.equals("getToken")) {
			return new CallableJSWrapper() {
				public Object call(Object... arguments) throws RedbackException {
					return session.getToken();
				}
			};
		} else if(key.equals("userProfile")) {
			return new UserProfileJSWrapper(session.getUserProfile());
		} else if(key.equals("expiry")) {
			return session.getUserProfile().getExpiry();
		} else if(key.equals("timezone")) {
			return session.getTimezone();
		} else {
			return null;
		}
	}
}
