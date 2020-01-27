package io.redback.managers.objectmanager;

import javax.script.Bindings;
import javax.script.Compilable;
import javax.script.CompiledScript;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import io.firebus.utils.DataLiteral;
import io.firebus.utils.DataMap;
import io.firebus.utils.FirebusDataUtil;
import io.redback.RedbackException;
import io.redback.managers.objectmanagers.js.ObjectManagerJSWrapper;
import io.redback.managers.objectmanagers.js.RedbackObjectJSWrapper;
import io.redback.security.js.UserProfileJSWrapper;
import io.redback.utils.FirebusJSWrapper;
import io.redback.utils.LoggerJSFunction;
import io.redback.utils.StringUtils;
import jdk.nashorn.api.scripting.JSObject;

public class RelatedObjectConfig
{
	protected DataMap config;
	protected FilterConfig listFilter;
	protected CompiledScript listFilterScript;

	
	public RelatedObjectConfig(DataMap cfg) throws RedbackException
	{
		config = cfg;
		if(config.containsKey("listfilter"))
		{
			Object f = config.get("listfilter");
			if(f instanceof DataMap)
			{
				listFilter = new FilterConfig((DataMap)f);
			}
			else if(f instanceof DataLiteral)
			{
				ScriptEngine jsEngine = new ScriptEngineManager().getEngineByName("javascript");
				try
				{
					String scriptSrc = ((DataLiteral)f).getString();
					listFilterScript = ((Compilable)jsEngine).compile(scriptSrc);
				} 
				catch (ScriptException e)
				{
					throw new RedbackException("Problem compiling the list filter script for related object '" + getObjectName() + "'", e);
				}
			}
		}
		else
		{
			listFilter = new FilterConfig(new DataMap());
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

	/*
	public FilterConfig getListFilterConfig()
	{
		return listFilter;
	}
	
	public CompiledScript getListScript()
	{
		return listFilterScript;
	}
	*/
	
	public DataMap generateFilter(RedbackObject obj) throws RedbackException
	{
		if(listFilter != null)
		{
			return listFilter.generateFilter(obj);
		}
		else if(listFilterScript != null)
		{
			DataMap filter = new DataMap();
			Bindings context = listFilterScript.getEngine().createBindings();
			context.put("self", obj);
			context.put("om", new ObjectManagerJSWrapper(obj.getObjectManager(), obj.getUserSession()));
			context.put("userprofile", obj.getUserSession().getUserProfile());
			context.put("firebus", new FirebusJSWrapper(obj.getObjectManager().getFirebus(), obj.getUserSession()));
			context.put("global", FirebusDataUtil.convertDataObjectToJSObject(obj.getObjectManager().getGlobalVariables()));
			context.put("log", new LoggerJSFunction());
			try
			{
				listFilterScript.eval(context);
				filter = FirebusDataUtil.convertJSObjectToDataObject((JSObject)context.get("filter"));
			} 
			catch (ScriptException e)
			{
				throw new RedbackException("Problem occurred executing a list filter script", e);
			}		
			catch(NullPointerException e)
			{
				throw new RedbackException("Null pointer exception in list filter script", e);
			}
			catch(RuntimeException e)
			{
				throw new RedbackException("Problem occurred executing a list filter script", e);
			}
			return filter;
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
