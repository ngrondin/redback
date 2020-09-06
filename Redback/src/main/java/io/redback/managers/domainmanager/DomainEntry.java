package io.redback.managers.domainmanager;

import io.firebus.utils.DataMap;

public class DomainEntry {
	protected DataMap config;
	protected String name;
	protected boolean _canCache;
	
	public DomainEntry(DataMap c) {
		config = c;
		name = config.getString("name");
		_canCache = config.getBoolean("cancache"); 
	}
	
	public String getName() {
		return name;
	}
	
	public boolean canCache() {
		return _canCache;
	}
	
	public DataMap getConfig() {
		return config;
	}
}
