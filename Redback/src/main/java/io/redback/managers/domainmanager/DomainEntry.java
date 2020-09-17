package io.redback.managers.domainmanager;

import io.firebus.utils.DataMap;

public class DomainEntry {
	protected DataMap config;
	protected String name;
	protected String domain;
	protected String type;
	protected boolean _canCache;
	
	public DomainEntry(DataMap c) {
		config = c;
		name = config.getString("name");
		domain = config.getString("domain");
		type = config.getString("type");
		_canCache = config.containsKey("cancache") ? config.getBoolean("cancache") : true; 
	}
	
	public String getName() {
		return name;
	}
	
	public String getDomain() {
		return domain;
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
