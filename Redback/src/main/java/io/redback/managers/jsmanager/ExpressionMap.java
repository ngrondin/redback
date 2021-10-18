package io.redback.managers.jsmanager;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import io.firebus.data.DataMap;
import io.firebus.script.ScriptFactory;
import io.firebus.script.exceptions.ScriptException;
import io.redback.exceptions.RedbackException;
import io.firebus.script.Expression;
import io.firebus.script.ScriptContext;

public class ExpressionMap {
	
	protected Map<String, Expression> map;
	
	public ExpressionMap(ScriptFactory sf, String n, DataMap m) throws RedbackException
	{
		try {
			map = new HashMap<String, Expression>();
			if(m != null) 
			{
				Iterator<String> it = m.keySet().iterator();
				while(it.hasNext()) 
				{
					String key = it.next();
					map.put(key, sf.createExpression(n + "_" + key, m.getString(key)));
				}
			}
		} catch(ScriptException e) {
			throw new RedbackException("Error compiling expression map", e);
		}
	}
	
	public DataMap eval(ScriptContext context) throws RedbackException
	{
		try {
			DataMap out = new DataMap();
			Iterator<String> it = map.keySet().iterator();
			while(it.hasNext()) 
			{
				String key = it.next();
				out.put(key, map.get(key).eval(context));
			}
			return out;
		} catch(ScriptException e) {
			throw new RedbackException("Error evaluating expression map", e);
		}
	}
	
	@Deprecated
	public DataMap eval(Map<String, Object> context) throws RedbackException
	{
		try {
			DataMap out = new DataMap();
			Iterator<String> it = map.keySet().iterator();
			while(it.hasNext()) 
			{
				String key = it.next();
				out.put(key, map.get(key).eval(context));
			}
			return out;
		} catch(ScriptException e) {
			throw new RedbackException("Error evaluating expression map", e);
		}
	}

}