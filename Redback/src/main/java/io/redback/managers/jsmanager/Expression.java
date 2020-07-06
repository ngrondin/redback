package io.redback.managers.jsmanager;

import java.util.List;
import java.util.Map;

import io.redback.RedbackException;

public class Expression extends Executor
{
	protected boolean fixedValueNull = false;
	protected Object fixedValue;
	
	public Expression(JSManager jsm, String fn, List<String> p, String exp) throws RedbackException
	{
		super(jsm, fn, p, exp);
		if(sourceString == null)
			fixedValueNull = true;
		else if(sourceString.matches("[-+]?\\d*\\.?\\d+"))
			fixedValue = Double.parseDouble(sourceString);
		else if(sourceString.equalsIgnoreCase("true") ||  sourceString.equalsIgnoreCase("false"))
			fixedValue = sourceString.equalsIgnoreCase("true") ? true : false;
		else
		{
			setSource("function " + functionName + "(" + (paramNames != null ? String.join(",", paramNames) : "") + ") { return (" + sourceString + ");}");
		}
	}
	
	public Object eval(Map<String, Object> context) throws RedbackException
	{
		return eval(context, null);
	}
	
	public Object eval(Map<String, Object> context, String contextDescriptor) throws RedbackException
	{
		if(fixedValueNull == true)
		{
			return null;
		}
		else if(fixedValue != null)
		{
			return fixedValue;
		}
		else
		{
			return execute(context, contextDescriptor);
		}
	}
}