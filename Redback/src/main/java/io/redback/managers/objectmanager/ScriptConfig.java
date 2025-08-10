package io.redback.managers.objectmanager;

import java.util.ArrayList;
import java.util.List;

import io.firebus.data.DataMap;
import io.firebus.script.Function;
import io.firebus.script.ScriptFactory;
import io.firebus.script.exceptions.ScriptException;
import io.redback.exceptions.RedbackException;
import io.redback.utils.StringUtils;

public class ScriptConfig 
{
	protected DataMap config;
	protected String name;
	protected String domain;
	protected String description;
	protected String accessCategory;
	protected String showExpression;
	protected String source;
	protected long timeout;
	protected String icon;
	protected Function function;

	public ScriptConfig(ScriptFactory sf, DataMap cfg) throws RedbackException
	{
		config = cfg;
		name = config.getString("name");
		source = StringUtils.unescape(config.getString("script"));
		description = config.getString("description");
		domain = config.getString("domain");
		accessCategory = config.getString("accesscat");
		showExpression = config.getString("showexpr");
		timeout = config.containsKey("timeout") ? config.getNumber("timeout").longValue(): 10000;
		icon = config.getString("icon");
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
		scriptVars.add("ic");
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
	
	public String getDomain()
	{
		return domain;
	}
	
	public String getDescription()
	{
		return description;
	}
	
	public String getAccessCategory() 
	{
		return accessCategory;
	}
	
	public String getShowExpression()
	{
		return showExpression;
	}
	
	public long getTimeout() 
	{
		return timeout;
	}
	
	public String getIcon() 
	{
		return icon;
	}
	
	public String getSource()
	{
		return source;
	}
	
	public Function getFunction() 
	{
		return function;
	}
}
