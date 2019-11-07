package com.nic.redback.managers.processmanager;

import java.util.logging.Formatter;
import java.util.logging.LogRecord;

public class ProcessLogger extends Formatter
{

	public String format(LogRecord rec)
	{
		StringBuffer buf = new StringBuffer(1000);
		buf.append(rec.getMillis());
		buf.append("\t");
		String className = rec.getSourceClassName();
		buf.append(pad(className.substring(className.lastIndexOf(".") + 1), 25));
		buf.append("\t");
		buf.append(rec.getMessage());
		buf.append("\r\n");
		return buf.toString();
	}
	
	private String pad(String s, int l)
	{
		String ret = s;
		for(int i = ret.length(); i < l; i+=1)
			ret = ret + " ";
		if(ret.length() > l)
			ret = ret.substring(0,  l);
		return ret;
	}

}
