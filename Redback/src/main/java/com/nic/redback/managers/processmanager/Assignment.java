package com.nic.redback.managers.processmanager;

import java.util.ArrayList;
import java.util.List;

import com.nic.firebus.utils.DataList;
import com.nic.firebus.utils.DataMap;

public class Assignment 
{
	public String processName;
	public String pid;
	public String interaction;
	public String message;
	public List<Action> actions;
	public DataMap data;

	public class Action {
		public String action;
		public String description;
		
		public Action(String a, String d)
		{
			action = a;
			description = d;
		}
	}
	
	public Assignment()
	{
		actions = new ArrayList<Action>();
	}
	
	public Assignment(String pn, String pi, String i, String m)
	{
		processName = pn;
		pid = pi;
		interaction = i;
		message = m;
		actions = new ArrayList<Action>();
	}
	
	public void addAction(String action, String desc)
	{
		Action a = new Action(action, desc);
		actions.add(a);
	}
	
	public void addData(String key, Object val)
	{
		if(data == null)
			data = new DataMap();
		data.put(key, val);
	}
	
	public DataMap getDataMap()
	{
		DataMap map = new DataMap();
		map.put("process", processName);
		map.put("pid", pid);
		map.put("interaction", interaction);
		map.put("message", message);
		DataList actionList = new DataList();
		for(int i = 0; i < actions.size(); i++)
		{
			DataMap action = new DataMap();
			action.put("action", actions.get(i).action);
			action.put("description", actions.get(i).description);
			actionList.add(action);
		}
		map.put("actions", actionList);		
		if(data != null)
			map.put("data", data);
		return map;
	}
	
	public String toString()
	{
		return getDataMap().toString();
	}
}
