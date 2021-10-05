package io.redback.managers.domainmanager.js;

import io.redback.exceptions.RedbackException;
import io.redback.managers.domainmanager.DomainLogger;
import io.redback.utils.js.CallableJSWrapper;

public class DomainLoggerJS extends CallableJSWrapper {
	protected DomainLogger logger;
	
	public DomainLoggerJS(DomainLogger l) {
		logger = l;
	}
	
	public Object call(Object... arguments) throws RedbackException {
		Object val = arguments[0];
		if(val != null)
			logger.log(val.toString());
		else
			logger.log("null");
		logger.log("\r\n");
		return null;
	}

}
