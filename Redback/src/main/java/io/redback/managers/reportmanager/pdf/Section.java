package io.redback.managers.reportmanager.pdf;

import io.firebus.data.DataMap;
import io.redback.exceptions.RedbackException;
import io.redback.managers.reportmanager.ReportConfig;
import io.redback.managers.reportmanager.ReportManager;

public abstract class Section extends ContainerUnit {
	
	public Section(ReportManager rm, ReportConfig rc, DataMap c) throws RedbackException {
		super(rm, rc, c);

	}
	

	

}
