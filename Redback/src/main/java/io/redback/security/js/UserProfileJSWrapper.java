package io.redback.security.js;

import io.firebus.data.DataMap;
import io.redback.exceptions.RedbackException;
import io.redback.security.UserProfile;
import io.redback.utils.js.CallableJSWrapper;
import io.redback.utils.js.ObjectJSWrapper;

public class UserProfileJSWrapper extends ObjectJSWrapper
{
	protected UserProfile userProfile;
	
	public UserProfileJSWrapper(UserProfile up)
	{
		super(new String[] {"username", "getAttribute", "getRights", "canRead", "canWrite", "canExecute", "getUsername"});
		userProfile = up;
	}

	public Object get(String key) {
		if(key.equals("getUsername")) {
			return new CallableJSWrapper() {
				public Object call(Object... arguments) throws RedbackException {
					return userProfile.getUsername();
				}
			};
		} else if(key.equals("getRoles")) {
			return new CallableJSWrapper() {
				public Object call(Object... arguments) throws RedbackException {
					return userProfile.getRoles();
				}
			};			
		} else if(key.equals("getAttribute")) {
			return new CallableJSWrapper() {
				public Object call(Object... arguments) throws RedbackException {
					return userProfile.getAttribute((String)arguments[0]);
				}
			};
		} else if(key.equals("getRights")) {
			return new CallableJSWrapper() {
				public Object call(Object... arguments) throws RedbackException {
					return userProfile.getRights((String)arguments[0], (String)arguments[1], (DataMap)(arguments[2]));
				}
			};
		} else if(key.equals("canRead")) {
			return new CallableJSWrapper() {
				public Object call(Object... arguments) throws RedbackException {
					return userProfile.canRead((String)arguments[0]);
				}
			};
		} else if(key.equals("canWrite")) {
			return new CallableJSWrapper() {
				public Object call(Object... arguments) throws RedbackException {
					return userProfile.canWrite((String)arguments[0]);
				}
			};
		} else if(key.equals("canExecute")) {
			return new CallableJSWrapper() {
				public Object call(Object... arguments) throws RedbackException {
					return userProfile.canExecute((String)arguments[0]);
				}
			};
		} else if(key.equals("username")) {
			return userProfile.getUsername();
		} else {
			return null;
		}
	}

}
