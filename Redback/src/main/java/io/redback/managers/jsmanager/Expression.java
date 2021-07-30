package io.redback.managers.jsmanager;

import java.util.List;
import java.util.Map;

import io.redback.exceptions.RedbackException;

public class Expression extends Executor
{
	protected boolean fixedValueNull = false;
	protected Object fixedValue;
	
	public Expression(JSManager jsm, String fn, List<String> p, String exp) throws RedbackException
	{
		super(jsm, fn, p, "return (" + exp + ");");
		if(exp == null)
			fixedValueNull = true;
		else if(exp.matches("[-+]?\\d*\\.?\\d+"))
			fixedValue = Double.parseDouble(exp);
		else if(exp.equalsIgnoreCase("true") ||  exp.equalsIgnoreCase("false"))
			fixedValue = exp.equalsIgnoreCase("true") ? true : false;
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