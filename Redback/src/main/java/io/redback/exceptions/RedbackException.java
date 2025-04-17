package io.redback.exceptions;

import io.firebus.exceptions.FunctionErrorException;
import io.firebus.script.exceptions.ScriptThrownException;

public class RedbackException extends Exception 
{
	private static final long serialVersionUID = 1L;

	public RedbackException(String msg)
	{
		super(msg);
	}	
	
	public RedbackException(String msg, Throwable t)
	{
		super(msg, t);
	}
	
	public int getErrorCode() 
	{
		Throwable c = getCause();
		while(c != null && !(c instanceof RedbackException || c instanceof FunctionErrorException || c instanceof ScriptThrownException))
			c = c.getCause();
		
		if(c != null && c instanceof RedbackException) {
			return ((RedbackException)c).getErrorCode();
		} else if(c != null && c instanceof FunctionErrorException) {
			return ((FunctionErrorException)c).getErrorCode();
		} else if(c != null && c instanceof ScriptThrownException) {
			return 400;
			
		} else {
			return 500;
		}
	}
}
