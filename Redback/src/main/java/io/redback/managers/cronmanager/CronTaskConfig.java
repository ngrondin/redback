package io.redback.managers.cronmanager;

import io.firebus.data.DataMap;
import io.redback.exceptions.RedbackException;

public class CronTaskConfig {
	protected CronTaskManager cronTaskManager;
	protected DataMap config;
	protected long nextRun;
	
	public CronTaskConfig(CronTaskManager ctm, DataMap c) throws RedbackException 
	{
		cronTaskManager = ctm;
		config = c;
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
	
	public long getPeriod()
	{
		if(config.containsKey("period"))
			return config.getNumber("period").longValue();
		else
			return 0;
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
