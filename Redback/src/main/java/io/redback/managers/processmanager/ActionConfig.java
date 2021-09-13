package io.redback.managers.processmanager;

import io.firebus.data.DataMap;
import io.firebus.script.Expression;
import io.firebus.script.exceptions.ScriptException;
import io.redback.exceptions.RedbackException;
import io.redback.utils.StringUtils;

public class ActionConfig {

	protected ProcessManager processManager;
	protected String actionDescription;
	protected String actionName;
	protected boolean main;
	protected String nextNode;
	protected String exclusiveStr;
	protected Expression exclusiveExpr;
	
	
	public ActionConfig(ProcessManager pm, Process p, ProcessUnit pu, DataMap c) throws RedbackException
	{
		try {
			processManager = pm;
			actionName = c.getString("action");
			actionDescription = c.getString("description");
			main = c.getBoolean("main");
			nextNode = c.getString("nextnode");
			if(c.containsKey("exclusive")) {
				exclusiveStr = c.getString("exclusive");
				String name = p.getName() + "_node_" + StringUtils.base16(pu.getId().hashCode()) + "_action_" + StringUtils.base16(c.hashCode());
				exclusiveExpr = processManager.getScriptFactory().createExpression(name, exclusiveStr);
			}
		} catch(Exception e) {
			throw new RedbackException("Error initialising action config", e);
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
	
	public boolean isMain() {
		return main;
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
		try {
			return exclusiveExpr.eval(pi.getScriptContext());
		} catch(ScriptException e) {
			throw new RedbackException("Error evaluating exlusiveid expression", e);
		}
	}
}
