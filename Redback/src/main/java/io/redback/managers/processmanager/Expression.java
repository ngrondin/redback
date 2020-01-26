package io.redback.managers.processmanager;

import javax.script.Bindings;
import javax.script.Compilable;
import javax.script.CompiledScript;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import jdk.nashorn.api.scripting.JSObject;
import io.firebus.utils.DataMap;
import io.firebus.utils.FirebusDataUtil;
import io.redback.RedbackException;


public class Expression
{
	protected String expressionString;
	protected Object fixedValue;
	protected CompiledScript script;
	protected Bindings executionContext;
	
	public Expression(String s) throws RedbackException
	{
		expressionString = s;
		if(expressionString == null)
			expressionString = "\"\"";
		if(expressionString.matches("[-+]?\\d*\\.?\\d+"))
			fixedValue = Double.parseDouble(expressionString);
		else if(expressionString.equalsIgnoreCase("true") ||  expressionString.equalsIgnoreCase("false"))
			fixedValue = expressionString.equalsIgnoreCase("true") ? true : false;
		else
		{
			ScriptEngine jsEngine = new ScriptEngineManager().getEngineByName("javascript");
			try
			{
				script = ((Compilable)jsEngine).compile("var returnValue = (" + expressionString  + ");");
				executionContext = jsEngine.createBindings();
			} 
			catch (ScriptException e)
			{
				throw new RedbackException("Problem compiling the expression '" + expressionString + "'", e);
			}
		}
	}
	
	public Object eval(String name, DataMap data) throws RedbackException
	{
		if(fixedValue != null)
		{
			return fixedValue;
		}
		else
		{
			executionContext.clear();
			executionContext.put(name, FirebusDataUtil.convertDataObjectToJSObject(data));		
			try
			{
				script.eval(executionContext);
			} 
			catch (ScriptException e)
			{
				throw new RedbackException("Problem executing the expression '" + expressionString + "'", e);
			}
			Object obj = executionContext.get("returnValue");
			if(obj instanceof JSObject)
			{
				JSObject jso = (JSObject)obj;
				if(jso.isArray())
					obj = FirebusDataUtil.convertJSArrayToDataList(jso);
				else
					obj = FirebusDataUtil.convertJSObjectToDataObject(jso);
			}
			return obj;
		}
	}
}
