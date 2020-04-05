package io.redback.managers.processmanager;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import io.firebus.utils.DataMap;
import io.redback.RedbackException;

public class ExpressionMap {
	
	protected Map<String, Expression> map;
	
	public ExpressionMap(DataMap m) throws RedbackException
	{
		map = new HashMap<String, Expression>();
		Iterator<String> it = m.keySet().iterator();
		while(it.hasNext()) 
		{
			String key = it.next();
			map.put(key, new Expression(m.getString(key)));
		}
	}
	
	public DataMap eval(String name, DataMap data) throws RedbackException
	{
		DataMap out = new DataMap();
		Iterator<String> it = map.keySet().iterator();
		while(it.hasNext()) 
		{
			String key = it.next();
			out.put(key, map.get(key).eval(name, data));
		}
		return out;
	}

}
