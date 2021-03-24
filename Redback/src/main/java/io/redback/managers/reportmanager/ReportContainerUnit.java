package io.redback.managers.reportmanager;

import java.util.ArrayList;
import java.util.List;

import io.firebus.utils.DataList;
import io.firebus.utils.DataMap;
import io.redback.RedbackException;

public abstract class ReportContainerUnit extends ReportUnit {
	protected List<ReportUnit> contentUnits;
	protected boolean canBreak;
	
	public ReportContainerUnit(ReportManager rm, ReportConfig rc, DataMap c) throws RedbackException {
		super(rm, rc, c);
		DataList content = config.getList("content");
		contentUnits = new ArrayList<ReportUnit>();
		for(int i = 0; i < content.size(); i++) {
			contentUnits.add(ReportUnit.fromConfig(reportManager, reportConfig, content.getObject(i)));
		}
		canBreak = config.containsKey("canbreak") ? config.getBoolean("canbreak") : true;
	}

}
