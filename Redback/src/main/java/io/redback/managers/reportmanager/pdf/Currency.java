package io.redback.managers.reportmanager.pdf;

import java.io.IOException;
import java.util.Map;

import io.firebus.data.DataMap;
import io.redback.exceptions.RedbackException;
import io.redback.managers.reportmanager.ReportConfig;
import io.redback.managers.reportmanager.ReportManager;

public class Currency extends DataUnit {

	public Currency(ReportManager rm, ReportConfig rc, DataMap c) throws RedbackException {
		super(rm, rc, c);
	}

	public Box produce(Map<String, Object> context) throws IOException, RedbackException {
		String valueStr = getSringValue(context);
		//float x = (float)context.get("x");
		//float y = (float)context.get("y");
		float valueWidth = font.getStringWidth(valueStr) / 1000f * fontSize;
		Box rb = Box.Text(valueStr, font, fontSize, width > -1 ? width : valueWidth, height);
		//context.put("x", x + (width == -1 ? valueWidth : width));
		//context.put("y", y + height);
		return rb;
	}

}
