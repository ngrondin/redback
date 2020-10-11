package io.redback.managers.domainmanager.js;


import org.graalvm.polyglot.Value;
import org.graalvm.polyglot.proxy.ProxyExecutable;

import io.redback.utils.js.JSConverter;

public class DomainLoggerJS implements ProxyExecutable {
	protected StringBuilder sb;
	
	public DomainLoggerJS() {
		sb = new StringBuilder();
	}
	
	public Object execute(Value... arguments) {
		Object val = JSConverter.toJava(arguments[0]);
		sb.append(val.toString());
		sb.append("\r\n");
		return null;
	}

	
	public void log(String s) {
		sb.append(s);
		sb.append("\r\n");
	}
	
	public String getLog() {
		return sb.toString();
	}
}
