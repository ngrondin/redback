package io.redback.managers.objectmanager;

import javax.script.Bindings;

import io.firebus.utils.DataLiteral;
import io.firebus.utils.DataMap;
import io.redback.RedbackException;
import io.redback.utils.Expression;
import io.redback.utils.ExpressionMap;

public class RelatedObjectConfig
{
	protected ObjectManager objectManager;
	protected DataMap config;
	protected ExpressionMap listFilterExpressionMap;
	protected Expression listFilterExpression;

	
	public RelatedObjectConfig(ObjectManager om, DataMap cfg) throws RedbackException
	{
		objectManager = om;
		config = cfg;
		if(config.containsKey("listfilter"))
		{
			Object f = config.get("listfilter");
			if(f instanceof DataMap)
			{
				listFilterExpressionMap = new ExpressionMap(objectManager.getScriptEngine(), (DataMap)f);
			}
			else if(f instanceof DataLiteral)
			{
				String scriptSrc = ((DataLiteral)f).getString();
				listFilterExpression = new Expression(objectManager.getScriptEngine(), scriptSrc); // ((Compilable)jsEngine).compile(scriptSrc);
			}
		}
		else
		{
			listFilterExpressionMap = new ExpressionMap(objectManager.getScriptEngine(), new DataMap());
		}
	}
	
	public String getObjectName()
	{
		return config.getString("name");
	}
	
	public String getLinkAttributeName()
	{
		return config.getString("linkattribute");
	}

	public DataMap generateFilter(RedbackElement elem) throws RedbackException
	{
		Bindings context = objectManager.createScriptContext(elem);
		if(listFilterExpressionMap != null)
		{
			return listFilterExpressionMap.eval(context);
		}
		else if(listFilterExpression != null)
		{
			try
			{
				Object o = listFilterExpression.eval(context);
				if(o instanceof DataMap)
					return (DataMap)o;
				else
					return null;
			} 
			catch(NullPointerException e)
			{
				throw new RedbackException("Null pointer exception in list filter script", e);
			}
			catch(RuntimeException e)
			{
				throw new RedbackException("Problem occurred executing a list filter script", e);
			}
		}
		else
		{
			return null;
		}
	}
	
	public DataMap getJSON()
	{
		return config;
	}

}
