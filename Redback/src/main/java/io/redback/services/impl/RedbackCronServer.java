package io.redback.services.impl;

import io.firebus.Firebus;
import io.firebus.data.DataMap;
import io.redback.managers.cronmanager.CronTaskManager;
import io.redback.services.CronServer;

public class RedbackCronServer extends CronServer 
{
	protected CronTaskManager cronTaskManager;
	protected boolean enableCron;
	
	public RedbackCronServer(String n, DataMap c, Firebus f) {
		super(n, c, f);
		cronTaskManager = new CronTaskManager(firebus, config);		
		enableCron = config.containsKey("enable") && !config.getString("enable").equals("") ? config.getBoolean("enable") : true;
	}
	
	public void configure() {
		cronTaskManager.clearCaches();
	}

	public void start() {
		if(enableCron)
			cronTaskManager.start();
	}	

}
