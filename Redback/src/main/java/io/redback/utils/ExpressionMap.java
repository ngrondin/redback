package io.redback.utils;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.script.Bindings;
import javax.script.ScriptEngine;

import io.firebus.utils.DataMap;
import io.redback.RedbackException;

public class ExpressionMap {
	
	protected Map<String, Expression> map;
	
	public ExpressionMap(ScriptEngine jsEngine, DataMap m) throws RedbackException
	{
		map = new HashMap<String, Expression>();
		if(m != null) 
		{
			Iterator<String> it = m.keySet().iterator();
			while(it.hasNext()) 
			{
				String key = it.next();
				map.put(key, new Expression(jsEngine, m.getString(key)));
			}
		}
	}
	
	public DataMap eval(Bindings context) throws RedbackException
	{
		return eval(context, null);
	}
	
	public DataMap eval(Bindings context, String contextDescriptor) throws RedbackException
	{
		DataMap out = new DataMap();
		Iterator<String> it = map.keySet().iterator();
		while(it.hasNext()) 
		{
			String key = it.next();
			out.put(key, map.get(key).eval(context, contextDescriptor));
		}
		return out;
	}

}