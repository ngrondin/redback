package io.redback.client;

import io.firebus.Firebus;
import io.firebus.data.DataMap;
import io.redback.exceptions.RedbackException;
import io.redback.security.Session;

public class ProcessClient extends Client 
{

	public ProcessClient(Firebus fb, String sn) 
	{
		super(fb, sn);
	}
	
	public void initiate(Session session, String process, String domain, DataMap data) throws RedbackException
	{
		DataMap request = new DataMap();
		request.put("action", "initiate");
		request.put("process", process);
		request.put("domain", domain);
		request.put("data", data);
		requestDataMap(session, request);
	}
	
	public void continueProcess(Session session, String pid) throws RedbackException
	{
		DataMap request = new DataMap();
		request.put("action", "continueprocess");
		request.put("pid", pid);
		requestDataMap(session, request);
	}
	
	public void actionProcess(Session session, String pid, String action) throws RedbackException
	{
		DataMap request = new DataMap();
		request.put("action", "actionprocess");
		request.put("pid", pid);
		request.put("action", action);
		requestDataMap(session, request);
	}
	
	public void interruptProcess(Session session, String pid) throws RedbackException
	{
		DataMap request = new DataMap();
		request.put("action", "interruptprocess");
		request.put("pid", pid);
		requestDataMap(session, request);
	}
	
	public void interruptProcesses(Session session, DataMap filter) throws RedbackException
	{
		DataMap request = new DataMap();
		request.put("action", "interruptprocesses");
		request.put("filter", filter);
		requestDataMap(session, request);
	}
	
	public ProcessAssignmentRemote getAssignment(Session session, DataMap filter) throws RedbackException
	{
		DataMap request = new DataMap();
		request.put("action", "getassignments");
		request.put("filter", filter);
		DataMap response = requestDataMap(session, request);
		if(response != null && response.getList("result").size() > 0) 
		{
			return new ProcessAssignmentRemote(firebus, serviceName, session.getToken(), response.getList("result").getObject(0));
		}
		return null;			
	}


}
