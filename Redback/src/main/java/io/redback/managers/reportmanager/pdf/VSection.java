package io.redback.managers.reportmanager.pdf;

import java.io.IOException;
import java.util.Map;

import io.firebus.data.DataMap;
import io.redback.exceptions.RedbackException;
import io.redback.managers.reportmanager.ReportConfig;
import io.redback.managers.reportmanager.ReportManager;

public class VSection extends Section {
	
	public VSection(ReportManager rm, ReportConfig rc, DataMap c) throws RedbackException {
		super(rm, rc, c);
	}

	public Box produce(Map<String, Object> context) throws IOException, RedbackException {
		Box c = Box.VContainer(true);
		c.breakBefore = pagebreak;
		c.color = color(context);
		c.borderColor = borderColor(context);
		if(show(context)) {
			for(Unit unit: contentUnits) {
				c.addChild(unit.produce(context));
			}
		}
		overrideHeight(c, context);
		overrideWidth(c, context);
		return c;
	}

}
