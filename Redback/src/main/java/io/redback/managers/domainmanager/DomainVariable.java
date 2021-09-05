package io.redback.managers.domainmanager;

import io.firebus.data.DataEntity;
import io.firebus.data.DataMap;

public class DomainVariable extends DomainEntry {

	public DomainVariable(DataMap c) {
		super(c);
	}
	
	public DataEntity getVariable() {
		return config.get("source");
	}

}
