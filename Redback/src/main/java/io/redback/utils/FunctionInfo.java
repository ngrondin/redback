package io.redback.utils;

public class FunctionInfo {
	public String name;
	public String description;
	public String showExpression;
	public long timeout;
	
	public FunctionInfo(String n, String d, long to) {
		name = n;
		description = d;
		showExpression = null;
		timeout = to;
	}
	
	public FunctionInfo(String n, String d, String se, long to) {
		name = n;
		description = d;
		showExpression = se;
		timeout = to;
	}

}