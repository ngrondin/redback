package io.redback.managers.processmanager;

import java.util.ArrayList;
import java.util.List;

import io.firebus.utils.DataMap;

public class RawNotification {
	public class Action {
		public String action;
		public String description;
		public String[] exclusiveToIds;
		public boolean main;
		
		public Action(String a, String d, boolean m, String[] el)
		{
			action = a;
			description = d;
			main = m;
			exclusiveToIds = el;
		}
	}	
	
	public String processName;
	public String pid;
	public String code;
	public String type;
	public String label;
	public String message;
	public List<Action> actions;
	public DataMap data;
	
	public RawNotification(String pn, String p, String c, String t, String l, String m) {
		processName = pn;
		pid = p;
		code = c;
		type = t;
		label = l;
		message = m;
		actions = new ArrayList<Action>();
	}

	public void addAction(String action, String desc, boolean m, String[] el)
	{
		Action a = new Action(action, desc, m, el);
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
	
	public Notification getNotificationForActionner(Actionner actionner) {
		Notification notification = new Notification(processName, pid, code, type, label, message, data);

		if(actions.size() > 0) {
			for(Action action: actions) {
				boolean applies = false;
				if(action.exclusiveToIds == null) {
					applies = true;
				} else if(action.exclusiveToIds.length == 0) {
					applies = true;
				} else {
					for(int i = 0; i < action.exclusiveToIds.length; i++)
						if(action.exclusiveToIds[i].equals(actionner.getId()))
							applies = true;
				}
				if(applies) {
					notification.addAction(action.action, action.description, action.main);				
				}
			}
		}
		return notification;
	}
}
