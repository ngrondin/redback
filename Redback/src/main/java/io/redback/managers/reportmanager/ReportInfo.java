package io.redback.managers.reportmanager;

import io.firebus.data.DataMap;

public class ReportInfo {
	public String name;
	public String description;
	public String domain;
	public String type;
	
	public ReportInfo(String n, String d, String t, String dm) {
		name = n;
		description = d;
		type = t;
		domain = dm;
	}
	
	public ReportInfo(DataMap c) {
		name = c.getString("name");
		description = c.getString("decription");
		type = c.getString("type");
		domain = c.getString("domain");
	}
	
	public DataMap toDataMap() {
		DataMap map = new DataMap();
		map.put("name", name);
		map.put("description", description);
		if(type != null)
			map.put("type", type);
		if(domain != null)
			map.put("domain", domain);
		return map;
	}
}
