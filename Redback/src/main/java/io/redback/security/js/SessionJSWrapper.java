package io.redback.security.js;

import io.firebus.script.Function;
import io.firebus.script.exceptions.ScriptException;
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
		} else if(key.equals("fordomain")) {
			return new CallableJSWrapper() {
				public Object call(Object... arguments) throws RedbackException {
					if(arguments.length >= 2 && arguments[1] instanceof Function) {
						String domain = arguments[0].toString();
						Function function = (Function)arguments[1];
						if(session.userProfile.hasDomain(domain) && session.getDomainLock() == null) {
							session.setDomainLock(domain);
							try {
								function.call();
							} catch(ScriptException e) {
								throw new RedbackException("Error in fordomain", e);
							}							
							session.setDomainLock(null);
						}
						return null;
					} else {
						throw new RedbackException("Requires an executable argument");
					}
				}
			};				
		} else {
			return null;
		}
	}
}
