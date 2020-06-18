package io.redback.utils.js;

import java.util.Arrays;
import java.util.HashSet;

import org.graalvm.polyglot.Value;
import org.graalvm.polyglot.proxy.ProxyArray;
import org.graalvm.polyglot.proxy.ProxyExecutable;
import org.graalvm.polyglot.proxy.ProxyObject;

import io.firebus.utils.DataEntity;
import io.firebus.utils.DataMap;
import io.redback.services.impl.RedbackUIServer;
import io.redback.utils.StringUtils;

public class RedbackUtilsJSWrapper implements ProxyObject
{
	protected String[] members = {"convertDataEntityToAttributeString", "convertDataMapToAttributeString", "convertFilterForClient"};
	
	public RedbackUtilsJSWrapper() {
	}
	
	public Object getMember(String key) {
		if(key.equals("convertDataEntityToAttributeString")) {
			return new ProxyExecutable() {
				public Object execute(Value... arguments) {
					return StringUtils.convertDataEntityToAttributeString((DataEntity)JSConverter.toJava(arguments[0]));
				}
			};
		} else if(key.equals("convertDataMapToAttributeString")) {
			return new ProxyExecutable() {
				public Object execute(Value... arguments) {
					return StringUtils.convertDataEntityToAttributeString((DataMap)JSConverter.toJava(arguments[0]));
				}
			};
		} else if(key.equals("convertFilterForClient")) {
			return new ProxyExecutable() {
				public Object execute(Value... arguments) {
					return RedbackUIServer.convertFilter((DataMap)JSConverter.toJava(arguments[0]));
				}
			};
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
