package io.redback.managers.processmanager;

import java.util.Map;

import io.firebus.data.DataMap;
import io.redback.exceptions.RedbackException;
import io.redback.managers.jsmanager.Expression;
import io.redback.utils.StringUtils;

public class AssigneeConfig
{
	protected ProcessManager processManager;
	protected int assigneeType;
	protected String assigneeStr;
	protected Expression assigneeExpr;
	
	public static int USER = 1;
	public static int GROUP = 2;
	public static int PROCESS = 3;
	
	/*
	public AssigneeConfig(ProcessManager pm, int at, String a) throws RedbackException
	{
		processManager = pm;
		assigneeType = at;
		assigneeStr = a;
		assigneeExpr = new Expression(processManager.getJSManager(), "pm_assignee_" + StringUtils.base16(a.hashCode()), pm.getScriptVariableNames(), a);
	}
	*/
	
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
		assigneeExpr = new Expression(processManager.getJSManager(), "pm_assignee_" + StringUtils.base16(assigneeStr.hashCode()), pm.getScriptVariableNames(), assigneeStr);
	}
	
	public Object evaluateId(Map<String, Object> context) throws RedbackException
	{
		return assigneeExpr.eval(context);
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
