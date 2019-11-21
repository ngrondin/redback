package com.nic.redback.managers.objectmanager;

import java.util.Iterator;

import javax.script.Bindings;
import javax.script.Compilable;
import javax.script.CompiledScript;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import com.nic.redback.RedbackException;
import com.nic.redback.security.js.SessionRightsJSFunction;


public class Expression
{
	protected String expressionString;
	protected Value fixedValue;
	protected CompiledScript script;
	protected Bindings executionContext;
	
	public Expression(String s) throws RedbackException
	{
		expressionString = s;
		if(s.matches("[-+]?\\d*\\.?\\d+"))
			fixedValue = new Value(Double.parseDouble(s));
		else if(s.equalsIgnoreCase("true") ||  s.equalsIgnoreCase("false"))
			fixedValue = new Value(s.equalsIgnoreCase("true") ? true : false);
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
	
	public Value eval(RedbackObject obj) throws RedbackException
	{
		if(fixedValue != null)
		{
			return fixedValue;
		}
		else
		{
			executionContext.clear();
			executionContext.put("canRead", new SessionRightsJSFunction(obj.getUserSession(), "read"));
			executionContext.put("canWrite", new SessionRightsJSFunction(obj.getUserSession(), "write"));
			executionContext.put("canExecute", new SessionRightsJSFunction(obj.getUserSession(), "execute"));
			executionContext.put("uid", obj.getUID().getString());
			Iterator<String> it = obj.getObjectConfig().getAttributeNames().iterator();
			while(it.hasNext())
			{	
				String key = it.next();
				if(obj.getObjectConfig().getAttributeConfig(key).getExpression() == null)
					executionContext.put(key, obj.get(key).getString());
			}
			try
			{
				script.eval(executionContext);
			} 
			catch (ScriptException e)
			{
				throw new RedbackException("Script error executing the expression  '" + expressionString + "' with object '" + obj.getObjectConfig().getName() + ":" + obj.getUID().getString() + "'", e);
			}
			catch(NullPointerException e)
			{
				throw new RedbackException("Null pointer exception in expression  '" + expressionString + "' with object '" + obj.getObjectConfig().getName() + ":" + obj.getUID().getString() + "'", e);
			}
			catch(RuntimeException e)
			{
				throw new RedbackException("Runtime error executing a expression  '" + expressionString + "' with object '" + obj.getObjectConfig().getName() + ":" + obj.getUID().getString() + "'", e);
			}
			return new Value(executionContext.get("returnValue"));
		}
	}
}