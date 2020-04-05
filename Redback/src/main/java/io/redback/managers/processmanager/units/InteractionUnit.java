package io.redback.managers.processmanager.units;

import java.util.ArrayList;

import io.firebus.utils.DataList;
import io.firebus.utils.DataMap;
import io.redback.RedbackException;
import io.redback.managers.processmanager.Actionner;
import io.redback.managers.processmanager.Assignee;
import io.redback.managers.processmanager.AssigneeConfig;
import io.redback.managers.processmanager.Assignment;
import io.redback.managers.processmanager.ProcessInstance;
import io.redback.managers.processmanager.ProcessManager;
import io.redback.managers.processmanager.ProcessUnit;

public class InteractionUnit extends ProcessUnit 
{
	protected DataList actionsConfig;
	protected String assigneeType;
	protected ArrayList<AssigneeConfig> assigneeConfigs;
	protected DataMap notificationConfig;

	public InteractionUnit(ProcessManager pm, DataMap config) throws RedbackException 
	{
		super(pm, config);
		actionsConfig = config.getList("actions");
		assigneeConfigs = new ArrayList<AssigneeConfig>();
		if(config.containsKey("assignees")  &&  config.get("assignees") instanceof DataList)
		{
			DataList list = config.getList("assignees");
			for(int i = 0; i < list.size(); i++)
				assigneeConfigs.add(new AssigneeConfig(list.getObject(i)));
		}
		notificationConfig = config.getObject("notification");
	}

	public void execute(ProcessInstance pi) throws RedbackException
	{
		logger.info("Starting interaction node execution");
		for(int i = 0; i < assigneeConfigs.size(); i++)
		{
			AssigneeConfig assigneeConfig = assigneeConfigs.get(i);
			Object assigneeObject = assigneeConfig.evaluateId(pi);
			if(assigneeObject instanceof String)
			{
				logger.fine("Adding assignee " + (String)assigneeObject);
				pi.addAssignee(new Assignee(assigneeConfig.getType(), (String)assigneeObject));
			}
			else if(assigneeObject instanceof DataList)
			{
				DataList assigneeList = (DataList)assigneeObject;
				for(int j = 0; j < assigneeList.size(); j++)
				{
					logger.fine("Adding assignee " + assigneeList.getString(j));
					pi.addAssignee(new Assignee(assigneeConfigs.get(i).getType(), assigneeList.getString(j)));
				}
			}
			//TODO: handle active notifications (email)
		}
		logger.info("Finished interaction node execution");
	}

	public void processAction(Actionner actionner, ProcessInstance pi, String action, DataMap data) throws RedbackException
	{
		logger.info("Starting interaction node action");
		boolean foundAction = false;
		if(isAssignee(actionner, pi))
		{
			for(int i = 0; i < actionsConfig.size(); i++)
			{
				if(actionsConfig.getObject(i).getString("action").equals(action))
				{
					logger.fine("Actionning interaction with '" + action + "'");
					foundAction = true;
					String nextNode = actionsConfig.getObject(i).getString("nextnode");
					pi.clearAssignees();
					pi.setLastActioner(actionner);
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
	
	public Assignment getNotification(Actionner actionner, ProcessInstance pi)
	{
		if(isAssignee(actionner, pi))
		{
			return getNotification(pi);
		}
		return null;
	}
	
	protected Assignment getNotification(ProcessInstance pi)
	{
		Assignment assignment = new Assignment(pi.getProcessName(), pi.getId(), notificationConfig.getString("code"), notificationConfig.getString("message"));
		for(int i = 0; i < actionsConfig.size(); i++)
			assignment.addAction(actionsConfig.getObject(i).getString("action"), actionsConfig.getObject(i).getString("description"));
		return assignment;		
	}
	
	protected boolean isAssignee(Actionner actionner, ProcessInstance pi)
	{
		boolean isAssignee = false;

		ArrayList<Assignee> assignees = pi.getAssignees();
		for(int i = 0; i < assignees.size(); i++)
		{
			Assignee assignee = assignees.get(i);
			if(assignee.getType() == actionner.getType() && assignee.getId().equals(actionner.getId()))
				isAssignee = true;
		}
		return isAssignee;
	}
}
