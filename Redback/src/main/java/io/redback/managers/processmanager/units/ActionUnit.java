package io.redback.managers.processmanager.units;

import java.util.List;


import io.firebus.utils.DataMap;
import io.redback.RedbackException;
import io.redback.managers.processmanager.Actionner;
import io.redback.managers.processmanager.Process;
import io.redback.managers.processmanager.ProcessInstance;
import io.redback.managers.processmanager.ProcessManager;
import io.redback.managers.processmanager.ProcessUnit;
import io.redback.utils.Notification;

public class ActionUnit extends ProcessUnit 
{
	protected String interactionCode;
	protected String action;
	protected String process;
	protected String nextNode;

	public ActionUnit(ProcessManager pm, Process p, DataMap config) 
	{
		super(pm, p, config);
		interactionCode = config.getString("interaction");
		process = config.getString("process");
		action = config.getString("action");
		nextNode = config.getString("nextnode");
	}
	
	public void execute(ProcessInstance pi) throws RedbackException
	{
		logger.finer("Starting Action node");
		Actionner actionner = pi.getOutboundActionner();
		DataMap filter = new DataMap();
		if(process != null) 
			filter.put("process", process);
		List<Notification> notifications = processManager.getNotifications(actionner, filter, null);
		for(int i = 0; i < notifications.size(); i++)
		{
			Notification notification = notifications.get(i);
			if(interactionCode == null || (interactionCode != null && interactionCode.equals(notification.code)))
			{
				boolean actionExists = false;
				for(int j = 0; j < notification.actions.size(); j++)
					if(notification.actions.get(j).action.equals(action))
						actionExists = true;
				if(actionExists)
				{
					logger.fine("Actionning interaction '" + notification.code + "' with action '" + action +"' in process '" + notification.processName + "' instance '" + notification.pid +"'");
					processManager.actionProcess(actionner, notification.pid, action, null, null);
				}
				else
				{
					logger.info("ActionUnit tried to process the interaction '" + interactionCode + "' with an invalid action '" + action + "'");
				}
			}
		}	

		pi.setCurrentNode(nextNode);
		logger.finer("Finished Action node");
	}

}
