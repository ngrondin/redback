package io.redback.managers.processmanager;

import io.firebus.utils.DataList;
import io.firebus.utils.DataMap;
import io.redback.security.UserProfile;

public class Actionner 
{
	protected String id;
	protected UserProfile userProfile;
	protected ProcessInstance processInstance;
	protected int type;
	public static int USER = 1;
	public static int PROCESS = 3;


	public Actionner(UserProfile up) 
	{
		userProfile = up;
		id = userProfile.getUsername();
		type = USER;
	}
	
	public Actionner(ProcessInstance pi)
	{
		processInstance = pi;
		id = processInstance.getId();
		type = PROCESS;
	}
	
	public Actionner(DataMap c)
	{
		String atStr = c.getString("type");
		if(atStr.equals("user"))
			type = USER;
		else if(atStr.equals("process"))
			type = PROCESS;
		id = c.getString("id");
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
