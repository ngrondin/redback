package io.redback.utils;

import java.util.ArrayList;
import java.util.List;

import io.firebus.utils.DataList;
import io.firebus.utils.DataMap;

public class Notification {
	
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
	
	public String processName;
	public String pid;
	public String code;
	public String type;
	public String label;
	public String message;
	public List<Action> actions;
	public List<String> to;
	public DataMap data;
	
	
	public Notification(String pn, String p, String c, String t, String l, String m) {
		processName = pn;
		pid = p;
		code = c;
		type = t;
		label = l;
		message = m;
		actions = new ArrayList<Action>();
		to = new ArrayList<String>();
	}
	
	public Notification(DataMap c) {
		processName = c.getString("process");
		pid = c.getString("pid");
		code = c.getString("code");
		type = c.getString("type");
		label = c.getString("label");
		message = c.getString("message");
		actions = new ArrayList<Action>();
		to = new ArrayList<String>();
		if(c.containsKey("actions")) {
			DataList actionList = c.getList("actions");
			for(int i = 0; i < actionList.size(); i++) {
				DataMap action = actionList.getObject(i);
				actions.add(new Action(action.getString("action"), action.getString("description"), action.getBoolean("main")));
			}
		}
		if(c.containsKey("to")) {
			DataList toList = c.getList("to");
			for(int i = 0; i < toList.size(); i++) {
				to.add(toList.getString(i));
			}
		}
		if(c.containsKey("data")) {
			data = c.getObject("data");
		}
	}
	
	public void addAction(String action, String desc, boolean m)
	{
		Action a = new Action(action, desc, m);
		actions.add(a);
	}
	
	public void addTo(String username) {
		to.add(username);
	}
	
	public void addData(String key, Object val)
	{
		if(data == null)
			data = new DataMap();
		data.put(key, val);
	}
	
	public String getProcessName() {
		return processName;
	}
	
	public String getPid() {
		return pid;
	}
	
	public String getCode() {
		return code;
	}
	
	public String getType() {
		return type;
	}
	
	public String getLabel() {
		return label;
	}
	
	public String getMessage() {
		return message;
	}
	
	public List<Action> getActions() {
		return actions;
	}
	
	public Object getData(String key) {
		if(data != null) {
			return data.get(key);
		} else {
			return null;
		}
	}
	
	public DataMap getDataMap() {
		DataMap map = new DataMap();
		map.put("process", processName);
		map.put("pid", pid);
		map.put("code", code);
		map.put("type", type);
		map.put("label", label);
		map.put("message", message);
		if(actions.size() > 0) {
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
		}
		if(to.size() > 0) {
			DataList toList = new DataList();
			for(int i = 0; i < to.size(); i++)
				toList.add(to.get(i));
			map.put("to", toList);
		}
		if(data != null)
			map.put("data", data);		
		return map;
	}

}
