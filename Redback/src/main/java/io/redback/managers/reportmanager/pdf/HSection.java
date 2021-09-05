package io.redback.managers.reportmanager.pdf;

import java.io.IOException;
import java.util.Map;

import io.firebus.data.DataMap;
import io.redback.exceptions.RedbackException;
import io.redback.managers.reportmanager.ReportConfig;
import io.redback.managers.reportmanager.ReportManager;

public class HSection extends ContainerUnit {
	protected float height;
	
	public HSection(ReportManager rm, ReportConfig rc, DataMap c) throws RedbackException {
		super(rm, rc, c);
		height = c.containsKey("height") ? c.getNumber("height").floatValue() : -1;
	}

	public Box produce(Map<String, Object> context) throws IOException, RedbackException {
		Box c = Box.HContainer(true);
		c.breakBefore = pagebreak;
		if(show(context)) {
			for(Unit unit: contentUnits) {
				c.addChild(unit.produce(context));
			}
		}
		return c;		
	}

}
