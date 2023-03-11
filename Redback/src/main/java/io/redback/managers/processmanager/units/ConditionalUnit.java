package io.redback.managers.processmanager.units;

import io.firebus.data.DataMap;
import io.firebus.logging.Logger;
import io.firebus.script.Expression;
import io.redback.managers.processmanager.ProcessInstance;
import io.redback.managers.processmanager.ProcessManager;
import io.redback.managers.processmanager.ProcessUnit;
import io.redback.exceptions.RedbackException;
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
		try {
			processManager = pm;
			trueNode = config.getString("truenode");
			falseNode = config.getString("falsenode");
			expressionStr = config.getString("condition");
			expression = pm.getScriptFactory().createExpression(jsFunctionNameRoot, expressionStr);
		} catch(Exception e) {
			throw new RedbackException("Error initialising conditional unit", e);
		}
	}

	public void execute(ProcessInstance pi) throws RedbackException
	{
		Logger.finer("rb.process.consitional.start", null);
		try
		{
			boolean bool = (Boolean)expression.eval(pi.getScriptContext());
			if(bool == true)
				pi.setCurrentNode(trueNode);
			else
				pi.setCurrentNode(falseNode);
		} 
		catch (Exception e)
		{
			throw new RedbackException("Error executing a process condition", e);
		}		
		Logger.finer("rb.process.consitional.finish", null);
	}

}
