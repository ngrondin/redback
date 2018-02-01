package com.nic.redback.services.objectserver;

import javax.script.Compilable;
import javax.script.CompiledScript;
import javax.script.ScriptEngine;
import javax.script.ScriptException;

import com.nic.firebus.utils.JSONObject;
import com.nic.redback.utils.StringUtils;

public class ScriptConfig 
{
	protected JSONObject config;	
	protected CompiledScript script;
	
	public ScriptConfig(JSONObject cfg, ScriptEngine jsEngine) throws ScriptException
	{
		config = cfg;
		String source = StringUtils.unescape(config.getString("script")) + "\\n//#sourceURL=" + getObjectName() + (getAttributeName() != null ? "." + getAttributeName() : "") + "!" + getEventName();
		script = ((Compilable) jsEngine).compile(source);
	}
	
	public String getObjectName()
	{
		return config.getString("object");
	}

	public String getEventName()
	{
		return config.getString("event");
	}

	public String getAttributeName()
	{
		return config.getString("attribute");
	}
	
	public CompiledScript getScript()
	{
		return script;
	}
	
}
