package io.redback.managers.processmanager.units;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.firebus.data.DataList;
import io.firebus.data.DataMap;
import io.firebus.logging.Logger;
import io.firebus.script.Expression;
import io.firebus.script.ScriptContext;
import io.firebus.script.exceptions.ScriptException;
import io.redback.exceptions.RedbackException;
import io.redback.exceptions.RedbackInvalidRequestException;
import io.redback.managers.processmanager.ActionConfig;
import io.redback.managers.processmanager.Actionner;
import io.redback.managers.processmanager.Assignee;
import io.redback.managers.processmanager.AssigneeConfig;
import io.redback.managers.processmanager.Notification;
import io.redback.managers.processmanager.ProcessInstance;
import io.redback.managers.processmanager.ProcessManager;
import io.redback.managers.processmanager.ProcessUnit;
import io.redback.managers.processmanager.RawNotification;
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
	protected Expression contextLabelExpression;

	public InteractionUnit(ProcessManager pm, Process p, DataMap config) throws RedbackException 
	{
		super(pm, p, config);
		try {
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
			labelExpression = pm.getScriptFactory().createExpression(jsFunctionNameRoot + "_labelexpr", notificationConfig.containsKey("label") ? notificationConfig.getString("label") : "'No Label'");
			messageExpression = pm.getScriptFactory().createExpression(jsFunctionNameRoot + "_msgexpr", notificationConfig.containsKey("message") ? notificationConfig.getString("message") : "'No Message'");
			contextLabelExpression =  pm.getScriptFactory().createExpression(jsFunctionNameRoot + "_clexpr", notificationConfig.containsKey("contextlabel") ? notificationConfig.getString("contextlabel") : "null");
			nextNodeInterruption = config.getString("interruption");
		} catch(Exception e) {
			throw new RedbackException("Error initialising interaction unit", e);
		}
	}

	public void execute(ProcessInstance pi) throws RedbackException
	{
		Logger.finer("rb.process.interaction.start", null);
		DataMap interactionDetails = new DataMap();
		interactionDetails.put("code", notificationConfig.getString("code"));
		interactionDetails.put("type", notificationConfig.containsKey("type") ? notificationConfig.getString("type") : "exception");
		pi.setInteractionDetails(interactionDetails);
		List<Assignee> assignees = getAssignes(pi);
		for(Assignee assignee : assignees)
			pi.addAssignee(assignee);
		RawNotification rn = getRawNotification(pi);
		List<String> usernames = getAssigneeUsernames(pi, assignees);
		Map<String, Notification> sendMap = new HashMap<String, Notification>();
		if(usernames.size() > 0) 
		{
			for(String username: usernames)
				sendMap.put(username, rn.getNotificationForActionner(new Actionner(username)));
			pi.getProcessManager().sendNotification(sendMap);
		}
		Logger.finer("rb.process.interaction.finish", null);
		
	}
	
	public void interrupt(Actionner actionner, ProcessInstance pi) throws RedbackException
	{
		Logger.finer("rb.process.interaction.interrupt.start", null);
		if(nextNodeInterruption != null) {
			pi.clearAssignees();
			pi.clearInteractionDetails();
			sendCompletion(pi);
			pi.setCurrentNode(nextNodeInterruption);
		}
		Logger.finer("rb.process.interaction.interrupt.finish", null);
	}

	public void action(Actionner actionner, ProcessInstance pi, String action, Date date, DataMap data) throws RedbackException
	{
		Logger.finer("rb.process.interaction.action.start", null);
		boolean foundAction = false;
		if(isAssignee(actionner, pi))
		{
			for(int i = 0; i < actionConfigs.size(); i++)
			{
				ActionConfig actionConfig = actionConfigs.get(i);
				if(actionConfig.getActionName().equals(action))
				{
					Logger.fine("rb.process.interaction.action", new DataMap("action", action));
					foundAction = true;
					pi.clearAssignees();
					pi.clearInteractionDetails();
					pi.setLastAction(actionner, date, action);
					sendCompletion(pi);
					pi.setCurrentNode(actionConfig.getNextNode());
				}
			}

			if(!foundAction)
			{
				throw new RedbackInvalidRequestException("No such action (" + action + ") available for process " + pi.getId());
			}
		}
		else
		{
			throw new RedbackInvalidRequestException("Actionning user or process is not a current assignee");
		}		
		Logger.finer("rb.process.interaction.action.finish", null);
	}
	
	public Notification getNotificationForActionner(Actionner actionner, ProcessInstance pi) throws RedbackException
	{
		if(isAssignee(actionner, pi)) 
		{
			RawNotification rn = getRawNotification(pi);
			return rn.getNotificationForActionner(actionner);
		}
		else
		{
			return null;
		}
	}
	
	private void sendCompletion(ProcessInstance pi) throws RedbackException 
	{
		List<String> usernames = getAssigneeUsernames(pi, getAssignes(pi));
		if(usernames.size() > 0)
			pi.getProcessManager().sendInteractionCompletion(pi.getProcessName(), pi.getId(), notificationConfig.getString("code"), usernames);
	}
	
	private List<Assignee> getAssignes(ProcessInstance pi) throws RedbackException {
		List<Assignee> assignees = new ArrayList<Assignee>();
		ScriptContext context = pi.getScriptContext();
		for(AssigneeConfig assigneeConfig : assigneeConfigs)
		{
			Object assigneeObject = assigneeConfig.evaluateId(context);
			if(assigneeObject instanceof String)
			{
				assignees.add(new Assignee(assigneeConfig.getType(), (String)assigneeObject));
			}
			else if(assigneeObject instanceof DataList)
			{
				DataList assigneeList = (DataList)assigneeObject;
				for(int j = 0; j < assigneeList.size(); j++)
					assignees.add(new Assignee(assigneeConfig.getType(), assigneeList.getString(j)));
			}
		}
		return assignees;
	}
	
	private List<String> getAssigneeUsernames(ProcessInstance pi, List<Assignee> assignees) throws RedbackException {
		List<String> usernames = new ArrayList<String>();
		for(Assignee assignee : assignees) {
			if(assignee.getType() == Assignee.USER) 
			{
				if(!usernames.contains(assignee.getId()))
					usernames.add(assignee.getId());
			}
			else if(assignee.getType() == Assignee.GROUP)
			{
				List<String> users = pi.getProcessManager().getUsersOfGroup(pi.getDomain(), assignee.getId());
				for(String username : users) { 
					if(!usernames.contains(username))
						usernames.add(username);
				}
			}			
		}
		return usernames;
	}
	

	
	private RawNotification getRawNotification(ProcessInstance pi) throws RedbackException 
	{
		try {
			ScriptContext context = pi.getScriptContext();
			String code = notificationConfig.getString("code");
			String type = notificationConfig.containsKey("type") ? notificationConfig.getString("type") : "exception";
			String label = (String)labelExpression.eval(context);
			String message = (String)messageExpression.eval(context);
			String contextLabel = (String)contextLabelExpression.eval(context);
			RawNotification notification = new RawNotification(pi.getProcessName(), pi.getId(), code, type, label, message, contextLabel);
			for(ActionConfig actionConfig: actionConfigs)
			{
				String[] exclusiveAppliesTo = null;
				if(actionConfig.isExclusive()) {
					Object exclusiveValue = actionConfig.evaluateExclusiveId(pi);
					if(exclusiveValue instanceof String) {
						exclusiveAppliesTo = new String[1];
						exclusiveAppliesTo[0] = (String)exclusiveValue;
					} else if(exclusiveValue instanceof DataList) {
						DataList list = (DataList)exclusiveValue;
						exclusiveAppliesTo = new String[list.size()];
						for(int i = 0; i < list.size(); i++) {
							exclusiveAppliesTo[i] = list.getString(i);
						}
					}
				}
				notification.addAction(actionConfig.getActionName(), actionConfig.getActionDescription(), actionConfig.isMain(), exclusiveAppliesTo);
			}
			if(pi.getData() != null && pi.getData().containsKey("objectname") && pi.getData().containsKey("uid")) {
				notification.addData("objectname", pi.getData().getString("objectname"));
				notification.addData("uid", pi.getData().getString("uid"));
			}
			return notification;
		} catch(ScriptException e) {
			throw new RedbackException("Error generating raw notification", e);
		}
	}
	
	private boolean isAssignee(Actionner actionner, ProcessInstance pi)
	{
		if(actionner.getId().equals(pi.getProcessManager().getSysUserManager().getUsername())) 
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
	
	private boolean assigneeMatch(Actionner actionner, Assignee assignee)
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
