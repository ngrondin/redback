package io.redback.managers.reportmanager.pdf;

import java.io.IOException;
import java.util.Map;

import io.firebus.utils.DataMap;
import io.redback.RedbackException;
import io.redback.managers.reportmanager.ReportConfig;
import io.redback.managers.reportmanager.ReportManager;

public class Space extends Unit {
	protected float width;
	protected float height;
	
	public Space(ReportManager rm, ReportConfig rc, DataMap c) throws RedbackException {
		super(rm, rc, c);
		width = config.containsKey("width") ? config.getNumber("width").floatValue() : 10;
		height = config.containsKey("height") ? config.getNumber("height").floatValue() : 10;
	}

	public Box produce(Map<String, Object> context) throws IOException, RedbackException {
		return Box.Empty(width, height);
	}

}
