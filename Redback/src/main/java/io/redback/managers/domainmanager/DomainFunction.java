package io.redback.managers.domainmanager;

import java.util.Map;

import io.firebus.data.DataMap;
import io.firebus.script.Function;
import io.firebus.script.ScriptFactory;
import io.firebus.script.exceptions.ScriptException;
import io.redback.exceptions.RedbackException;

public class DomainFunction extends DomainEntry {
	protected DomainManager domainManager;
	protected Function function;
	protected long timeout;
	
	public DomainFunction(DomainManager dm, ScriptFactory sf, DataMap c) throws RedbackException {
		super(c);
		domainManager = dm;
		timeout = config.containsKey("timeout") && !config.getString("timeout").equals("") ? config.getNumber("timeout").longValue() : -1;
		String funcName = "domain_" + config.getString("domain") + "_" + config.getString("name");
		String[] params = new String[] {"log", "session", "dm", "oc", "pc", "fc", "nc", "rc", "gc", "geo", "ic", "domain", "param"};
		String source = config.getString("source");
		try {
			function = sf.createFunction(funcName, params, source);
		} catch(ScriptException e) {
			throw new RedbackException("Error compiling domain function", e);
		}
	}

	public Object execute(Map<String, Object> context) throws RedbackException {
		try {
			return function.call(context);
		} catch(ScriptException e) {
			throw new RedbackException("Error executing domain function", e); 
		}
	}
	
	public DomainFunctionInfo getInfo() {
		return new DomainFunctionInfo(this.name, this.description, timeout);
	}
}
