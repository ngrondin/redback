package io.redback.managers.reportmanager.pdf;

import java.io.IOException;
import java.util.Map;

import io.firebus.data.DataMap;
import io.redback.exceptions.RedbackException;
import io.redback.managers.reportmanager.ReportConfig;
import io.redback.managers.reportmanager.ReportManager;

public class Space extends Unit {
	protected float width;
	protected float height;
	
	public Space(ReportManager rm, ReportConfig rc, DataMap c) throws RedbackException {
		super(rm, rc, c);

	}

	public Box produce(Map<String, Object> context) throws IOException, RedbackException {
		Box c = Box.Empty(10, 10);
		c.breakBefore = pagebreak;
		overrideWidth(c, context);
		overrideHeight(c, context);
		return c;
	}

}
