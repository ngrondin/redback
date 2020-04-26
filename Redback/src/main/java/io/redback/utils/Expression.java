package io.redback.utils;

import javax.script.Bindings;
import javax.script.Compilable;
import javax.script.CompiledScript;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import io.firebus.utils.FirebusDataUtil;
import io.redback.RedbackException;
import io.redback.managers.objectmanager.Value;
import jdk.nashorn.api.scripting.JSObject;

public class Expression
{
	protected String expressionString;
	protected Object fixedValue;
	protected CompiledScript script;
	
	public Expression(ScriptEngine jsEngine, String exp) throws RedbackException
	{
		expressionString = exp != null ? exp : "null";
		if(expressionString.matches("[-+]?\\d*\\.?\\d+"))
			fixedValue = Double.parseDouble(expressionString);
		else if(expressionString.equalsIgnoreCase("true") ||  expressionString.equalsIgnoreCase("false"))
			fixedValue = expressionString.equalsIgnoreCase("true") ? true : false;
		else
		{
			try
			{
				script = ((Compilable)jsEngine).compile("var returnValue = (" + expressionString  + ");");
			} 
			catch (ScriptException e)
			{
				throw new RedbackException("Problem compiling the expression '" + expressionString + "'", e);
			}
		}
	}
	
	public Object eval(Bindings context) throws RedbackException
	{
		return eval(context, null);
	}
	
	public Object eval(Bindings context, String contextDescriptor) throws RedbackException
	{
		if(fixedValue != null)
		{
			return fixedValue;
		}
		else
		{
			try
			{
				script.eval(context);
			} 
			catch (ScriptException e)
			{
				throw new RedbackException("Script error executing the expression  '" + expressionString + "'" + (contextDescriptor != null ? " in '" + contextDescriptor + "'" : ""), e);
			}
			catch(NullPointerException e)
			{
				throw new RedbackException("Null pointer exception in expression  '" + expressionString + "'" + (contextDescriptor != null ? " in '" + contextDescriptor + "'" : ""), e);
			}
			catch(RuntimeException e)
			{
				throw new RedbackException("Runtime error executing a expression  '" + expressionString + "'" + (contextDescriptor != null ? " in '" + contextDescriptor + "'" : ""), e);
			}
			Object obj = context.get("returnValue");
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