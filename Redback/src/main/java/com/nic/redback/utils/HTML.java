package com.nic.redback.utils;

public class HTML 
{
	//private String htmlStr;
	private StringBuilder sb;
	
	public HTML()
	{
		sb = new StringBuilder();
	}
	
	public HTML(String s)
	{
		sb = new StringBuilder();
		append(s);
	}
	
	public void append(Object o)
	{
		if(o != null)
		{
			if(o instanceof HTML)
				sb.append(((HTML)o).toString());
			else
				sb.append(o);
		}
	}
	
	public boolean hasTag(String t)
	{
		return sb.indexOf("#" + t + "#") > -1;
	}
	
	public HTML inject(String t, HTML fragment)
	{
		String tag = "#" + t + "#";
		int pos = -1;
		while((pos = sb.indexOf(tag)) > -1)
		{
			int posNewLine = sb.substring(0, pos).lastIndexOf("\r\n");
			String indentStr = sb.substring(posNewLine + 2, pos);
			sb.replace(pos, pos + tag.length(), fragment.toString().replace("\r\n", "\r\n" + indentStr));
			//sb.replace(pos, pos + tag.length(), fragment.toString());
		}
		return this;
	}
	
	public String toString()
	{
		return sb.toString();
	}
}
