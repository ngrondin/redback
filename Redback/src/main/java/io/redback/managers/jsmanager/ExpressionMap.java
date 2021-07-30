package io.redback.managers.jsmanager;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import io.firebus.utils.DataMap;
import io.redback.exceptions.RedbackException;

public class ExpressionMap {
	
	protected Map<String, Expression> map;
	
	public ExpressionMap(JSManager jsm, String n, List<String> pn, DataMap m) throws RedbackException
	{
		map = new HashMap<String, Expression>();
		if(m != null) 
		{
			Iterator<String> it = m.keySet().iterator();
			while(it.hasNext()) 
			{
				String key = it.next();
				map.put(key, new Expression(jsm, n + "_" + key, pn, m.getString(key)));
			}
		}
	}
	
	public DataMap eval(Map<String, Object> context) throws RedbackException
	{
		return eval(context, null);
	}
	
	public DataMap eval(Map<String, Object> context, String contextDescriptor) throws RedbackException
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