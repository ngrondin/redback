package io.redback.test;

import io.firebus.utils.DataMap;
import io.redback.RedbackException;

public class RedbackAssignmentProxy {
	public DataMap data;
	protected RedbackTestUnit tester;
	protected String username;
	
	public RedbackAssignmentProxy(RedbackTestUnit t, DataMap d, String u) {
		tester = t;
		data = d;
		username = u;
	}
	
	public String getPid()
	{
		return data.getString("pid");
	}
	
	public void action(String action) throws RedbackException {
		tester.actionAssignment(username, getPid(), action);
	}
}
