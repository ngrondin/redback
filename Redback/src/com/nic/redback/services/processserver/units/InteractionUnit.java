package com.nic.redback.services.processserver.units;

import java.util.ArrayList;

import com.nic.firebus.utils.JSONList;
import com.nic.firebus.utils.JSONObject;
import com.nic.redback.RedbackException;
import com.nic.redback.services.processserver.Assignee;
import com.nic.redback.services.processserver.AssigneeConfig;
import com.nic.redback.services.processserver.ProcessInstance;
import com.nic.redback.services.processserver.ProcessManager;
import com.nic.redback.services.processserver.ProcessUnit;
import com.nic.redback.security.UserProfile;

public class InteractionUnit extends ProcessUnit 
{
	protected JSONList actions;
	protected String assigneeType;
	protected ArrayList<AssigneeConfig> assigneeConfigs;
	protected JSONObject notification;

	public InteractionUnit(ProcessManager pm, JSONObject config) throws RedbackException 
	{
		super(pm, config);
		actions = config.getList("actions");
		assigneeConfigs = new ArrayList<AssigneeConfig>();
		if(config.containsKey("assignees")  &&  config.get("assignees") instanceof JSONList)
		{
			JSONList list = config.getList("assignees");
			for(int i = 0; i < list.size(); i++)
				assigneeConfigs.add(new AssigneeConfig(list.getObject(i)));
		}
		notification = config.getObject("notification");
	}

	public void execute(ProcessInstance pi) throws RedbackException
	{
		JSONObject notificationMsg = new JSONObject();
		notificationMsg.put("extpid", pi.getId());
		notificationMsg.put("message", notification.getString("message"));
		notificationMsg.put("actions", getActions());
		for(int i = 0; i < assigneeConfigs.size(); i++)
		{
			Assignee assignee = new Assignee(assigneeConfigs.get(i).getType(), assigneeConfigs.get(i).evaluateId(pi));
			pi.addAssignee(assignee);
			if(assignee.getType() == Assignee.PROCESS  &&  notification.getString("method").equals("rbprocessnotification"))
				processManager.notifyProcess(processManager.getSystemUserProfile(), pi.getId(), assignee.getId(), notificationMsg);
			//TODO: Add more notificatio methods
		}
	}

	public void processAction(UserProfile up, String extpid, ProcessInstance pi, String action, JSONObject data) throws RedbackException
	{
		boolean foundAction = false;
		if(isAssignee(up, extpid, pi))
		{
			for(int i = 0; i < actions.size(); i++)
			{
				if(actions.getObject(i).getString("action").equals(action))
				{
					foundAction = true;
					String nextNode = actions.getObject(i).getString("nextnode");
					pi.clearAssignees();
					pi.setCurrentNode(nextNode);
				}
			}

			if(!foundAction)
			{
				error("No such action available for this process");
			}
		}
		else
		{
			error("Actionning user or process is not a current assignee");
		}		
	}

	public JSONList getActions(UserProfile up, String extpid, ProcessInstance pi)
	{
		if(isAssignee(up, extpid, pi))
		{
			return getActions();
		}
		return null;
	}
	
	protected JSONList getActions()
	{
		JSONList list = new JSONList();
		for(int i = 0; i < actions.size(); i++)
		{
			JSONObject action = new JSONObject();
			action.put("action", actions.getObject(i).getString("action"));
			action.put("description", actions.getObject(i).getString("description"));
			list.add(action);
		}
		return list;
	}
	
	protected boolean isAssignee(UserProfile up, String extpid, ProcessInstance pi)
	{
		boolean isAssignee = false;

		ArrayList<Assignee> assignees = pi.getAssignees();
		for(int i = 0; i < assignees.size(); i++)
		{
			Assignee assignee = assignees.get(i);
			if(assignee.getType() == Assignee.PROCESS  &&  assignee.getId().equals(extpid)  &&  up.getAttribute("rb.process.sysuser").equals("true"))
				isAssignee = true;
			if(assignee.getType() == Assignee.USER  && assignee.getId().equals(up.getUsername()))
				isAssignee = true;
			//TODO: Need to add groups
		}
		return isAssignee;
	}
}
