package io.redback.managers.objectmanager;

import java.util.ArrayList;
import java.util.List;

import io.firebus.data.DataMap;
import io.firebus.script.Function;
import io.firebus.script.ScriptContext;
import io.firebus.script.ScriptFactory;
import io.firebus.script.exceptions.ScriptException;
import io.redback.exceptions.RedbackException;
import io.redback.utils.StringUtils;

public class ScriptConfig 
{
	protected DataMap config;
	protected String name;
	protected String source;
	protected Function function;

	public ScriptConfig(ScriptFactory sf, DataMap cfg) throws RedbackException
	{
		config = cfg;
		name = config.getString("name");
		source = StringUtils.unescape(config.getString("script"));
		List<String> scriptVars = new ArrayList<String>();
		scriptVars.add("session");
		scriptVars.add("userprofile");
		scriptVars.add("firebus");
		scriptVars.add("om");
		scriptVars.add("pm"); //Deprecated
		scriptVars.add("pc");
		scriptVars.add("geo");
		scriptVars.add("fc");
		scriptVars.add("rc");
		scriptVars.add("nc");
		scriptVars.add("param");
		try {
			function = sf.createFunction(name, scriptVars.toArray(new String[] {}), source);
		} catch(ScriptException e) {
			throw new RedbackException("Error compiling script", e);
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

	
	public void execute(ScriptContext context) throws RedbackException
	{
		try {
			function.call(context);
		} catch(ScriptException e) {
			throw new RedbackException("Error running script", e);
		}
	}	
}
