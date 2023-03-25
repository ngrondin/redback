package io.redback.utils;

public class FunctionInfo {
	public String name;
	public String description;
	public long timeout;
	
	public FunctionInfo(String n, String d, long to) {
		name = n;
		description = d;
		timeout = to;
	}

}