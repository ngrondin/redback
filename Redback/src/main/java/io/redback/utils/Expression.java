package io.redback.utils;

import java.util.logging.Logger;

import javax.script.Bindings;
import javax.script.Compilable;
import javax.script.CompiledScript;
import javax.script.ScriptEngine;
import javax.script.ScriptException;

import org.graalvm.polyglot.Value;

import io.redback.RedbackException;
import io.redback.utils.js.JSConverter;

public class Expression
{
	private Logger logger = Logger.getLogger("io.redback");
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
				synchronized(jsEngine) 
				{
					//script = ((Compilable)jsEngine).compile("var returnValue = (" + expressionString  + ");");
					script = ((Compilable)jsEngine).compile(expressionString);
				}
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
				Object obj = script.eval(context);
				obj = JSConverter.toJava(Value.asValue(obj));
				return obj;
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
			//Object obj = JSConverter.toJava(Value.asValue(context.get("returnValue")));
			//return obj;
		}
	}
}