package com.nic.redback.services.processserver;

import javax.script.Bindings;
import javax.script.Compilable;
import javax.script.CompiledScript;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import com.nic.firebus.utils.FirebusDataUtil;
import com.nic.redback.RedbackException;


public class Expression
{
	protected String expressionString;
	protected Object fixedValue;
	protected CompiledScript script;
	protected Bindings executionContext;
	
	public Expression(String s) throws RedbackException
	{
		expressionString = s;
		if(s.matches("[-+]?\\d*\\.?\\d+"))
			fixedValue = Double.parseDouble(s);
		else if(s.equalsIgnoreCase("true") ||  s.equalsIgnoreCase("false"))
			fixedValue = s.equalsIgnoreCase("true") ? true : false;
		else
		{
			ScriptEngine jsEngine = new ScriptEngineManager().getEngineByName("javascript");
			try
			{
				script = ((Compilable)jsEngine).compile("var returnValue = (" + s  + ");");
				executionContext = jsEngine.createBindings();
			} 
			catch (ScriptException e)
			{
				throw new RedbackException("Problem compiling the expression '" + expressionString + "'", e);
			}
		}
	}
	
	public Object eval(ProcessInstance pi) throws RedbackException
	{
		if(fixedValue != null)
		{
			return fixedValue;
		}
		else
		{
			executionContext.clear();
			executionContext.put("data", FirebusDataUtil.convertDataObjectToJSObject(pi.getData()));
			try
			{
				script.eval(executionContext);
			} 
			catch (ScriptException e)
			{
				throw new RedbackException("Problem executing the expression '" + expressionString + "'", e);
			}
			return executionContext.get("returnValue");
		}
	}
}
