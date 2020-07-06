package io.redback.managers.processmanager.units;

import io.firebus.utils.DataMap;
import io.redback.RedbackException;
import io.redback.managers.processmanager.ProcessInstance;
import io.redback.managers.processmanager.ProcessManager;
import io.redback.managers.processmanager.ProcessUnit;
import io.redback.managers.jsmanager.Expression;
import io.redback.managers.processmanager.Process;

public class ConditionalUnit extends ProcessUnit 
{
	protected String expressionStr;
	protected Expression expression;
	protected String trueNode;
	protected String falseNode;
	
	public ConditionalUnit(ProcessManager pm, Process p, DataMap config) throws RedbackException 
	{
		super(pm, p, config);
		processManager = pm;
		trueNode = config.getString("truenode");
		falseNode = config.getString("falsenode");
		expressionStr = config.getString("condition");
		expression = new Expression(pm.getJSManager(), jsFunctionNameRoot, pm.getScriptVariableNames(), expressionStr);
	}

	public void execute(ProcessInstance pi) throws RedbackException
	{
		logger.finer("Start executing condition");
		try
		{
			boolean bool = (Boolean)expression.eval(pi.getScriptContext());
			if(bool == true)
				pi.setCurrentNode(trueNode);
			else
				pi.setCurrentNode(falseNode);
		} 
		catch (RedbackException e)
		{
			error("Problem occurred executing a condition", e);
		}		
		logger.finer("Finish executing condition");		
	}

}
