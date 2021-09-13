package io.redback.managers.processmanager;

import java.util.Map;

import io.firebus.data.DataMap;
import io.firebus.script.Expression;
import io.redback.exceptions.RedbackException;
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
	

	
	public AssigneeConfig(ProcessManager pm, DataMap c) throws RedbackException
	{
		try {
			processManager = pm;
			String atStr = c.getString("type");
			if(atStr.equals("user"))
				assigneeType = USER;
			else if(atStr.equals("group"))
				assigneeType = GROUP;
			else if(atStr.equals("process"))
				assigneeType = PROCESS;
			assigneeStr = c.getString("id");
			assigneeExpr = processManager.getScriptFactory().createExpression("pm_assignee_" + StringUtils.base16(assigneeStr.hashCode()), assigneeStr);
		} catch(Exception e) {
			throw new RedbackException("Error initialising assignee config", e);
		}
	}
	
	public Object evaluateId(Map<String, Object> context) throws RedbackException
	{
		try {
			return assigneeExpr.eval(context);
		} catch(Exception e) {
			throw new RedbackException("Error evaluating assignee expression", e);
		}
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
