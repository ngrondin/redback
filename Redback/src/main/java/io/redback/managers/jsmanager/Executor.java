package io.redback.managers.jsmanager;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.script.ScriptException;

import org.graalvm.polyglot.Value;

import io.redback.RedbackException;
import io.redback.utils.js.JSConverter;

public abstract class Executor {
	protected String sourceString;
	protected List<String> paramNames;
	protected JSManager jsManager;
	protected String functionName;
	
	public Executor(JSManager jsm, String fn, List<String> p, String src) {
		jsManager = jsm;
		functionName = fn;
		sourceString = src;
		paramNames = p;
		if(paramNames == null)
			paramNames = new ArrayList<String>();
	}
	
	protected void setSource(String src) throws RedbackException
	{
		jsManager.addFunction(functionName, src);
	}
	
	public Object execute(Map<String, Object> context) throws RedbackException
	{
		return execute(context, null);
	}
	
	public Object execute(Map<String, Object> context, String contextDescriptor) throws RedbackException
	{
		try
		{
			Object[] args = new Object[paramNames.size()];
			for(int i = 0; i < paramNames.size(); i++)
				args[i] = context.get(paramNames.get(i));
			Object obj = jsManager.execute(functionName, args);
			obj = JSConverter.toJava(Value.asValue(obj));
			return obj;
		} 
		catch (ScriptException e)
		{
			throw new RedbackException("Script error executing '" + sourceString + "'" + (contextDescriptor != null ? " in '" + contextDescriptor + "'" : ""), e);
		}
		catch(NullPointerException e)
		{
			throw new RedbackException("Null pointer exception in '" + sourceString + "'" + (contextDescriptor != null ? " in '" + contextDescriptor + "'" : ""), e);
		}
		catch(RuntimeException e)
		{
			throw new RedbackException("Runtime error executing in '" + sourceString + "'" + (contextDescriptor != null ? " in '" + contextDescriptor + "'" : ""), e);
		}
		catch (NoSuchMethodException e) 
		{
			throw new RedbackException("No such method error executing '" + sourceString + "'" + (contextDescriptor != null ? " in '" + contextDescriptor + "'" : ""), e);
		}
	}
	
}
