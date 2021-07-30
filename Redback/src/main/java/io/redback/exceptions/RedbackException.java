package io.redback.exceptions;

import io.firebus.exceptions.FunctionErrorException;

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
		if(c instanceof RedbackException) {
			return ((RedbackException)c).getErrorCode();
		} else if(c instanceof FunctionErrorException) {
			return ((FunctionErrorException)c).getErrorCode();
		} else {
			return 0;
		}
	}
}
