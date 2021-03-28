package io.redback.managers.reportmanager;


import io.firebus.utils.DataMap;
import io.redback.RedbackException;


public class ReportConfig {
	protected ReportManager reportManager;
	protected DataMap config;
	protected String name;
	protected String type;
	protected String description;
	protected String domain;
	protected String category;

	
	public ReportConfig(ReportManager rm, DataMap c) throws RedbackException {
		config = c;
		reportManager = rm;
		name = config.getString("name");
		type = config.containsKey("type") ? config.getString("type") : "pdf";
		description = config.getString("description");
		domain = config.getString("domain");
		category = config.getString("category");

	}
	
	public String getName() {
		return name;
	}
	
	public String getType() {
		return type;
	}
	
	public String getDescription() {
		return description;
	}
	
	public String getDomain() {
		return domain;
	}
	
	public String getCategory() {
		return category;
	}
	
	public DataMap getData() {
		return config;
	}
	
}
