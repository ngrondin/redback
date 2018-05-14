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
import com.nic.redback.security.Session;
import com.nic.redback.security.UserProfile;

public class InteractionUnit extends ProcessUnit 
{
	protected JSONList actionsConfig;
	protected String assigneeType;
	protected ArrayList<AssigneeConfig> assigneeConfigs;
	protected JSONObject notificationConfig;

	public InteractionUnit(ProcessManager pm, JSONObject config) throws RedbackException 
	{
		super(pm, config);
		actionsConfig = config.getList("actions");
		assigneeConfigs = new ArrayList<AssigneeConfig>();
		if(config.containsKey("assignees")  &&  config.get("assignees") instanceof JSONList)
		{
			JSONList list = config.getList("assignees");
			for(int i = 0; i < list.size(); i++)
				assigneeConfigs.add(new AssigneeConfig(list.getObject(i)));
		}
		notificationConfig = config.getObject("notification");
	}

	public void execute(ProcessInstance pi, JSONObject result) throws RedbackException
	{
		logger.info("Starting interaction node execution");
		for(int i = 0; i < assigneeConfigs.size(); i++)
		{
			AssigneeConfig assigneeConfig = assigneeConfigs.get(i);
			Object assigneeObject = assigneeConfig.evaluateId(pi);
			if(assigneeObject instanceof String)
			{
				logger.fine("Adding assignee " + (String)assigneeObject);
				addAssignee(pi, new Assignee(assigneeConfigs.get(i).getType(), (String)assigneeObject));
			}
			else if(assigneeObject instanceof JSONList)
			{
				JSONList assigneeList = (JSONList)assigneeObject;
				for(int j = 0; j < assigneeList.size(); j++)
				{
					logger.fine("Adding assignee " + assigneeList.getString(j));
					addAssignee(pi, new Assignee(assigneeConfigs.get(i).getType(), assigneeList.getString(j)));
				}
			}
		}
		logger.info("Finished interaction node execution");
	}

	public void processAction(Session session, String extpid, ProcessInstance pi, String action, JSONObject data) throws RedbackException
	{
		logger.info("Starting interaction node action");
		boolean foundAction = false;
		if(isAssignee(session.getUserProfile(), extpid, pi))
		{
			for(int i = 0; i < actionsConfig.size(); i++)
			{
				if(actionsConfig.getObject(i).getString("action").equals(action))
				{
					logger.fine("Actionning interaction with '" + action + "'");
					foundAction = true;
					String nextNode = actionsConfig.getObject(i).getString("nextnode");
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
		logger.info("Finished interaction node action");
	}
	
	protected void addAssignee(ProcessInstance pi, Assignee assignee) throws RedbackException
	{
		pi.addAssignee(assignee);
		if(assignee.getType() == Assignee.PROCESS  &&  notificationConfig.getString("method") != null  &&  notificationConfig.getString("method").equals("rbprocessnotification"))
			processManager.notifyProcess(processManager.getSystemUserSession(pi.getDomain()), pi.getId(), assignee.getId(), getNotification(pi));
		//TODO: Add more notification methods
		
	}

	public JSONObject getNotification(Session session, String extpid, ProcessInstance pi)
	{
		if(isAssignee(session.getUserProfile(), extpid, pi))
		{
			return getNotification(pi);
		}
		return null;
	}
	
	protected JSONObject getNotification(ProcessInstance pi)
	{
		JSONObject notificationMsg = new JSONObject();
		notificationMsg.put("process", pi.getProcessName());
		notificationMsg.put("pid", pi.getId());
		notificationMsg.put("interaction", notificationConfig.getString("code"));
		notificationMsg.put("message", notificationConfig.getString("message"));
		
		JSONList actionList = new JSONList();
		for(int i = 0; i < actionsConfig.size(); i++)
		{
			JSONObject action = new JSONObject();
			action.put("action", actionsConfig.getObject(i).getString("action"));
			action.put("description", actionsConfig.getObject(i).getString("description"));
			actionList.add(action);
		}
		notificationMsg.put("actions", actionList);
		return notificationMsg;		
	}
	
	/*
	protected JSONList getNotification()
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
	*/
	
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
