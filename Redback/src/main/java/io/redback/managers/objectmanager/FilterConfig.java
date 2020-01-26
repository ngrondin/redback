package io.redback.managers.objectmanager;

import java.util.HashMap;
import java.util.Iterator;

import io.firebus.utils.DataMap;
import io.redback.RedbackException;

public class FilterConfig
{
	protected DataMap config;
	protected HashMap<String, Expression> expressions;
	
	public FilterConfig(DataMap cfg) throws RedbackException
	{
		config = cfg;
		expressions = new HashMap<String, Expression>();
		Iterator<String> it = cfg.keySet().iterator();
		while(it.hasNext())
		{
			String key = it.next();
			expressions.put(key, new Expression(cfg.getString(key)));
		}
	}
	
	public DataMap generateFilter(RedbackObject obj) throws RedbackException
	{
		DataMap filter = new DataMap();
		Iterator<String> it = expressions.keySet().iterator();
		while(it.hasNext())
		{
			String key = it.next();
			filter.put(key, expressions.get(key).eval(obj).getObject());
		}
		return filter;
	}
}
