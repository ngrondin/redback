package io.redback.services.impl;

import io.firebus.Firebus;
import io.firebus.utils.DataMap;
import io.redback.managers.cronmanager.CronTaskManager;
import io.redback.services.CronServer;

public class RedbackCronServer extends CronServer 
{
	protected CronTaskManager cronTaskManager;
	
	public RedbackCronServer(String n, DataMap c, Firebus f) {
		super(n, c, f);
		cronTaskManager = new CronTaskManager(firebus, config);		
	}
	
	public void configure() {
		cronTaskManager.clearCaches();
	}

	public void start() {
		cronTaskManager.start();
	}	

}
