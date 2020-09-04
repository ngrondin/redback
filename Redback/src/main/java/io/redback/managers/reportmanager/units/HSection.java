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

public class HSection extends ReportContainerUnit {
	protected float height;
	
	public HSection(ReportManager rm, ReportConfig rc, DataMap c) throws RedbackException {
		super(rm, rc, c);
		height = c.containsKey("height") ? c.getNumber("height").floatValue() : -1;
	}

	public ReportBox produce(Map<String, Object> context) throws IOException, RedbackException {
		ReportBox c = ReportBox.HContainer(true);
		for(ReportUnit unit: contentUnits) {
			c.addChild(unit.produce(context));
		}
		return c;		
	}

}
