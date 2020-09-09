package io.redback.security.js;

import java.util.Arrays;

import org.graalvm.polyglot.Value;
import org.graalvm.polyglot.proxy.ProxyArray;
import org.graalvm.polyglot.proxy.ProxyExecutable;
import org.graalvm.polyglot.proxy.ProxyObject;

import io.firebus.utils.DataMap;
import io.redback.security.UserProfile;
import io.redback.utils.js.JSConverter;

public class UserProfileJSWrapper implements ProxyObject
{
	protected UserProfile userProfile;
	protected String[] members = {"username", "getAttribute", "getRights", "canRead", "canWrite", "canExecute", "getUsername"};
	
	
	public UserProfileJSWrapper(UserProfile up)
	{
		userProfile = up;
	}

	public Object getMember(String key) {
		if(key.equals("getUsername")) {
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
					return userProfile.getRights(arguments[0].asString(), arguments[1].asString(), (DataMap)JSConverter.toJava(arguments[2]));
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
		} else if(key.equals("username")) {
			return userProfile.getUsername();
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
