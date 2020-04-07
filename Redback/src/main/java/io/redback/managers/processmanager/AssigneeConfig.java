package io.redback.managers.processmanager;

import io.firebus.utils.DataMap;
import io.redback.RedbackException;
import io.redback.utils.Expression;

public class AssigneeConfig
{
	protected ProcessManager processManager;
	protected int assigneeType;
	protected String assigneeStr;
	protected Expression assigneeExpr;
	
	public static int USER = 1;
	public static int GROUP = 2;
	public static int PROCESS = 3;
	
	public AssigneeConfig(ProcessManager pm, int at, String a) throws RedbackException
	{
		processManager = pm;
		assigneeType = at;
		assigneeStr = a;
		assigneeExpr = new Expression(processManager.getScriptEngine(), a);
	}
	
	public AssigneeConfig(ProcessManager pm, DataMap c) throws RedbackException
	{
		processManager = pm;
		String atStr = c.getString("type");
		if(atStr.equals("user"))
			assigneeType = USER;
		else if(atStr.equals("group"))
			assigneeType = GROUP;
		else if(atStr.equals("process"))
			assigneeType = PROCESS;
		assigneeStr = c.getString("id");
		assigneeExpr = new Expression(processManager.getScriptEngine(), assigneeStr);
	}
	
	public Object evaluateId(ProcessInstance pi) throws RedbackException
	{
		return assigneeExpr.eval(processManager.createScriptContext(pi));
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
