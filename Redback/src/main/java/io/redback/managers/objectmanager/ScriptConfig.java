package io.redback.managers.objectmanager;

import javax.script.Bindings;
import javax.script.Compilable;
import javax.script.CompiledScript;
import javax.script.ScriptEngine;
import javax.script.ScriptException;

import io.firebus.utils.DataMap;
import io.redback.RedbackException;
import io.redback.utils.StringUtils;

public class ScriptConfig 
{
	protected DataMap config;
	protected String name;
	protected String source;
	protected CompiledScript script;

	public ScriptConfig(ScriptEngine jsEngine, DataMap cfg) throws RedbackException
	{
		config = cfg;
		name = config.getString("name");
		source = StringUtils.unescape(config.getString("script"));
		try
		{
			script = ((Compilable)jsEngine).compile(source);
		} 
		catch (ScriptException e)
		{
			throw new RedbackException("Problem compiling the script '" + name + "'", e);
		}
		
	}
	
	public String getName()
	{
		return name;
	}
	
	public String getSource()
	{
		return source;
	}
	
	public void execute(Bindings context) throws RedbackException
	{
		try
		{
			script.eval(context);
		} 
		catch (ScriptException e)
		{
			throw new RedbackException("Script error executing the script  '" + name + "'", e);
		}
	}	
}
