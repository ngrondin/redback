package com.nic.redback.services.objectserver;

import javax.script.Compilable;
import javax.script.CompiledScript;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import com.nic.firebus.utils.JSONObject;
import com.nic.redback.RedbackException;
import com.nic.redback.utils.StringUtils;

public class RelatedObjectConfig
{
	protected JSONObject config;
	protected FilterConfig listFilter;
	protected 	CompiledScript listScript;

	
	public RelatedObjectConfig(JSONObject cfg) throws RedbackException
	{
		config = cfg;
		if(config.get("listfilter") != null)
		{
			listFilter = new FilterConfig(config.getObject("listfilter"));
		}
		else if(config.get("listscript") != null)
		{
			ScriptEngine jsEngine = new ScriptEngineManager().getEngineByName("javascript");
			try
			{
				String scriptSrc = StringUtils.unescape(config.getString("listscript"));
				listScript = ((Compilable)jsEngine).compile(scriptSrc);
			} 
			catch (ScriptException e)
			{
				throw new RedbackException("Problem compiling the list script for related object '" + getObjectName() + "'", e);
			}
		}
		else
		{
			listFilter = new FilterConfig(new JSONObject());
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

	public FilterConfig getListFilterConfig()
	{
		return listFilter;
	}
	
	public CompiledScript getListScript()
	{
		return listScript;
	}
	
	public JSONObject getJSON()
	{
		return config;
	}

}
