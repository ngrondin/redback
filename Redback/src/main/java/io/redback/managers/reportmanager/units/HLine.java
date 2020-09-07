package io.redback.managers.reportmanager.units;

import java.io.IOException;
import java.util.Map;

import io.firebus.utils.DataMap;
import io.redback.RedbackException;
import io.redback.managers.reportmanager.ReportBox;
import io.redback.managers.reportmanager.ReportConfig;
import io.redback.managers.reportmanager.ReportManager;
import io.redback.managers.reportmanager.ReportUnit;

public class HLine extends ReportUnit {
	protected float width;
	
	public HLine(ReportManager rm, ReportConfig rc, DataMap c) throws RedbackException {
		super(rm, rc, c);
		width = config.containsKey("width") ? config.getNumber("width").floatValue() : 0;
	}

	public ReportBox produce(Map<String, Object> context) throws IOException, RedbackException {
		return ReportBox.HLine(width, 10);
	}

}
