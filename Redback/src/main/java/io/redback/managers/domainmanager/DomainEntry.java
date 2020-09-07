package io.redback.managers.domainmanager;

import io.firebus.utils.DataMap;

public class DomainEntry {
	protected DataMap config;
	protected String name;
	protected String type;
	protected boolean _canCache;
	
	public DomainEntry(DataMap c) {
		config = c;
		name = config.getString("name");
		type = config.getString("type");
		_canCache = config.getBoolean("cancache"); 
	}
	
	public String getName() {
		return name;
	}
	
	public String getType() {
		return type;
	}
	
	public boolean canCache() {
		return _canCache;
	}
	
	public DataMap getConfig() {
		return config;
	}
}
