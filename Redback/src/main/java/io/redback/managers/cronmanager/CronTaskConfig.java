package io.redback.managers.cronmanager;

import javax.script.Compilable;
import javax.script.CompiledScript;
import javax.script.ScriptEngine;

import io.firebus.utils.DataMap;
import io.redback.RedbackException;

public class CronTaskConfig {
	protected CronTaskManager cronTaskManager;
	protected DataMap config;
	protected long nextRun;
	protected String lock;
	protected CompiledScript script;
	
	public CronTaskConfig(CronTaskManager ctm, DataMap c) throws RedbackException 
	{
		cronTaskManager = ctm;
		config = c;
		try {
			if(config.get("script") != null) {
				String source = config.getString("script");
				ScriptEngine jsEngine = cronTaskManager.getScriptEngine();
				synchronized(jsEngine) 
				{
					script = ((Compilable)jsEngine).compile(source);
				}
			}
		} catch(Exception e) {
			throw new RedbackException("Problem compiling script", e);
		}
	}
	
	public String getName()
	{
		return config.getString("name");
	}
	
	public void setNextRun(long nr)
	{
		nextRun = nr;
	}
	
	public long getNextRun() 
	{
		return nextRun;
	}
	
	public void setLock(String l)
	{
		lock = l;
	}
	
	public String getLock()
	{
		return lock;
	}
	
	public long getPeriod()
	{
		if(config.containsKey("period"))
			return config.getNumber("period").longValue();
		else
			return 0;
	}
	
	public CompiledScript getScript()
	{
		return script;
	}
	
	public DataMap getFirebusCall()
	{
		return config.getObject("firebus");
	}
	
	public String toString() 
	{
		return config.toString();
	}

}
