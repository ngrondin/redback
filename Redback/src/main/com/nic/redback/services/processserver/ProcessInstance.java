package com.nic.redback.services.processserver;

import java.util.ArrayList;
import java.util.Date;
import java.util.UUID;

import com.nic.firebus.utils.JSONList;
import com.nic.firebus.utils.JSONObject;

public class ProcessInstance 
{
	protected String processName;
	protected int processVersion;
	protected String domain;
	protected UUID id;
	protected JSONObject data;
	protected String currentNode;
	protected boolean complete;
	protected JSONList assignees;
	protected Assignee lastActioner;
	//protected JSONList receivedNotifications;
	
	protected ProcessInstance(String pn, int v, String dom, JSONObject d)
	{
		processName = pn;
		processVersion = v;
		domain = dom;
		id = UUID.randomUUID();
		data = d;	
		complete = false;
		assignees = new JSONList();
		//receivedNotifications = new JSONList();
	}
	
	protected ProcessInstance(JSONObject c)
	{
		processName = c.getString("process");
		processVersion = c.getNumber("version").intValue();
		domain = c.getString("domain");
		id = UUID.fromString(c.getString("_id"));
		currentNode = c.getString("currentnode");
		complete = c.getBoolean("compelte");
		data = c.getObject("data");
		if(c.containsKey("assignees")  &&  c.get("assignees") instanceof JSONList)
			assignees = c.getList("assignees");
		else
			assignees = new JSONList();
		
		if(c.containsKey("lastactioner")  &&  c.get("lastactioner") instanceof JSONObject)
			lastActioner = new Assignee(c.getObject("lastactioner"));
		
		/*
		if(c.containsKey("receivednotifications")  &&  c.get("receivednotifications") instanceof JSONList)
			receivedNotifications = c.getList("receivednotifications");
		else
			receivedNotifications = new JSONList();
		*/
	}
	
	public String getId()
	{
		return id.toString();
	}
	
	public String getProcessName()
	{
		return processName;
	}
	
	public int getProcessVersion()
	{
		return processVersion;
	}
	
	public String getDomain()
	{
		return domain;
	}
	
	public JSONObject getData()
	{
		return data;
	}
	
	public void setData(JSONObject d)
	{
		data = d;
	}
	
	public void setCurrentNode(String cn)
	{
		if(cn == null)
			complete = true;
		currentNode = cn;
	}
	
	public String getCurrentNode()
	{
		return currentNode;
	}
	
	public void addAssignee(Assignee a)
	{
		assignees.add(a.getJSON());
	}
	
	public void clearAssignees()
	{
		assignees = new JSONList();
	}
	
	public ArrayList<Assignee> getAssignees()
	{
		ArrayList<Assignee> ret = new ArrayList<Assignee>();
		for(int i = 0; i < assignees.size(); i++)
			ret.add(new Assignee(assignees.getObject(i)));
		return ret;
	}
	
	public void setLastActioner(Assignee la)
	{
		lastActioner = la;
	}
	
	/*
	public void addNotification(JSONObject notification)
	{
		for(int i = 0; i < receivedNotifications.size(); i++)
			if(receivedNotifications.getObject(i).getString("pid").equals(notification.getString("pid"))  &&  receivedNotifications.getObject(i).getString("interaction").equals(notification.getString("interaction")))
				receivedNotifications.remove(i--);
		receivedNotifications.add(notification);
	}
	*/
	/*
	public JSONList getReceivedNotifications()
	{
		return receivedNotifications;
	}
	*/
	
	public boolean isComplete()
	{
		return complete;
	}
	
	public JSONObject getJSON()
	{
		JSONObject retVal = new JSONObject();
		retVal.put("_id", id.toString());
		retVal.put("process", processName);
		retVal.put("version", processVersion);
		retVal.put("domain", domain);
		retVal.put("currentnode", currentNode);
		retVal.put("lastupdate", new Date());
		if(assignees.size() > 0)
			retVal.put("assignees", assignees);
		if(lastActioner != null)
			retVal.put("lastactioner", lastActioner.getJSON());
		//if(receivedNotifications.size() > 0)
		//	retVal.put("receivednotifications", receivedNotifications);
		retVal.put("data", data);
		return retVal;
	}
}
