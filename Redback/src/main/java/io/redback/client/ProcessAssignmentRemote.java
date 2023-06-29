package io.redback.client;

import java.util.Date;

import io.firebus.Firebus;
import io.firebus.Payload;
import io.firebus.data.DataMap;

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
	
	public String getInteractionCode()
	{
		return data.getString("code");
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
	
	public boolean hasAction(String a)
	{
		for(int i = 0; i < data.getList("actions").size(); i++) 
			if(data.getList("actions").getObject(i).getString("action").equals(a))
				return true;
		return false;
	}
	
	public void action(String action)
	{
		action(action, null);
	}
	
	public void action(String action, Date date)
	{
		DataMap request = new DataMap();
		request.put("action", "actionprocess");
		request.put("pid", getPid());
		request.put("processaction", action);
		if(date != null) 
			request.put("date", date);
		try
		{
			Payload requestPayload = new Payload(request);
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
			Payload requestPayload = new Payload(request);
			requestPayload.metadata.put("token", token);
			firebus.requestService(processService, requestPayload);
		}
		catch(Exception e) 
		{
			throw new RuntimeException("Error actionning process", e);						
		}	
	}
	
}
