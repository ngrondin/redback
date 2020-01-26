package io.redback.services.impl;

import java.util.List;

import io.firebus.Firebus;
import io.firebus.utils.DataList;
import io.firebus.utils.DataMap;
import io.redback.RedbackException;
import io.redback.managers.processmanager.Assignment;
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
