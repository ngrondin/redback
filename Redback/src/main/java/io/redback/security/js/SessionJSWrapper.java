package io.redback.security.js;

import java.util.Arrays;
import java.util.HashSet;

import org.graalvm.polyglot.Value;
import org.graalvm.polyglot.proxy.ProxyArray;
import org.graalvm.polyglot.proxy.ProxyExecutable;
import org.graalvm.polyglot.proxy.ProxyObject;

import io.redback.security.Session;

public class SessionJSWrapper implements ProxyObject
{
	protected Session session;
	protected String[] members = {"userProfile", "getUserProfile"};
	
	public SessionJSWrapper(Session s)
	{
		session = s;
	}

	public Object getMember(String key) {
		if(key.equals("getUserProfile")) {
			return new ProxyExecutable() {
				public Object execute(Value... arguments) {
					return new UserProfileJSWrapper(session.getUserProfile());
				}
			};
		} else if(key.equals("userProfile")) {
			return new UserProfileJSWrapper(session.getUserProfile());
		} else {
			return null;
		}
	}

	public Object getMemberKeys() {
		return ProxyArray.fromArray(((Object[])members));		
	}

	public boolean hasMember(String key) {
		
		return Arrays.asList(members).contains(key);
	}

	public void putMember(String key, Value value) {
	
	}

}
