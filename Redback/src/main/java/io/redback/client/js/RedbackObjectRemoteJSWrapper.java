package io.redback.client.js;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.graalvm.polyglot.Value;
import org.graalvm.polyglot.proxy.ProxyArray;
import org.graalvm.polyglot.proxy.ProxyExecutable;
import org.graalvm.polyglot.proxy.ProxyObject;

import io.firebus.data.DataMap;
import io.redback.client.RedbackObjectRemote;
import io.redback.exceptions.RedbackException;
import io.redback.utils.js.JSConverter;

public class RedbackObjectRemoteJSWrapper implements ProxyObject
{
	protected RedbackObjectRemote rbObjectRemote;
	protected String[] members = {"getRelated", "execute"};
	
	public RedbackObjectRemoteJSWrapper(RedbackObjectRemote o)
	{
		rbObjectRemote = o;
	}
	
	public Object getMember(String name)
	{
		if(name.equals("getRelated"))
		{
			return new ProxyExecutable() {
				public Object execute(Value... arguments) {
					RedbackObjectRemote ror = rbObjectRemote.getRelated(arguments[0].asString());
					if(ror != null)
						return new RedbackObjectRemoteJSWrapper(ror);
					return null;
				}
			};				
		}
		else if(name.equals("execute"))
		{
			return new ProxyExecutable() {
				public Object execute(Value... arguments) {
					String function = arguments[0].asString();
					DataMap data = arguments.length > 1 ? (DataMap)JSConverter.toJava(arguments[1]) : null;
					try {
						rbObjectRemote.execute(function, data);
						return null;
					} catch(Exception e) {
						throw new RuntimeException("Error executing function on object", e);
					}
				}
			};				
		}
		else if(name.equals("uid"))
		{
			return rbObjectRemote.getUid();
		}
		else
		{
			Object obj = rbObjectRemote.get(name);
			return JSConverter.toJS(obj);
		}
	}

	public Object getMemberKeys() {
		List<Object> list = new ArrayList<Object>();
		for(int i = 0; i < members.length; i++)
			list.add(members[i]);
		list.add("objectname");
		list.add("uid");
		list.add("domain");
		list.addAll(rbObjectRemote.getAttributeNames());
		return ProxyArray.fromList(list);
	}

	public boolean hasMember(String key) {
		if(Arrays.asList(members).contains(key)) {
			return true;
		} else if(rbObjectRemote.get(key) != null) {
			return true;
		} else if(key.equals("uid") || key.equals("objectname") || key.equals("domain")) {
			return true;
		} else {
			return false;
		}
	}

	public void putMember(String key, Value value) {
		try
		{
			rbObjectRemote.set(key, JSConverter.toJava(value));
		} 
		catch (RedbackException e)
		{
			String errMsg = "Error setting the Redback Object attribute '" + key + "'";
			throw new RuntimeException(errMsg, e);		
		}		
	}
}
