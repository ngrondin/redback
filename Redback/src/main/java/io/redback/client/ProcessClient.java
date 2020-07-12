package io.redback.client;

import io.firebus.Firebus;
import io.firebus.Payload;
import io.firebus.utils.DataMap;
import io.redback.RedbackException;
import io.redback.security.Session;

public class ProcessClient extends Client 
{

	public ProcessClient(Firebus fb, String sn) 
	{
		super(fb, sn);
	}
	
	public void initiate(Session session, String process, String objectName, String uid, String domain, DataMap data) throws RedbackException
	{
		data.put("objectname", objectName);
		data.put("uid", uid);
		DataMap request = new DataMap();
		request.put("action", "initiate");
		request.put("process", process);
		request.put("objectname", objectName);
		request.put("uid", uid);
		request.put("domain", domain);
		request.put("data", data);
		try
		{
			Payload requestPayload = new Payload(request.toString());
			requestPayload.metadata.put("token", session.getToken());
			firebus.requestService(serviceName, requestPayload);
		}
		catch(Exception e) 
		{
			throw new RedbackException("Error initiating process", e);						
		}
	}
	
	public ProcessAssignmentRemote getAssignment(Session session, String objectName, String uid)
	{
		DataMap request = new DataMap();
		request.put("action", "getassignments");
		DataMap filter = new DataMap();
		filter.put("data.objectname", objectName);
		filter.put("data.uid", uid);
		request.put("filter", filter);
		try
		{
			Payload requestPayload = new Payload(request.toString());
			requestPayload.metadata.put("token", session.getToken());
			Payload resp = firebus.requestService(serviceName, requestPayload);
			DataMap response = new DataMap(resp.getString());
			if(response != null && response.getList("result").size() > 0) 
			{
				return new ProcessAssignmentRemote(firebus, serviceName, session.getToken(), response.getList("result").getObject(0));
			}
		}
		catch(Exception e) 
		{
			throw new RuntimeException("Error getting process assignment", e);						
		}
		return null;			
	}


}
