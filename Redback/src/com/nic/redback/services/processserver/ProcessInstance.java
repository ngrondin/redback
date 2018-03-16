package com.nic.redback.services.processserver;

import java.util.ArrayList;
import java.util.UUID;

import com.nic.firebus.utils.JSONList;
import com.nic.firebus.utils.JSONLiteral;
import com.nic.firebus.utils.JSONObject;
import com.nic.redback.security.UserProfile;

public class ProcessInstance 
{
	protected String processName;
	protected int processVersion;
	protected UUID id;
	protected JSONObject data;
	protected String currentNode;
	protected boolean complete;
	protected ArrayList<String> assigneeList;
	protected String assigneeType;
	
	protected ProcessInstance(String pn, int v, JSONObject d)
	{
		processName = pn;
		processVersion = v;
		id = UUID.randomUUID();
		data = d;	
		complete = false;
		assigneeList = new ArrayList<String>();
	}
	
	protected ProcessInstance(JSONObject c)
	{
		processName = c.getString("process");
		processVersion = c.getNumber("version").intValue();
		id = UUID.fromString(c.getString("_id"));
		currentNode = c.getString("currentnode");
		complete = c.getBoolean("compelte");
		data = c.getObject("data");
		assigneeList = new ArrayList<String>();
		if(c.containsKey("assignee")  &&  c.containsKey("assigneetype"))
		{
			assigneeType = c.getString("assigneetype");
			if(c.get("assignee") instanceof JSONList)
			{
				JSONList list = c.getList("assignee");
				for(int i = 0; i < list.size(); i++)
					assigneeList.add(list.getString(i));
			}
			else
			{
				assigneeList.add(c.getString("assignee"));
			}
		}
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
	
	public JSONObject getData()
	{
		return data;
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
	
	public void setAssignee(String t, String a)
	{
		assigneeType = t;
		assigneeList.add(a);
	}
	
	public String getAssigneeType()
	{
		return assigneeType;
	}
	
	public ArrayList<String> getAssignees()
	{
		return assigneeList;
	}
	
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
		retVal.put("currentnode", currentNode);
		if(assigneeType != null  &&  assigneeList != null)
		{
			retVal.put("assgneeType", assigneeType);
			if(assigneeList.size() == 1)
			{
				retVal.put("assignee", assigneeList.get(0));			
			}
			else
			{
				JSONList list = new JSONList();
				for(int i = 0; i < assigneeList.size(); i++)
					list.add(new JSONLiteral(assigneeList.get(i)));
				retVal.put("assignee", list);
			}
		}
		retVal.put("data", data);
		return retVal;
	}
}
