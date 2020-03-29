package io.redback.managers.processmanager.units;

import java.util.ArrayList;

import io.firebus.utils.DataList;
import io.firebus.utils.DataMap;
import io.redback.RedbackException;
import io.redback.managers.processmanager.Assignee;
import io.redback.managers.processmanager.AssigneeConfig;
import io.redback.managers.processmanager.Assignment;
import io.redback.managers.processmanager.ProcessInstance;
import io.redback.managers.processmanager.ProcessManager;
import io.redback.managers.processmanager.ProcessUnit;
import io.redback.security.Session;
import io.redback.security.UserProfile;

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

	public void execute(ProcessInstance pi, DataMap result) throws RedbackException
	{
		logger.info("Starting interaction node execution");
		for(int i = 0; i < assigneeConfigs.size(); i++)
		{
			AssigneeConfig assigneeConfig = assigneeConfigs.get(i);
			Object assigneeObject = assigneeConfig.evaluateId(pi);
			if(assigneeObject instanceof String)
			{
				logger.fine("Adding assignee " + (String)assigneeObject);
				pi.addAssignee(new Assignee(assigneeConfigs.get(i).getType(), (String)assigneeObject));
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
		}
		logger.info("Finished interaction node execution");
	}

	public void processAction(Session session, String extpid, ProcessInstance pi, String action, DataMap data) throws RedbackException
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
					pi.setLastActioner(new Assignee(session.getUserProfile().getUsername(), extpid));
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
	
	public Assignment getNotification(Session session, String extpid, ProcessInstance pi)
	{
		if(isAssignee(session.getUserProfile(), extpid, pi))
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