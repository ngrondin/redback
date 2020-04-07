package io.redback.managers.processmanager;

import java.util.ArrayList;
import java.util.Date;
import java.util.UUID;

import javax.script.Bindings;

import io.firebus.utils.DataList;
import io.firebus.utils.DataMap;

public class ProcessInstance 
{
	protected String processName;
	protected int processVersion;
	protected String domain;
	protected UUID id;
	protected DataMap data;
	protected String currentNode;
	protected boolean complete;
	protected DataList assignees;
	protected Actionner lastActioner;
	//protected JSONList receivedNotifications;
	
	protected ProcessInstance(String pn, int v, String dom, DataMap d)
	{
		processName = pn;
		processVersion = v;
		domain = dom;
		id = UUID.randomUUID();
		data = d;	
		complete = false;
		assignees = new DataList();
		//receivedNotifications = new JSONList();
	}
	
	protected ProcessInstance(DataMap c)
	{
		processName = c.getString("process");
		processVersion = c.getNumber("version").intValue();
		domain = c.getString("domain");
		id = UUID.fromString(c.getString("_id"));
		currentNode = c.getString("currentnode");
		complete = c.getBoolean("compelte");
		data = c.getObject("data");
		if(c.containsKey("assignees")  &&  c.get("assignees") instanceof DataList)
			assignees = c.getList("assignees");
		else
			assignees = new DataList();
		
		if(c.containsKey("lastactioner")  &&  c.get("lastactioner") instanceof DataMap)
			lastActioner = new Actionner(c.getObject("lastactioner"));
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
	
	public DataMap getData()
	{
		return data;
	}
	
	public void setData(DataMap d)
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
		assignees = new DataList();
	}
	
	public ArrayList<Assignee> getAssignees()
	{
		ArrayList<Assignee> ret = new ArrayList<Assignee>();
		for(int i = 0; i < assignees.size(); i++)
			ret.add(new Assignee(assignees.getObject(i)));
		return ret;
	}
	
	public void setLastActioner(Actionner la)
	{
		lastActioner = la;
	}
	
	public boolean isComplete()
	{
		return complete;
	}
	
	public DataMap getJSON()
	{
		DataMap retVal = new DataMap();
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
