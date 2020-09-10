package io.redback.utils;

import io.firebus.utils.DataMap;

public class Notification {
	
	public String code;
	public String type;
	public String label;
	public String message;
	
	public Notification(String c, String t, String l, String m) {
		code = c;
		type = t;
		label = l;
		message = m;
	}
	
	public Notification(DataMap c) {
		code = c.getString("code");
		type = c.getString("type");
		label = c.getString("label");
		message = c.getString("message");
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
	
	public DataMap getDataMap() {
		DataMap map = new DataMap();
		map.put("code", code);
		map.put("type", type);
		map.put("label", label);
		map.put("message", message);
		return map;
	}

}
