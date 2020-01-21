package com.nic.redback.services.impl;

import java.util.List;

import com.nic.firebus.Firebus;
import com.nic.firebus.utils.DataList;
import com.nic.firebus.utils.DataMap;
import com.nic.redback.RedbackException;
import com.nic.redback.managers.processmanager.Assignment;
import com.nic.redback.managers.processmanager.ProcessManager;
import com.nic.redback.security.Session;
import com.nic.redback.services.ProcessServer;

public class RedbackProcessServer extends ProcessServer 
{
	protected ProcessManager processManager;


	public RedbackProcessServer(String n, DataMap c, Firebus f) 
	{
		super(n, c, f);
		processManager = new ProcessManager(firebus, config);
	}
	
	protected DataMap initiate(Session session, String process, DataMap data) throws RedbackException
	{
		DataMap responseData = processManager.initiateProcess(session, process, data);
		processManager.commitCurrentTransaction();
		return responseData;
	}
	
	protected DataMap processAction(Session session, String extpid, String pid, String processAction, DataMap data) throws RedbackException
	{
		DataMap responseData = processManager.processAction(session, extpid, pid, processAction, data);
		processManager.commitCurrentTransaction();
		return responseData;
	}
	
	protected List<Assignment> getAssignments(Session session, String extpid, DataMap filter, DataList viewdata) throws RedbackException
	{
		List<Assignment> result = processManager.getAssignments(session, extpid, filter, viewdata);
		return result;
	}
	
	public void clearCaches()
	{
		processManager.refreshAllConfigs();			
	}

}
