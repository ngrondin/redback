package io.redback.security.js;

import java.util.Arrays;
import java.util.HashSet;

import org.graalvm.polyglot.Value;
import org.graalvm.polyglot.proxy.ProxyExecutable;
import org.graalvm.polyglot.proxy.ProxyObject;

import io.redback.security.UserProfile;

public class UserProfileJSWrapper implements ProxyObject
{
	protected UserProfile userProfile;
	protected String[] members = {"username", "getAttributes", "getRights", "canRead", "canWrite", "canExecute"};
	
	
	public UserProfileJSWrapper(UserProfile up)
	{
		userProfile = up;
	}

	public Object getMember(String key) {
		if(key.equals("username")) {
			return new ProxyExecutable() {
				public Object execute(Value... arguments) {
					return userProfile.getUsername();
				}
			};
		} else if(key.equals("getAttribute")) {
			return new ProxyExecutable() {
				public Object execute(Value... arguments) {
					return userProfile.getAttribute(arguments[0].asString());
				}
			};
		} else if(key.equals("getRights")) {
			return new ProxyExecutable() {
				public Object execute(Value... arguments) {
					return userProfile.getRights(arguments[0].asString());
				}
			};
		} else if(key.equals("canRead")) {
			return new ProxyExecutable() {
				public Object execute(Value... arguments) {
					return userProfile.canRead(arguments[0].asString());
				}
			};
		} else if(key.equals("canWrite")) {
			return new ProxyExecutable() {
				public Object execute(Value... arguments) {
					return userProfile.canWrite(arguments[0].asString());
				}
			};
		} else if(key.equals("canExecute")) {
			return new ProxyExecutable() {
				public Object execute(Value... arguments) {
					return userProfile.canExecute(arguments[0].asString());
				}
			};
		} else {
			return null;
		}
	}

	public Object getMemberKeys() {
		return new HashSet<>(Arrays.asList(members));
	}

	public boolean hasMember(String key) {
		
		return Arrays.asList(members).contains(key);
	}

	public void putMember(String key, Value value) {
	
	}

}
