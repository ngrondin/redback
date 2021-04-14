package io.redback.managers.processmanager.units;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import io.firebus.utils.DataList;
import io.firebus.utils.DataMap;
import io.redback.RedbackException;
import io.redback.managers.jsmanager.Expression;
import io.redback.managers.processmanager.ActionConfig;
import io.redback.managers.processmanager.Actionner;
import io.redback.managers.processmanager.Assignee;
import io.redback.managers.processmanager.AssigneeConfig;
import io.redback.managers.processmanager.ProcessInstance;
import io.redback.managers.processmanager.ProcessManager;
import io.redback.managers.processmanager.ProcessUnit;
import io.redback.utils.Notification;
import io.redback.managers.processmanager.Process;

public class InteractionUnit extends ProcessUnit 
{
	protected String assigneeType;
	protected ArrayList<AssigneeConfig> assigneeConfigs;
	protected ArrayList<ActionConfig> actionConfigs;
	protected String nextNodeInterruption;
	protected DataMap notificationConfig;
	protected Expression labelExpression;
	protected Expression messageExpression;

	public InteractionUnit(ProcessManager pm, Process p, DataMap config) throws RedbackException 
	{
		super(pm, p, config);
		assigneeConfigs = new ArrayList<AssigneeConfig>();
		if(config.containsKey("assignees")  &&  config.get("assignees") instanceof DataList)
		{
			DataList list = config.getList("assignees");
			for(int i = 0; i < list.size(); i++)
				assigneeConfigs.add(new AssigneeConfig(processManager, list.getObject(i)));
		}
		actionConfigs = new ArrayList<ActionConfig>();
		if(config.containsKey("actions")  &&  config.get("actions") instanceof DataList)
		{
			DataList list = config.getList("actions");
			for(int i = 0; i < list.size(); i++)
				actionConfigs.add(new ActionConfig(processManager, p, this, list.getObject(i)));
		}
		notificationConfig = config.getObject("notification");
		labelExpression = new Expression(pm.getJSManager(), jsFunctionNameRoot + "_labelexpr", pm.getScriptVariableNames(), notificationConfig.containsKey("label") ? notificationConfig.getString("label") : "'No Label'");
		messageExpression = new Expression(pm.getJSManager(), jsFunctionNameRoot + "_msgexpr", pm.getScriptVariableNames(), notificationConfig.containsKey("message") ? notificationConfig.getString("message") : "'No Message'");
		nextNodeInterruption = config.getString("interruption");
	}

	public void execute(ProcessInstance pi) throws RedbackException
	{
		logger.finer("Starting interaction node execution");
		Map<String, Object> context = pi.getScriptContext();
		DataMap interactionDetails = new DataMap();
		interactionDetails.put("code", notificationConfig.getString("code"));
		interactionDetails.put("type", notificationConfig.containsKey("type") ? notificationConfig.getString("type") : "exception");
		pi.setInteractionDetails(interactionDetails);
		
		for(int i = 0; i < assigneeConfigs.size(); i++)
		{
			AssigneeConfig assigneeConfig = assigneeConfigs.get(i);
			Object assigneeObject = assigneeConfig.evaluateId(context);
			if(assigneeObject instanceof String)
			{
				addAssigneeAndSendNotification(pi, assigneeConfig.getType(), (String)assigneeObject);
			}
			else if(assigneeObject instanceof DataList)
			{
				DataList assigneeList = (DataList)assigneeObject;
				for(int j = 0; j < assigneeList.size(); j++)
					addAssigneeAndSendNotification(pi, assigneeConfigs.get(i).getType(), assigneeList.getString(j));
			}
		}
		logger.finer("Finished interaction node execution");
	}
	
