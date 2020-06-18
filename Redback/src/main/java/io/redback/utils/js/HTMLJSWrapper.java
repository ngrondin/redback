package io.redback.utils.js;

import java.util.Arrays;
import java.util.HashSet;

import org.graalvm.polyglot.Value;
import org.graalvm.polyglot.proxy.ProxyArray;
import org.graalvm.polyglot.proxy.ProxyExecutable;
import org.graalvm.polyglot.proxy.ProxyObject;

import io.redback.utils.HTML;

public class HTMLJSWrapper implements ProxyObject
{
	protected HTML html;
	protected String[] members = {"append", "toString"};
	
	public HTMLJSWrapper(HTML h) {
		html = h;
	}
	
	public Object getMember(String key) {
		if(key.equals("append")) {
			return new ProxyExecutable() {
				public Object execute(Value... arguments) {
					html.append(JSConverter.toJava(arguments[0]));
					return null;
				}
			};
		} else if(key.equals("toString")) {
			return new ProxyExecutable() {
				public Object execute(Value... arguments) {
					return html.toString();
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
