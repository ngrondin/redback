package io.redback.managers.domainmanager;

import io.firebus.utils.DataEntity;
import io.firebus.utils.DataMap;

public class DomainVariable extends DomainEntry {

	public DomainVariable(DataMap c) {
		super(c);
	}
	
	public DataEntity getVariable() {
		return config.get("source");
	}

}
