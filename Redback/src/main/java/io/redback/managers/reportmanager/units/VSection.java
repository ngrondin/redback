package io.redback.managers.reportmanager.units;

import java.io.IOException;
import java.util.Map;

import io.firebus.utils.DataMap;
import io.redback.RedbackException;
import io.redback.managers.reportmanager.ReportBox;
import io.redback.managers.reportmanager.ReportConfig;
import io.redback.managers.reportmanager.ReportContainerUnit;
import io.redback.managers.reportmanager.ReportManager;
import io.redback.managers.reportmanager.ReportUnit;

public class VSection extends ReportContainerUnit {
	protected float width;
	
	public VSection(ReportManager rm, ReportConfig rc, DataMap c) throws RedbackException {
		super(rm, rc, c);
		width = c.containsKey("width") ? c.getNumber("width").floatValue() : -1;
	}

	public ReportBox produce(Map<String, Object> context) throws IOException, RedbackException {
		ReportBox c = ReportBox.VContainer(true);
		c.breakBefore = pagebreak;
		for(ReportUnit unit: contentUnits) {
			c.addChild(unit.produce(context));
		}
		return c;
	}

}
