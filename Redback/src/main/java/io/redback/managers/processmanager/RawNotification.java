package io.redback.managers.processmanager;

import java.util.ArrayList;
import java.util.List;

import io.firebus.data.DataMap;

public class RawNotification {
	public class Action {
		public String action;
		public String description;
		public String confirm;
		public String[] exclusiveToIds;
		public boolean main;
		
		public Action(String a, String d, String c, boolean m, String[] el)
		{
			action = a;
			description = d;
			confirm = c;
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
	public String contextLabel;
	public List<Action> actions;
	public DataMap data;
	
	public RawNotification(String pn, String p, String c, String t, String l, String m, String cl) {
		processName = pn;
		pid = p;
		code = c;
		type = t;
		label = l;
		message = m;
		contextLabel = cl;
		actions = new ArrayList<Action>();
	}

	public void addAction(String action, String desc, String conf, boolean m, String[] el)
	{
		Action a = new Action(action, desc, conf, m, el);
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
		Notification notification = new Notification(processName, pid, code, type, label, message, contextLabel, data);

		if(actions.size() > 0) {
			for(Action action: actions) {
				boolean applies = false;
				if(action.exclusiveToIds == null) {
					applies = true;
				} else if(action.exclusiveToIds.length == 0) {
					applies = true;
				} else {
					for(int i = 0; i < action.exclusiveToIds.length; i++) {
						String exclusiveToId = action.exclusiveToIds[i];
						if(actionner.getId().equals(exclusiveToId) || actionner.isInGroup(exclusiveToId))
							applies = true;
					}
				}
				if(applies) {
					notification.addAction(action.action, action.description, action.confirm, action.main);				
				}
			}
		}
		return notification;
	}
}
