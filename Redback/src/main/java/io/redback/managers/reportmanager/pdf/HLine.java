package io.redback.managers.reportmanager.pdf;

import java.awt.Color;
import java.io.IOException;
import java.util.Map;

import io.firebus.utils.DataMap;
import io.redback.exceptions.RedbackException;
import io.redback.managers.reportmanager.ReportConfig;
import io.redback.managers.reportmanager.ReportManager;

public class HLine extends Unit {
	protected float width;
	protected Color color;
	
	public HLine(ReportManager rm, ReportConfig rc, DataMap c) throws RedbackException {
		super(rm, rc, c);
		width = config.containsKey("width") ? config.getNumber("width").floatValue() : 0;
		color = config.containsKey("color") ? getColor(config.getString("color")) : Color.DARK_GRAY;
	}

	public Box produce(Map<String, Object> context) throws IOException, RedbackException {
		Box rb = Box.HLine(width, 10);
		rb.color = color;
		return rb;
	}

}
