package com.nic.redback.services.impl;

import java.util.List;

import com.nic.firebus.Firebus;
import com.nic.firebus.utils.DataList;
import com.nic.firebus.utils.DataMap;
import com.nic.redback.RedbackException;
import com.nic.redback.managers.processmanager.ProcessManager;
import com.nic.redback.security.Session;
import com.nic.redback.services.ProcessServer;

public class RedbackProcessServer extends ProcessServer 
{
	protected ProcessManager processManager;


	public RedbackProcessServer(DataMap c, Firebus f) 
	{
		super(c, f);
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
	
	protected DataList getAssignments(Session session, String extpid, DataMap filter, DataList viewdata) throws RedbackException
	{
		List<DataMap> result = processManager.getAssignments(session, extpid, filter, viewdata);
		DataList responseList = new DataList();
		for(int i = 0; i < result.size(); i++)
			responseList.add(result.get(i));
		return responseList;
	}

	protected void refreshConfigs() 
	{
		processManager.refreshAllConfigs();			
	}
}
