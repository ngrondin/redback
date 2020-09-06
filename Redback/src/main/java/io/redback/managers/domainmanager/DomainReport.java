package io.redback.managers.domainmanager;

import io.firebus.utils.DataMap;

public class DomainReport extends DomainEntry {

	public DomainReport(DataMap c) {
		super(c);
	}

	public DataMap getReportConfig() {
		return config.getObject("report");
	}
}
