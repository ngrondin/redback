package io.redback.managers.objectmanager;

import io.firebus.utils.DataMap;
import io.redback.RedbackException;
import io.redback.utils.StringUtils;

public class IncludeScript 
{
	protected DataMap config;
	protected String name;
	protected String source;

	public IncludeScript(DataMap cfg) throws RedbackException
	{
		config = cfg;
		name = config.getString("name");
		source = StringUtils.unescape(config.getString("script"));
	}
	
	public String getName()
	{
		return name;
	}
	
	public String getSource()
	{
		return source;
	}
}
