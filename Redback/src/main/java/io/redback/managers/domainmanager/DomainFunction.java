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
	
	public DomainFunction(DomainManager dm, JSManager jsm, DataMap c) throws RedbackException {
		super(c);
		jsManager = jsm;
		domainManager = dm;
		List<String> params = Arrays.asList(new String[] {"session", "dm", "om", "fm"});
		function = new Function(jsManager, "domain_" + config.getString("domain") + "_" + config.getString("name"), params, config.getString("function"));
	}

	public void execute(Map<String, Object> context) throws RedbackException {
		function.execute(context);
	}
}