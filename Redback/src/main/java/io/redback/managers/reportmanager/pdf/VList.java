package io.redback.managers.reportmanager.pdf;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import io.firebus.data.DataMap;
import io.redback.exceptions.RedbackException;
import io.redback.managers.reportmanager.ReportConfig;
import io.redback.managers.reportmanager.ReportManager;

public class VList extends ContainerUnit {
	protected float width;

	public VList(ReportManager rm, ReportConfig rc, DataMap c) throws RedbackException {
		super(rm, rc, c);
		width = c.containsKey("width") ? c.getNumber("width").floatValue() : -1;
	}
	
	public Box produce(Map<String, Object> context) throws IOException, RedbackException {
		List<?> dataset = (List<?>)context.get("dataset");
		Box c = Box.VContainer(true);
		c.breakBefore = pagebreak;
		for(Object object: dataset) {
			context.put("object", object);
			for(Unit unit: contentUnits) {
				c.addChild(unit.produce(context));
			}
		}
		return c;
	}
}
