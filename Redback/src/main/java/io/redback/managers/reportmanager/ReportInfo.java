package io.redback.managers.reportmanager;

import io.firebus.utils.DataMap;

public class ReportInfo {
	public String name;
	public String description;
	public String domain;
	
	public ReportInfo(String n, String d, String dm) {
		name = n;
		description = d;
		domain = dm;
	}
	
	public ReportInfo(DataMap c) {
		name = c.getString("name");
		description = c.getString("decription");
		domain = c.getString("domain");
	}
	
	public DataMap toDataMap() {
		DataMap map = new DataMap();
		map.put("name", name);
		map.put("description", description);
		if(domain != null)
			map.put("domain", domain);
		return map;
	}
}
