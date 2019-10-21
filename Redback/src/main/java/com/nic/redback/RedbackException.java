package com.nic.redback;

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
}