	private void addAssigneeAndSendNotification(ProcessInstance pi, int assigneeType, String assigneeId) throws RedbackException 
	{
		logger.finer("Adding assignee " + assigneeId);
		pi.addAssignee(new Assignee(assigneeType, assigneeId));
		
		if(assigneeType == Assignee.USER || assigneeType == Assignee.GROUP) 
		{
			Map<String, Object> context = pi.getScriptContext();
			String code = notificationConfig.getString("code");
			String type = notificationConfig.containsKey("type") ? notificationConfig.getString("type") : "exception";
			String label = (String)labelExpression.eval(context);
			String message = (String)messageExpression.eval(context);
			Notification notification = new Notification(pi.getProcessName(), pi.getId(), code, type, label, message);
			for(ActionConfig actionConfig: actionConfigs)
				if(!actionConfig.isExclusive() || (actionConfig.isExclusive() && assigneeId.equals((String)actionConfig.evaluateExclusiveId(pi))))
					notification.addAction(actionConfig.getActionName(), actionConfig.getActionDescription(), actionConfig.isMain());
			
			if(assigneeType == Assignee.USER) 
			{
				notification.addTo(assigneeId);
			}
			else if(assigneeType == Assignee.GROUP)
			{
				List<String> users = pi.getProcessManager().getUsersOfGroup(pi.getDomain(), assigneeId);
				for(String username : users) 
					notification.addTo(username);
			}
			
			pi.getProcessManager().sendNotification(notification);
		}
	}
	
	public void interrupt(Actionner actionner, ProcessInstance pi) throws RedbackException
	{
		logger.finer("Starting interaction node interruption");
		if(nextNodeInterruption != null) {
			pi.clearAssignees();
			pi.clearInteractionDetails();
			pi.setCurrentNode(nextNodeInterruption);
		}
		logger.finer("Finished interaction node interruption");
	}

	public void action(Actionner actionner, ProcessInstance pi, String action, DataMap data) throws RedbackException
	{
		logger.finer("Starting interaction node action");
		boolean foundAction = false;
		if(isAssignee(actionner, pi))
		{
			for(int i = 0; i < actionConfigs.size(); i++)
			{
				ActionConfig actionConfig = actionConfigs.get(i);
				if(actionConfig.getActionName().equals(action))
				{
					logger.fine("Actionning interaction with '" + action + "'");
					foundAction = true;
					pi.clearAssignees();
					pi.clearInteractionDetails();
					pi.setLastActioner(actionner);
					pi.setCurrentNode(actionConfig.getNextNode());
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
		logger.finer("Finished interaction node action");
	}
	
	public Notification getAssignment(Actionner actionner, ProcessInstance pi) throws RedbackException
	{
		if(isAssignee(actionner, pi))
		{
			Map<String, Object> context = pi.getScriptContext();
			String code = notificationConfig.getString("code");
			String type = notificationConfig.containsKey("type") ? notificationConfig.getString("type") : "exception";
			String label = (String)labelExpression.eval(context);
			String message = (String)messageExpression.eval(context);
			Notification notification = new Notification(pi.getProcessName(), pi.getId(), code, type, label, message);
			for(ActionConfig actionConfig: actionConfigs)
			{
				if(!actionConfig.isExclusive() || (actionConfig.isExclusive() && assigneeMatch(actionner, pi.getAssigneeById((String)actionConfig.evaluateExclusiveId(pi)))))
					notification.addAction(actionConfig.getActionName(), actionConfig.getActionDescription(), actionConfig.isMain());
			}
			notification.addTo(actionner.getId());
			return notification;
		}
		return null;
	}
	/*
	protected Notification getNotification(ProcessInstance pi) throws RedbackException 
	{

	}
	*/
	protected boolean isAssignee(Actionner actionner, ProcessInstance pi)
	{
		if(actionner.getId().equals(pi.getProcessManager().getProcessUsername())) 
		{
			return true;
		} 
		else 
		{
			for(Assignee assignee: pi.getAssignees())
				if(assigneeMatch(actionner, assignee))
					return true;
			return false;
		}
	}
	
	protected boolean assigneeMatch(Actionner actionner, Assignee assignee)
	{
		if(assignee != null && actionner != null) {
			if(assignee.getType() == Assignee.GROUP && actionner.isInGroup(assignee.getId())) {
				return true;
			} else if(assignee.getType() == actionner.getType()) {
				if(assignee.getId().equals(actionner.getId())) {
					return true;
				} else if(assignee.getId().equals("*")) {
					return true;
				} else {
					return false;
				}
			} else { 
				return false;
			}
		} else {
			return false;
		}
	}
	
	
}
