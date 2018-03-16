package com.nic.redback.services.processserver.units;

import java.util.ArrayList;
import java.util.Iterator;

import com.nic.firebus.utils.JSONObject;
import com.nic.redback.RedbackException;
import com.nic.redback.services.processserver.ProcessInstance;
import com.nic.redback.services.processserver.ProcessManager;
import com.nic.redback.services.processserver.ProcessUnit;
import com.nic.redback.security.UserProfile;

public class InteractionUnit extends ProcessUnit 
{
	protected JSONObject actions;
	protected String assigneeType;
	protected String assignee;

	public InteractionUnit(ProcessManager pm, JSONObject config) 
	{
		super(pm, config);
		actions = config.getObject("actions");
		assigneeType = config.getString("assigneetype");
		assignee = config.getString("assignee");
	}

	public void execute(UserProfile up, ProcessInstance pi) throws RedbackException
	{
		pi.setAssignee(assigneeType, assignee);
	}

	public void processAction(UserProfile up, ProcessInstance pi, String action, JSONObject data) throws RedbackException
	{
		if(actions.containsKey(action))
		{
			String nextNode = actions.getObject(action).getString("nextnode");
			pi.setCurrentNode(nextNode);
		}
		else
		{
			throw new RedbackException("No such action available for this process");
		}
	}
	
	public ArrayList<String[]> getActions()
	{
		ArrayList<String[]> retVal = new ArrayList<String[]>();
		Iterator<String> it = actions.keySet().iterator();
		while(it.hasNext())
		{
			String key = it.next();
			retVal.add(new String[]{key, actions.getObject(key).getString("name")});
		}
		return retVal;
	}
}
