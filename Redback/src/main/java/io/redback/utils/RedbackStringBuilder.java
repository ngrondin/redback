package io.redback.utils;


public class RedbackStringBuilder
{
	protected StringBuilder sb;
	
	public RedbackStringBuilder()
	{
		sb = new StringBuilder();
	}

	
	public void append(Object o)
	{
		if(o != null)
			sb.append(o);
	}
	
	public String toString()
	{
		return sb.toString();
	}
	
}
