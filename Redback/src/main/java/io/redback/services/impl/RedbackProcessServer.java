package io.redback.services.impl;

import java.util.Date;
import java.util.List;

import io.firebus.Firebus;
import io.firebus.data.DataList;
import io.firebus.data.DataMap;
import io.redback.exceptions.RedbackException;
import io.redback.managers.processmanager.Actionner;
import io.redback.managers.processmanager.Notification;
import io.redback.managers.processmanager.ProcessInstance;
import io.redback.managers.processmanager.ProcessManager;
import io.redback.security.Session;
import io.redback.services.ProcessServer;

public class RedbackProcessServer extends ProcessServer
{
	protected ProcessManager processManager;


	public RedbackProcessServer(String n, DataMap c, Firebus f) throws RedbackException 
	{
		super(n, c, f);
		processManager = new ProcessManager(firebus, config);
	}
	
	public void configure() {
		super.configure();
		processManager.refreshAllConfigs();		
	}	
	
	protected void startTransaction(Session session, boolean store) throws RedbackException {
		processManager.initiateCurrentTransaction(session, store);
	}

	protected void commitTransaction(Session session) throws RedbackException {
		processManager.commitCurrentTransaction(session);
	}
	
	protected ProcessInstance initiate(Session session, String process, String domain, DataMap data) throws RedbackException
	{
		Actionner actionner = new Actionner(session);
		ProcessInstance pi = processManager.initiateProcess(actionner, process, domain, data);
		return pi;
	}
	
	protected void continueProcess(Session session, String pid) throws RedbackException
	{
		Actionner actionner = new Actionner(session);
		processManager.continueProcess(actionner, pid);
	}
	
	protected void actionProcess(Session session, String pid, String processAction, Date date, DataMap data) throws RedbackException
	{
		Actionner actionner = new Actionner(session);
		processManager.actionProcess(actionner, pid, processAction, date, data);
	}
	
	protected void interruptProcess(Session session, String pid) throws RedbackException {
		Actionner actionner = new Actionner(session);
		processManager.interruptProcess(actionner, pid);
	}

	protected void interruptProcesses(Session session, DataMap filter) throws RedbackException {
		Actionner actionner = new Actionner(session);
		List<ProcessInstance> list = processManager.findProcesses(actionner, filter, 0, 50);
		for(ProcessInstance pi: list) {
			processManager.interruptProcess(actionner, pi.getId());
		}
	}


	protected List<Notification> getAssignments(Session session, DataMap filter, DataList viewdata, int page, int pageSize) throws RedbackException
	{
		Actionner actionner = new Actionner(session);
		List<Notification> result = processManager.getNotifications(actionner, filter, viewdata, page, pageSize);
		return result;
	}
	
	protected int getAssignmentCount(Session session, DataMap filter) throws RedbackException {
		Actionner actionner = new Actionner(session);
		int count = processManager.getNotificationCount(actionner, filter);
		return count;
	}

	protected void runCron(Session session) throws RedbackException {
		Actionner actionner = new Actionner(session);
		processManager.runCron(actionner);
	}

}
