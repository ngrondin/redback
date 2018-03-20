package com.nic.redback.services.processserver.units;

import com.nic.firebus.utils.JSONList;
import com.nic.firebus.utils.JSONObject;
import com.nic.redback.RedbackException;
import com.nic.redback.services.processserver.ProcessInstance;
import com.nic.redback.services.processserver.ProcessManager;
import com.nic.redback.services.processserver.ProcessUnit;

public class ActionUnit extends ProcessUnit 
{
	protected String action;
	protected String nextNode;

	public ActionUnit(ProcessManager pm, JSONObject config) 
	{
		super(pm, config);
		action = config.getString("action");
		nextNode = config.getString("nextnode");
	}
	
	public void execute(ProcessInstance pi) throws RedbackException
	{
		JSONList notifications = pi.getReceivedNotifications();
		for(int i = 0; i < notifications.size(); i++)
		{
			JSONObject notification = notifications.getObject(i);
			JSONList actions = notification.getList("actions");
			boolean actionExists = false;
			for(int j = 0; j < actions.size(); j++)
				if(actions.getObject(j).getString("action").equals(action))
					actionExists = true;
			if(actionExists)
			{
				processManager.processAction(processManager.getSystemUserProfile(), pi.getId(), notification.getString("extpid"), action, null);
				notifications.remove(i);
				i--;
			}
		}
		pi.setCurrentNode(nextNode);
	}

}
