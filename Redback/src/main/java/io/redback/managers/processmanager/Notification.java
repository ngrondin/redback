package io.redback.managers.processmanager;

import java.util.ArrayList;
import java.util.List;

import io.firebus.data.DataList;
import io.firebus.data.DataMap;

public class Notification {
	
	public class Action {
		public String action;
		public String description;
		public String confirm;
		public boolean main;
		
		public Action(String a, String d, String c, boolean m)
		{
			action = a;
			description = d;
			confirm = c;
			main = m;
		}
	}	
	
	public String processName;
	public String pid;
	public String code;
	public String type;
	public String label;
	public String message;
	public String contextLabel;
	public List<Action> actions;
	public DataMap data;
	
	
	public Notification(String pn, String p, String c, String t, String l, String m, String cl, DataMap d) {
		processName = pn;
		pid = p;
		code = c;
		type = t;
		label = l;
		message = m;
		contextLabel = cl;
		actions = new ArrayList<Action>();
		data = d;
	}
	
	public void addAction(String action, String desc, String conf, boolean m)
	{
		Action a = new Action(action, desc, conf, m);
		actions.add(a);
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
		map.put("contextlabel", contextLabel);
		if(actions.size() > 0) {
			DataList actionList = new DataList();
			for(Action action: actions) {
				DataMap actionMap = new DataMap();
				actionMap.put("action", action.action);
				actionMap.put("description", action.description);
				actionMap.put("confirm", action.confirm);
				actionMap.put("main", action.main);
				actionList.add(actionMap);					
			}
			map.put("actions", actionList);
		}
		if(data != null)
			map.put("data", data);		
		return map;
	}

}
