package io.redback.managers.processmanager;

import java.util.ArrayList;
import java.util.List;

import io.firebus.utils.DataList;
import io.firebus.utils.DataMap;
import io.redback.utils.Notification;

public class Assignment 
{
	public String processName;
	public String pid;
	public Notification notification;
	public List<Action> actions;
	public DataMap data;

	public class Action {
		public String action;
		public String description;
		public boolean main;
		
		public Action(String a, String d, boolean m)
		{
			action = a;
			description = d;
			main = m;
		}
	}
	
	public Assignment()
	{
		actions = new ArrayList<Action>();
	}
	
	public Assignment(String pn, String pi, Notification n)
	{
		processName = pn;
		pid = pi;
		notification = n;
		actions = new ArrayList<Action>();
	}
	
	public void addAction(String action, String desc, boolean m)
	{
		Action a = new Action(action, desc, m);
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
		map.put("code", notification.getCode());
		map.put("type", notification.getType());
		map.put("label", notification.getLabel());
		map.put("message", notification.getMessage());
		DataList actionList = new DataList();
		for(int i = 0; i < actions.size(); i++)
		{
			DataMap action = new DataMap();
			action.put("action", actions.get(i).action);
			action.put("description", actions.get(i).description);
			action.put("main", actions.get(i).main);
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
