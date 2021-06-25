package io.redback.managers.domainmanager;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import io.firebus.utils.DataMap;
import io.redback.RedbackException;
import io.redback.managers.jsmanager.Function;
import io.redback.managers.jsmanager.JSManager;

public class DomainFunction extends DomainEntry {
	protected JSManager jsManager;
	protected DomainManager domainManager;
	protected Function function;
	protected long timeout;
	
	public DomainFunction(DomainManager dm, JSManager jsm, DataMap c) throws RedbackException {
		super(c);
		jsManager = jsm;
		domainManager = dm;
		String funcName = "domain_" + config.getString("domain") + "_" + config.getString("name");
		List<String> params = Arrays.asList(new String[] {"log", "session", "dm", "oc", "pc", "fc", "nc", "rc", "gc", "geo", "ic", "domain", "param"});
		function = new Function(jsManager, funcName, params, config.getString("source"));
		timeout = config.containsKey("timeout") && !config.getString("timeout").equals("") ? config.getNumber("timeout").longValue() : -1;
	}

	public Object execute(Map<String, Object> context) throws RedbackException {
		return function.execute(context);
	}
	
	public DomainFunctionInfo getInfo() {
		return new DomainFunctionInfo(this.name, this.description, timeout);
	}
}
