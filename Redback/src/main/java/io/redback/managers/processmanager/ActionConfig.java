package io.redback.managers.processmanager;

import io.firebus.utils.DataMap;
import io.redback.RedbackException;
import io.redback.managers.jsmanager.Expression;
import io.redback.utils.StringUtils;

public class ActionConfig {

	protected ProcessManager processManager;
	protected String actionDescription;
	protected String actionName;
	protected String nextNode;
	protected String exclusiveStr;
	protected Expression exclusiveExpr;
	
	
	public ActionConfig(ProcessManager pm, Process p, ProcessUnit pu, DataMap c) throws RedbackException
	{
		processManager = pm;
		actionName = c.getString("action");
		actionDescription = c.getString("description");
		nextNode = c.getString("nextnode");
		if(c.containsKey("exclusive")) {
			exclusiveStr = c.getString("exclusive");
			exclusiveExpr = new Expression(processManager.getJSManager(), p.getName() + "_node_" + StringUtils.base16(pu.getId().hashCode()) + "_action_" + StringUtils.base16(c.hashCode()), pm.getScriptVariableNames(), exclusiveStr);
		}
	}
	
	public String getActionName()
	{
		return actionName;
	}
	
	public String getActionDescription()
	{
		return actionDescription;
	}
	
	public String getNextNode()
	{
		return nextNode;
	}
	
	public boolean isExclusive()
	{
		return exclusiveExpr != null;
	}
	
	public Object evaluateExclusiveId(ProcessInstance pi) throws RedbackException
	{
		return exclusiveExpr.eval(pi.getScriptContext());
	}
}
