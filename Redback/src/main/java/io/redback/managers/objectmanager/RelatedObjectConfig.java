package io.redback.managers.objectmanager;

import io.firebus.data.DataLiteral;
import io.firebus.data.DataMap;
import io.firebus.script.Expression;
import io.firebus.script.exceptions.ScriptException;
import io.redback.exceptions.RedbackException;
import io.redback.managers.jsmanager.ExpressionMap;

public class RelatedObjectConfig
{
	protected ObjectManager objectManager;
	protected DataMap config;
	protected ExpressionMap listFilterExpressionMap;
	protected Expression listFilterExpression;

	
	public RelatedObjectConfig(ObjectManager om, ObjectConfig oc, AttributeConfig ac, DataMap cfg) throws RedbackException
	{
		try {
			objectManager = om;
			config = cfg;
			String fn = oc.getName() + "_" + ac.getName() + "_relatedlistfilter";
			if(config.containsKey("listfilter"))
			{
				Object f = config.get("listfilter");
				if(f instanceof DataMap)
				{
					listFilterExpressionMap = new ExpressionMap(objectManager.getScriptFactory(), fn, (DataMap)f);
				}
				else if(f instanceof DataLiteral)
				{
					String scriptSrc = ((DataLiteral)f).getString();
					listFilterExpression = objectManager.getScriptFactory().createExpression(fn, scriptSrc); 
				}
			}
			else
			{
				listFilterExpressionMap = new ExpressionMap(objectManager.getScriptFactory(), fn, new DataMap());
			}
		} catch(ScriptException e) {
			throw new RedbackException("Error initialising related object config", e);
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
	
	public String getReverseAttributeName()
	{
		return config.getString("reverseattribute");
	}
	
	public String getReverseMapAttributeName()
	{
		return config.getString("reversemapattribute");
	}
	
	public boolean shouldUIResolve()
	{
		return config.containsKey("uiresolve") ? config.getBoolean("uiresolve") : true;
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
			catch(ScriptException e)
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
