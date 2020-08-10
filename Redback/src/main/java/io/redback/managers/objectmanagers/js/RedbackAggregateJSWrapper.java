package io.redback.managers.objectmanagers.js;

import java.util.Arrays;
import org.graalvm.polyglot.Value;
import org.graalvm.polyglot.proxy.ProxyArray;
import org.graalvm.polyglot.proxy.ProxyObject;

import io.redback.RedbackException;
import io.redback.managers.objectmanager.RedbackAggregate;
import io.redback.utils.js.JSConverter;

public class RedbackAggregateJSWrapper implements ProxyObject
{
	//private Logger logger = Logger.getLogger("io.redback");
	protected RedbackAggregate rbAggregate;
	protected String[] members = {"objectname"};
	
	public RedbackAggregateJSWrapper(RedbackAggregate o)
	{
		rbAggregate = o;
	}
	
	public Object call(Object arg0, Object... args)
	{
		return null;
	}


	public Object getMember(String name)
	{
		try
		{
			if(name.equals("objectname"))
			{
				return rbAggregate.getObjectConfig().getName();
			}
			else
			{
				Object obj = rbAggregate.get(name).getObject();
				if(obj == null)
				{
					obj = rbAggregate.getMetric(name).getObject();
				}
				return JSConverter.toJS(obj);
			}
		} 
		catch (RedbackException e)
		{
			throw new RuntimeException("Error getting the Redback Object attribute '" + name + "'", e);
		}
	}

	public Object getMemberKeys() {
		return ProxyArray.fromArray(((Object[])members));
	}

	public boolean hasMember(String key) {
		if(Arrays.asList(members).contains(key)) {
			return true;
		} else if(rbAggregate.getAttributeNames().contains(key)) {
			return true;
		} else {
			return false;
		}
	}

	@Override
	public void putMember(String key, Value value) {
		
	}

}
