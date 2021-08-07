package io.redback.services.impl;

import java.util.Date;
import java.util.List;

import io.firebus.Firebus;
import io.firebus.utils.DataList;
import io.firebus.utils.DataMap;
import io.redback.exceptions.RedbackException;
import io.redback.managers.processmanager.Actionner;
import io.redback.managers.processmanager.ProcessInstance;
import io.redback.managers.processmanager.ProcessManager;
import io.redback.security.Session;
import io.redback.services.ProcessServer;
import io.redback.utils.Notification;

public class RedbackProcessServer extends ProcessServer
{
	protected ProcessManager processManager;


	public RedbackProcessServer(String n, DataMap c, Firebus f) 
	{
		super(n, c, f);
		processManager = new ProcessManager(firebus, config);
	}
	
	public void configure() {
		processManager.refreshAllConfigs();		
	}

	public void start() {
		
	}	
	
	protected ProcessInstance initiate(Session session, String process, String domain, DataMap data) throws RedbackException
	{
		Actionner actionner = new Actionner(session);
		processManager.initiateCurrentTransaction();
		ProcessInstance pi = processManager.initiateProcess(actionner, process, domain, data);
		processManager.commitCurrentTransaction();
		return pi;
	}
	
	protected void continueProcess(Session session, String pid) throws RedbackException
	{
		Actionner actionner = new Actionner(session);
		processManager.initiateCurrentTransaction();
		processManager.continueProcess(actionner, pid);
		processManager.commitCurrentTransaction();
	}
	
	protected void actionProcess(Session session, String pid, String processAction, Date date, DataMap data) throws RedbackException
	{
		Actionner actionner = new Actionner(session);
		processManager.initiateCurrentTransaction();
		processManager.actionProcess(actionner, pid, processAction, date, data);
		processManager.commitCurrentTransaction();
	}
	
	protected void interruptProcess(Session session, String pid) throws RedbackException {
		Actionner actionner = new Actionner(session);
		processManager.initiateCurrentTransaction();
		processManager.interruptProcess(actionner, pid);
		processManager.commitCurrentTransaction();
	}

	protected void interruptProcesses(Session session, DataMap filter) throws RedbackException {
		Actionner actionner = new Actionner(session);
		processManager.initiateCurrentTransaction();
		List<ProcessInstance> list = processManager.findProcesses(actionner, filter, 0, 50);
		for(ProcessInstance pi: list) {
			processManager.interruptProcess(actionner, pi.getId());
		}
		processManager.commitCurrentTransaction();		
	}


	protected List<Notification> getAssignments(Session session, DataMap filter, DataList viewdata, int page, int pageSize) throws RedbackException
	{
		Actionner actionner = new Actionner(session);
		processManager.initiateCurrentTransaction();
		List<Notification> result = processManager.getNotifications(actionner, filter, viewdata, page, pageSize);
		processManager.commitCurrentTransaction();
		return result;
	}
	
	protected int getAssignmentCount(Session session, DataMap filter) throws RedbackException {
		Actionner actionner = new Actionner(session);
		processManager.initiateCurrentTransaction();
		int count = processManager.getNotificationCount(actionner, filter);
		processManager.commitCurrentTransaction();
		return count;
	}

}
