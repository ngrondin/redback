package io.redback.utils.js;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.Date;

import org.graalvm.polyglot.Value;
import org.graalvm.polyglot.proxy.ProxyArray;
import org.graalvm.polyglot.proxy.ProxyExecutable;
import org.graalvm.polyglot.proxy.ProxyObject;

import io.firebus.data.ZonedTime;

public class JSZonedTime implements ProxyObject {
	protected String[] members = {"atDate", "toString"};
	protected ZonedTime zonedTime;
	
	public JSZonedTime(ZonedTime zt) {
		zonedTime = zt;
	}
	
	public ZonedTime getTime() {
		return zonedTime;
	}
	
	public Object getMember(String key) {
		if(key.equals("atDate")) {
			return new ProxyExecutable() {
				public Object execute(Value... arguments) {
					Object inDateObj = JSConverter.toJava(arguments[0]);
					if(inDateObj instanceof Date) {
						Date inDate = (Date)inDateObj;
						ZonedDateTime inZDT = ZonedDateTime.ofInstant(Instant.ofEpochMilli(inDate.getTime()), ZoneId.systemDefault());
						ZonedDateTime outZDT = zonedTime.atDate(inZDT);
						Date outDate = Date.from(outZDT.toInstant());
						return JSConverter.toJS(outDate);	
					} else {
						throw new RuntimeException("Not a valid date");
					}
				}
			};			
		} else if(key.equals("toString")) {
			return new ProxyExecutable() {
				public Object execute(Value... arguments) {
					return zonedTime.toString();
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
	
	public String toString() {
		return zonedTime.toString();
	}


}
