package io.redback.managers.objectmanager.js;


import io.redback.managers.objectmanager.RedbackAggregate;
import io.redback.utils.js.ObjectJSWrapper;

public class RedbackAggregateJSWrapper extends ObjectJSWrapper
{
	protected RedbackAggregate rbAggregate;
	
	public RedbackAggregateJSWrapper(RedbackAggregate o)
	{
		super(o.getAttributeNames().toArray(new String[] {}));
		rbAggregate = o;
	}
	
	public Object call(Object arg0, Object... args)
	{
		return null;
	}


	public Object get(String name)
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
				try {
					obj = rbAggregate.getMetric(name).getObject();
				} catch(Exception e) {}
			}
			return obj;
		}
	}

}
