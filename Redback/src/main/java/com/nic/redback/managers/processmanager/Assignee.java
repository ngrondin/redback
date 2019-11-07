package com.nic.redback.managers.processmanager;

import com.nic.firebus.utils.DataMap;

public class Assignee
{
	protected int assigneeType;
	protected String assignee;
	
	public static int USER = 1;
	public static int GROUP = 2;
	public static int PROCESS = 3;
	
	public Assignee(int at, String a)
	{
		assigneeType = at;
		assignee = a;
	}
	
	public Assignee(String username, String pid)
	{
		if(pid == null)
		{
			assigneeType = USER;
			assignee = username;
		}
		else
		{
			assigneeType = PROCESS;
			assignee = pid;
		}
	}
	
	public Assignee(DataMap c)
	{
		String atStr = c.getString("type");
		if(atStr.equals("user"))
			assigneeType = USER;
		else if(atStr.equals("group"))
			assigneeType = GROUP;
		else if(atStr.equals("process"))
			assigneeType = PROCESS;
		assignee = c.getString("id");
	}
	
	public String getId()
	{
		return assignee;
	}
	
	public int getType()
	{
		return assigneeType;
	}
	
	public boolean equals(Assignee other)
	{
		if(assigneeType == other.getType()  &&  assignee.equals(other.getId()))
			return true;
		else
			return false;
	}
	
	public DataMap getJSON()
	{
		DataMap retVal = new DataMap();
		retVal.put("id", assignee);
		if(assigneeType == USER)
			retVal.put("type", "user");
		else if(assigneeType == GROUP)
			retVal.put("type", "group");
		else if(assigneeType == PROCESS)
			retVal.put("type", "process");
		return retVal;
	}
	
}
