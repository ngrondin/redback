package io.redback.managers.processmanager.units;

import java.util.logging.Logger;

import javax.script.Bindings;
import javax.script.Compilable;
import javax.script.CompiledScript;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import io.firebus.utils.DataMap;
import io.firebus.utils.FirebusDataUtil;
import io.redback.RedbackException;
import io.redback.managers.processmanager.ProcessInstance;
import io.redback.managers.processmanager.ProcessManager;
import io.redback.managers.processmanager.ProcessUnit;
import io.redback.managers.processmanager.js.ProcessManagerJSWrapper;
import io.redback.security.Session;
import io.redback.utils.Expression;
import io.redback.utils.FirebusJSWrapper;
import io.redback.utils.StringUtils;

public class ConditionalUnit extends ProcessUnit 
{
	protected String expressionStr;
	protected Expression expression;
	protected String trueNode;
	protected String falseNode;
	
	public ConditionalUnit(ProcessManager pm, DataMap config) throws RedbackException 
	{
		super(pm, config);
		processManager = pm;
		trueNode = config.getString("truenode");
		falseNode = config.getString("falsenode");
		expressionStr = config.getString("condition");
		expression = new Expression(pm.getScriptEngine(), expressionStr);
	}

	public void execute(ProcessInstance pi) throws RedbackException
	{
		Bindings context = processManager.createScriptContext(pi);
		logger.finer("Start executing condition");
		try
		{
			boolean bool = (Boolean)expression.eval(context);
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
