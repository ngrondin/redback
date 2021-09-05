package io.redback.managers.objectmanager;

import io.firebus.data.DataLiteral;
import io.firebus.data.DataMap;
import io.redback.exceptions.RedbackException;
import io.redback.managers.jsmanager.Expression;
import io.redback.managers.jsmanager.ExpressionMap;

public class RelatedObjectConfig
{
	protected ObjectManager objectManager;
	protected DataMap config;
	protected ExpressionMap listFilterExpressionMap;
	protected Expression listFilterExpression;

	
	public RelatedObjectConfig(ObjectManager om, ObjectConfig oc, AttributeConfig ac, DataMap cfg) throws RedbackException
	{
		objectManager = om;
		config = cfg;
		String fn = oc.getName() + "_" + ac.getName() + "_relatedlistfilter";
		if(config.containsKey("listfilter"))
		{
			Object f = config.get("listfilter");
			if(f instanceof DataMap)
			{
				listFilterExpressionMap = new ExpressionMap(objectManager.getJSManager(), fn, oc.getScriptVariables(), (DataMap)f);
			}
			else if(f instanceof DataLiteral)
			{
				String scriptSrc = ((DataLiteral)f).getString();
				listFilterExpression = new Expression(objectManager.getJSManager(), fn, oc.getScriptVariables(), scriptSrc); 
			}
		}
		else
		{
			listFilterExpressionMap = new ExpressionMap(objectManager.getJSManager(), fn, oc.getScriptVariables(), new DataMap());
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
		if(listFilterExpressionMap != null)
		{
			return listFilterExpressionMap.eval(elem.getScriptContext());
		}
		else if(listFilterExpression != null)
		{
			try
			{
				Object o = listFilterExpression.eval(elem.getScriptContext());
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
