package io.redback.utils.js;

import java.time.Instant;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;

import org.graalvm.polyglot.Value;
import org.graalvm.polyglot.proxy.ProxyExecutable;
import org.graalvm.polyglot.proxy.ProxyInstant;
import org.graalvm.polyglot.proxy.ProxyObject;

public class JSDate implements ProxyObject, ProxyInstant {
	protected String[] members = {"getTime"};
	protected Date date;
	
	public JSDate(Date dt) {
		date = dt;
	}
	
	public Instant asInstant() {
		return date.toInstant();
	}

	public Object getMember(String key) {
		if(key.equals("getTime")) {
			return new ProxyExecutable() {
				public Object execute(Value... arguments) {
					return date.getTime();
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
	
	public String toString() {
		return date.toString();
	}


}
