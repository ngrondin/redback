package io.redback.managers.domainmanager.js;


import org.graalvm.polyglot.Value;
import org.graalvm.polyglot.proxy.ProxyExecutable;

import io.redback.managers.domainmanager.DomainFunction;
import io.redback.managers.domainmanager.DomainLogger;
import io.redback.managers.domainmanager.DomainManager;
import io.redback.security.Session;
import io.redback.utils.js.JSConverter;

public class DomainLoggerJS extends DomainLogger implements ProxyExecutable {
	
	public DomainLoggerJS(Session s, DomainManager dm, DomainFunction df) {
		super(s, dm, df);
	}
	
	public Object execute(Value... arguments) {
		Object val = JSConverter.toJava(arguments[0]);
		sb.append(val.toString());
		sb.append("\r\n");
		return null;
	}

}
