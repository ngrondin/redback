package com.nic.redback.services.objectserver;

import java.util.HashMap;
import java.util.Iterator;

import com.nic.firebus.utils.JSONObject;
import com.nic.redback.RedbackException;

public class FilterConfig
{
	protected JSONObject config;
	protected HashMap<String, Expression> expressions;
	
	public FilterConfig(JSONObject cfg) throws RedbackException
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
	
	public JSONObject generateFilter(RedbackObject obj) throws RedbackException
	{
		JSONObject filter = new JSONObject();
		Iterator<String> it = expressions.keySet().iterator();
		while(it.hasNext())
		{
			String key = it.next();
			filter.put(key, expressions.get(key).eval(obj).getObject());
		}
		return filter;
	}
}
