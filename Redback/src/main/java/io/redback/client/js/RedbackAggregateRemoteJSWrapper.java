package io.redback.client.js;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.graalvm.polyglot.Value;
import org.graalvm.polyglot.proxy.ProxyArray;
import org.graalvm.polyglot.proxy.ProxyExecutable;
import org.graalvm.polyglot.proxy.ProxyObject;

import io.redback.client.RedbackAggregateRemote;
import io.redback.utils.js.JSConverter;

public class RedbackAggregateRemoteJSWrapper implements ProxyObject
{
	protected RedbackAggregateRemote rbAggregateRemote;
	protected String[] members = {"getRelated", "getMetric", "getDimension"};
	
	public RedbackAggregateRemoteJSWrapper(RedbackAggregateRemote rar)
	{
		rbAggregateRemote = rar;
	}
	
	public Object getMember(String name)
	{
		if(name.equals("getRelated"))
		{
			return new ProxyExecutable() {
				public Object execute(Value... arguments) {
					RedbackAggregateRemote rar = rbAggregateRemote.getRelated(arguments[0].asString());
					if(rar != null)
						return new RedbackAggregateRemoteJSWrapper(rar);
					return null;
				}
			};				
		}
		else if(name.equals("getMetric"))
		{
			return new ProxyExecutable() {
				public Object execute(Value... arguments) {
					Number n = rbAggregateRemote.getMetric(arguments[0].asString());
					if(n != null)
						return JSConverter.toJS(n);
					return null;
				}
			};				
		}		
		else if(name.equals("getDimension"))
		{
			return new ProxyExecutable() {
				public Object execute(Value... arguments) {
					Object o = rbAggregateRemote.getDimension(arguments[0].asString());
					if(o != null)
						return JSConverter.toJS(o);
					return null;
				}
			};				
		}
		else {
			return null;
		}
	}

	public Object getMemberKeys() {
		List<Object> list = new ArrayList<Object>();
		for(int i = 0; i < members.length; i++)
			list.add(members[i]);
		list.add("objectname");
		list.addAll(rbAggregateRemote.getAttributeNames());
		return ProxyArray.fromList(list);
	}

	public boolean hasMember(String key) {
		if(Arrays.asList(members).contains(key)) {
			return true;
		} else if(rbAggregateRemote.getDimension(key) != null) {
			return true;
		} else if(key.equals("objectname")) {
			return true;
		} else {
			return false;
		}
	}

	public void putMember(String key, Value value) {
		// TODO Auto-generated method stub
		
	}


}
