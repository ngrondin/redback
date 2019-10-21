package com.nic.redback.services.processserver;

import com.nic.firebus.utils.DataMap;
import com.nic.redback.RedbackException;

public class AssigneeConfig
{
	protected int assigneeType;
	protected String assigneeStr;
	protected Expression assigneeExpr;
	
	public static int USER = 1;
	public static int GROUP = 2;
	public static int PROCESS = 3;
	
	public AssigneeConfig(int at, String a) throws RedbackException
	{
		assigneeType = at;
		assigneeStr = a;
		assigneeExpr = new Expression(a);
	}
	
	public AssigneeConfig(DataMap c) throws RedbackException
	{
		String atStr = c.getString("type");
		if(atStr.equals("user"))
			assigneeType = USER;
		else if(atStr.equals("group"))
			assigneeType = GROUP;
		else if(atStr.equals("process"))
			assigneeType = PROCESS;
		assigneeStr = c.getString("id");
		assigneeExpr = new Expression(assigneeStr);
	}
	
	public Object evaluateId(ProcessInstance pi) throws RedbackException
	{
		return assigneeExpr.eval("data", pi.getData());
	}
	
	public int getType()
	{
		return assigneeType;
	}
	
	public DataMap getJSON()
	{
		DataMap retVal = new DataMap();
		retVal.put("id", assigneeStr);
		if(assigneeType == USER)
			retVal.put("type", "user");
		else if(assigneeType == GROUP)
			retVal.put("type", "group");
		else if(assigneeType == PROCESS)
			retVal.put("type", "process");
		return retVal;
	}
	
}
