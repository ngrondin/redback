package io.redback.client;

import io.firebus.Firebus;
import io.firebus.Payload;
import io.firebus.utils.DataMap;

public class ProcessAssignmentRemote {
	public DataMap data;
	protected Firebus firebus;
	protected String processService;
	protected String token;
	
	public ProcessAssignmentRemote(Firebus fb, String ps, String t, DataMap d) {
		firebus = fb;
		processService = ps;
		token = t;
		data = d;
	}
	
	public String getPid()
	{
		return data.getString("pid");
	}
	
	public String getMessage()
	{
		return null;
	}
	
	public int getActionCount()
	{
		return data.getList("actions").size();
	}
	
	public String getAction(int i)
	{
		return data.getList("actions").getObject(i).getString("action");
	}
	
	public String getActionDescription(int i)
	{
		return data.getList("actions").getObject(i).getString("description");
	}
	
	public void action(String action)
	{
		DataMap request = new DataMap();
		request.put("action", "actionprocess");
		request.put("pid", getPid());
		request.put("processaction", action);
		try
		{
			Payload requestPayload = new Payload(request.toString());
			requestPayload.metadata.put("token", token);
			firebus.requestService(processService, requestPayload);
		}
		catch(Exception e) 
		{
			throw new RuntimeException("Error actionning process", e);						
		}	
	}
	
	public void interrupt()
	{
		DataMap request = new DataMap();
		request.put("action", "interruptprocess");
		request.put("pid", getPid());
		try
		{
			Payload requestPayload = new Payload(request.toString());
			requestPayload.metadata.put("token", token);
			firebus.requestService(processService, requestPayload);
		}
		catch(Exception e) 
		{
			throw new RuntimeException("Error actionning process", e);						
		}	
	}
	
}
