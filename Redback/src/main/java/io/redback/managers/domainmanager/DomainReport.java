package io.redback.managers.domainmanager;

import io.firebus.data.DataMap;

public class DomainReport extends DomainEntry {

	public DomainReport(DataMap c) {
		super(c);
	}

	public DataMap getReportConfig() {
		return config.getObject("source");
	}
}
