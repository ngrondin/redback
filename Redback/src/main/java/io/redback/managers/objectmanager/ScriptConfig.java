package io.redback.managers.objectmanager;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import io.firebus.utils.DataMap;
import io.redback.RedbackException;
import io.redback.managers.jsmanager.Function;
import io.redback.managers.jsmanager.JSManager;
import io.redback.utils.StringUtils;

public class ScriptConfig 
{
	protected DataMap config;
	protected String name;
	protected String source;
	protected Function function;

	public ScriptConfig(JSManager jsm, DataMap cfg) throws RedbackException
	{
		config = cfg;
		name = config.getString("name");
		source = StringUtils.unescape(config.getString("script"));
		List<String> scriptVars = new ArrayList<String>();
		scriptVars.add("session");
		scriptVars.add("userprofile");
		scriptVars.add("firebus");
		scriptVars.add("om");
		scriptVars.add("pm");
		scriptVars.add("geo");
		scriptVars.add("fc");
		scriptVars.add("rc");
		scriptVars.add("nc");
		scriptVars.add("param");
		function = new Function(jsm, name, scriptVars, source);
	}
	
	public String getName()
	{
		return name;
	}
	
	public String getSource()
	{
		return source;
	}

	
	public void execute(Map<String, Object> context) throws RedbackException
	{
		function.execute(context);	
	}	
}
