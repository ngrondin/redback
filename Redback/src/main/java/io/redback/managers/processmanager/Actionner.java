package io.redback.managers.processmanager;

import java.util.ArrayList;
import java.util.List;

import io.firebus.utils.DataMap;
import io.redback.security.UserProfile;

public class Actionner 
{
	protected String id;
	protected UserProfile userProfile;
	protected ProcessInstance processInstance;
	protected int type;
	protected List<String> groups;
	public static int USER = 1;
	public static int PROCESS = 3;


	public Actionner(UserProfile up) 
	{
		userProfile = up;
		id = userProfile.getUsername();
		type = USER;
		groups = new ArrayList<String>();
	}
	
	public Actionner(ProcessInstance pi)
	{
		processInstance = pi;
		id = processInstance.getId();
		type = PROCESS;
		groups = new ArrayList<String>();
	}
	
	public Actionner(DataMap c)
	{
		String atStr = c.getString("type");
		if(atStr.equals("user"))
			type = USER;
		else if(atStr.equals("process"))
			type = PROCESS;
		id = c.getString("id");
		groups = new ArrayList<String>();
	}

	public void addGroup(String group)
	{
		groups.add(group);
	}
	
	public int getType() 
	{
		return type;
	}
	
	public boolean isUser()
	{
		return type == USER;
	}
	
	public boolean isProcess()
	{
		return type == PROCESS;
	}
	
	public boolean isInGroup(String g)
	{
		return groups.contains(g);
	}
	
	public String getId()
	{
		return id;
	}
	
	public  UserProfile getUserProfile()
	{
		return userProfile;
	}
	
	public ProcessInstance getProcessInstance()
	{
		return processInstance;
	}
	
	public List<String> getGroups()
	{
		return groups;
	}
	
	public DataMap getJSON()
	{
		DataMap retVal = new DataMap();
		retVal.put("id", id);
		if(type == USER)
			retVal.put("type", "user");
		else if(type == PROCESS)
			retVal.put("type", "process");
		return retVal;
	}

}
