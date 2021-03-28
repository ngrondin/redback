package io.redback.managers.reportmanager.pdf;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import io.firebus.utils.DataMap;
import io.redback.RedbackException;
import io.redback.client.RedbackObjectRemote;
import io.redback.managers.reportmanager.ReportConfig;
import io.redback.managers.reportmanager.ReportManager;

public class VList extends ContainerUnit {
	protected float width;

	public VList(ReportManager rm, ReportConfig rc, DataMap c) throws RedbackException {
		super(rm, rc, c);
		width = c.containsKey("width") ? c.getNumber("width").floatValue() : -1;
	}
	
	public Box produce(Map<String, Object> context) throws IOException, RedbackException {
		List<?> rors = (List<?>)context.get("dataset");
		Box c = Box.VContainer(true);
		for(Object ror: rors) {
			context.put("object", (RedbackObjectRemote)ror);
			for(Unit unit: contentUnits) {
				c.addChild(unit.produce(context));
			}
		}
		return c;
	}
}
