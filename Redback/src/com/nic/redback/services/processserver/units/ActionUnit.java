package com.nic.redback.services.processserver.units;

import java.util.ArrayList;

import com.nic.firebus.utils.JSONList;
import com.nic.firebus.utils.JSONObject;
import com.nic.redback.RedbackException;
import com.nic.redback.security.Session;
import com.nic.redback.services.processserver.ProcessInstance;
import com.nic.redback.services.processserver.ProcessManager;
import com.nic.redback.services.processserver.ProcessUnit;

public class ActionUnit extends ProcessUnit 
{
	protected String interactionCode;
	protected String action;
	protected String nextNode;

	public ActionUnit(ProcessManager pm, JSONObject config) 
	{
		super(pm, config);
		interactionCode = config.getString("interaction");
		action = config.getString("action");
		nextNode = config.getString("nextnode");
	}
	
	public void execute(ProcessInstance pi, JSONObject result) throws RedbackException
	{
		logger.info("Starting Action node");
		JSONList notifications = pi.getReceivedNotifications();
		ArrayList<Integer> relevantNotificationIds = new ArrayList<Integer>();
		for(int i = 0; i < notifications.size(); i++)
		{
			JSONObject notification = notifications.getObject(i);
			if(interactionCode.equals(notification.getString("interaction")))
			{
				boolean actionExists = false;
				JSONList actions = notification.getList("actions");
				for(int j = 0; j < actions.size(); j++)
					if(actions.getObject(j).getString("action").equals(action))
						actionExists = true;
				if(actionExists)
					relevantNotificationIds.add(i);
				else
					logger.info("ActionUnit tried to process the interaction '" + interactionCode + "' with an invalid action '" + action + "'");
			}
		}
		
		//the relevantNotificationIds list is created to ensure new notifications coming in during process as a result of loop back are not considered
		if(relevantNotificationIds.size() == 0)
		{
			logger.info("ActionUnit tried to process the interaction '" + interactionCode + "'  for which no notification was received");
		}
		else
		{
			for(int i = relevantNotificationIds.size() - 1; i >= 0; i--)
			{
				JSONObject notification = notifications.getObject(relevantNotificationIds.get(i));
				Session sysUserSession = processManager.getSystemUserSession(pi.getDomain());
				logger.fine("Actionning interaction '" + interactionCode + "' with action '" + action +"' in instance '" + notification.getString("pid") +"'");
				JSONObject actionResult = processManager.processAction(sysUserSession, pi.getId(), notification.getString("pid"), action, null);
				result.merge(actionResult);
				notifications.remove(relevantNotificationIds.get(i));
			}	
		}

		pi.setCurrentNode(nextNode);
		logger.info("Finished Action node");
	}

}
