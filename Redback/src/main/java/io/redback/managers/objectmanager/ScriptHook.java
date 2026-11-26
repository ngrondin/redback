package io.redback.managers.objectmanager;

import io.firebus.data.DataMap;
import io.redback.exceptions.RedbackException;

public class ScriptHook
{
	protected String name;
	protected String object;
	protected String field;
	protected String scriptName;
	protected String domain;
	protected String type;
	protected boolean enabled;
	
	public ScriptHook(DataMap cfg) throws RedbackException
	{
		object = cfg.getString("object");
		field = cfg.getString("field");
		scriptName = cfg.getString("scriptname");
		if(object == null || field == null || scriptName == null)
			throw new RedbackException("Invalid script hook: 'object', 'field' and 'scriptname' are required");
		name = cfg.getString("name");
		if(name == null)
			name = object + "." + field;
		domain = cfg.getString("domain");
		type = cfg.containsKey("type") ? cfg.getString("type") : "update";
		enabled = cfg.containsKey("enabled") ? cfg.getBoolean("enabled") : true;
	}
	
	public String getName()
	{
		return name;
	}
	
	public String getObject()
	{
		return object;
	}
	
	public String getField()
	{
		return field;
	}
	
	public String getScriptName()
	{
		return scriptName;
	}
	
	public String getDomain()
	{
		return domain;
	}
	
	public String getType()
	{
		return type;
	}
		
	public boolean isOnUpdate() 
	{
		return type.equals("update");
	}
	
	public boolean isOnSave() 
	{
		return type.equals("save") || type.equals("savebulk");
	}
	
	public boolean isAfterSave() 
	{
		return type.equals("aftersave") || type.equals("aftersavebulk");
	}
	
	public boolean isBulk() 
	{
		return type.equals("savebulk") || type.equals("aftersavebulk");
	}
	
	public boolean isEnabled()
	{
		return enabled;
	}
	
	public boolean matchesDomain(String domain)
	{
		return this.domain == null || this.domain.equals(domain);
	}
}
