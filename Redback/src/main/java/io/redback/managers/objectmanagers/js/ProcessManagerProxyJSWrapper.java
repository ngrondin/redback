package io.redback.managers.objectmanagers.js;


import io.firebus.Firebus;
import io.firebus.Payload;
import io.firebus.utils.DataMap;
import io.firebus.utils.FirebusDataUtil;
import io.redback.RedbackException;
import io.redback.security.Session;
import jdk.nashorn.api.scripting.JSObject;

public class ProcessManagerProxyJSWrapper
{
	protected Firebus firebus;
	protected String processServiceName;
	protected Session session;
	
	public ProcessManagerProxyJSWrapper(Firebus f, String ps, Session s)
	{
		firebus = f;
		processServiceName = ps;
		session = s;
	}
	
	public void initiate(String process, RedbackObjectJSWrapper object, JSObject jsData) throws RedbackException
	{
		DataMap data = null;
		if(jsData == null)
			data = new DataMap();
		else
			data = FirebusDataUtil.convertJSObjectToDataObject(jsData);
		data.put("objectname", object.getMember("objectname"));
		data.put("uid", object.getMember("uid"));
		DataMap request = new DataMap();
		request.put("action", "initiate");
		request.put("process", process);
		request.put("objectname", object.getMember("objectname"));
		request.put("uid", object.getMember("uid"));
		request.put("domain", object.getMember("domain"));
		request.put("data", data);
		try
		{
			Payload requestPayload = new Payload(request.toString());
			requestPayload.metadata.put("token", session.getToken());
			firebus.requestService(processServiceName, requestPayload);
		}
		catch(Exception e) 
		{
			throw new RedbackException("Error initiating process", e);
		}
	}
	
	public JSObject getAssignment(RedbackObjectJSWrapper object) throws RedbackException
	{
		JSObject assignmentJSO = null;
		DataMap request = new DataMap();
		request.put("action", "getassignments");
		DataMap filter = new DataMap();
		filter.put("data.objectname", object.getMember("objectname"));
		filter.put("data.uid", object.getMember("uid"));
		request.put("filter", filter);
		try
		{
			Payload requestPayload = new Payload(request.toString());
			requestPayload.metadata.put("token", session.getToken());
			Payload resp = firebus.requestService(processServiceName, requestPayload);
			DataMap response = new DataMap(resp.getString());
			if(response != null && response.getList("result").size() > 0) 
			{
				assignmentJSO = FirebusDataUtil.convertDataObjectToJSObject(response.getList("result").getObject(0));	
			}
		}
		catch(Exception e) 
		{
			throw new RedbackException("Error getting process assignment", e);
		}
		return assignmentJSO;
	}
	
	public void action(String pid, String action) throws RedbackException
	{
		DataMap request = new DataMap();
		request.put("action", "processaction");
		request.put("pid", pid);
		request.put("processaction", action);
		try
		{
			Payload requestPayload = new Payload(request.toString());
			requestPayload.metadata.put("token", session.getToken());
			firebus.requestService(processServiceName, requestPayload);
		}
		catch(Exception e) 
		{
			throw new RedbackException("Error actionning process", e);
		}		
	}
	

}
