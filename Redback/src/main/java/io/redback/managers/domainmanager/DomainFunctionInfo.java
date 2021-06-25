package io.redback.managers.domainmanager;

public class DomainFunctionInfo {
	public String name;
	public String description;
	public long timeout;
	
	public DomainFunctionInfo(String n, String d, long to) {
		name = n;
		description = d;
		timeout = to;
	}

}
