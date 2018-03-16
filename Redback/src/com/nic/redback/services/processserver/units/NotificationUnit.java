package com.nic.redback.services.processserver.units;

import com.nic.firebus.utils.JSONObject;
import com.nic.redback.RedbackException;
import com.nic.redback.services.processserver.ProcessInstance;
import com.nic.redback.services.processserver.ProcessManager;
import com.nic.redback.services.processserver.ProcessUnit;
import com.nic.redback.security.UserProfile;

public class NotificationUnit extends ProcessUnit 
{

	public NotificationUnit(ProcessManager pm, JSONObject config) 
	{
		super(pm, config);
	}
	
	public void execute(UserProfile up, ProcessInstance pi) throws RedbackException
	{
	}

}
