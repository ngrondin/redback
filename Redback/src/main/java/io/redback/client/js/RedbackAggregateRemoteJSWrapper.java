package io.redback.client.js;

import java.util.ArrayList;
import java.util.List;

import io.redback.client.RedbackAggregateRemote;
import io.redback.exceptions.RedbackException;
import io.redback.utils.js.CallableJSWrapper;
import io.redback.utils.js.ObjectJSWrapper;
import io.redback.client.RedbackObjectRemote;

public class RedbackAggregateRemoteJSWrapper extends ObjectJSWrapper
{
	protected RedbackAggregateRemote rbAggregateRemote;

	public RedbackAggregateRemoteJSWrapper(RedbackAggregateRemote rar)
	{
		super(new String[] {"getRelated", "getMetric", "getDimension"});
		
		rbAggregateRemote = rar;
	}
	
	public Object get(String name)
	{
		if(name.equals("getRelated"))
		{
			return new CallableJSWrapper() {
				public Object call(Object... arguments) throws RedbackException {
					RedbackObjectRemote ror = rbAggregateRemote.getRelated((String)arguments[0]);
					if(ror != null)
						return new RedbackObjectRemoteJSWrapper(ror);
					return null;
				}
			};				
		}
		else if(name.equals("getMetric"))
		{
			return new CallableJSWrapper() {
				public Object call(Object... arguments) throws RedbackException  {
					Number n = rbAggregateRemote.getMetric((String)arguments[0]);
					if(n != null)
						return n;
					return null;
				}
			};				
		}		
		else if(name.equals("getDimension"))
		{
			return new CallableJSWrapper() {
				public Object call(Object... arguments) throws RedbackException {
					Object o = rbAggregateRemote.getDimension((String)arguments[0]);
					if(o != null)
						return o;
					return null;
				}
			};				
		}
		else {
			return null;
		}
	}

	public static List<RedbackAggregateRemoteJSWrapper> convertList(List<RedbackAggregateRemote> list) 
	{
		List<RedbackAggregateRemoteJSWrapper> ret = new ArrayList<RedbackAggregateRemoteJSWrapper>();
		if(list != null)
			for(RedbackAggregateRemote rar: list) 
				ret.add(new RedbackAggregateRemoteJSWrapper(rar));
		return ret;
	}
	
}
