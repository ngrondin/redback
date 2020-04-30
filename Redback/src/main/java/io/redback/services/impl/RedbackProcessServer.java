package io.redback.services.impl;

import java.util.List;

import io.firebus.Firebus;
import io.firebus.utils.DataList;
import io.firebus.utils.DataMap;
import io.redback.RedbackException;
import io.redback.managers.processmanager.Actionner;
import io.redback.managers.processmanager.Assignment;
import io.redback.managers.processmanager.ProcessInstance;
import io.redback.managers.processmanager.ProcessManager;
import io.redback.security.Session;
import io.redback.services.ProcessServer;

public class RedbackProcessServer extends ProcessServer 
{
	protected ProcessManager processManager;


	public RedbackProcessServer(String n, DataMap c, Firebus f) 
	{
		super(n, c, f);
		processManager = new ProcessManager(firebus, config);
	}
	
	protected ProcessInstance initiate(Session session, String process, String domain, DataMap data) throws RedbackException
	{
		Actionner actionner = new Actionner(session.getUserProfile());
		processManager.initiateCurrentTransaction();
		ProcessInstance pi = processManager.initiateProcess(actionner, process, domain, data);
		processManager.commitCurrentTransaction();
		return pi;
	}
	
	protected void processAction(Session session, String pid, String processAction, DataMap data) throws RedbackException
	{
		Actionner actionner = new Actionner(session.getUserProfile());
		processManager.initiateCurrentTransaction();
		processManager.processAction(actionner, pid, processAction, data);
		processManager.commitCurrentTransaction();
	}
	
	protected List<Assignment> getAssignments(Session session, DataMap filter, DataList viewdata) throws RedbackException
	{
		Actionner actionner = new Actionner(session.getUserProfile());
		processManager.initiateCurrentTransaction();
		List<Assignment> result = processManager.getAssignments(actionner, filter, viewdata);
		processManager.commitCurrentTransaction();
		return result;
	}
	
	protected int getAssignmentCount(Session session) throws RedbackException {
		Actionner actionner = new Actionner(session.getUserProfile());
		processManager.initiateCurrentTransaction();
		int count = processManager.getAssignmentCount(actionner);
		processManager.commitCurrentTransaction();
		return count;
	}

	public void clearCaches()
	{
		processManager.refreshAllConfigs();			
	}


}
