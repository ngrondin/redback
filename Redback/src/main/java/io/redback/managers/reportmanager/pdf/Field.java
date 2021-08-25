package io.redback.managers.reportmanager.pdf;

import java.awt.Color;
import java.io.IOException;
import java.util.Map;

import io.firebus.utils.DataMap;
import io.redback.exceptions.RedbackException;
import io.redback.managers.reportmanager.ReportConfig;
import io.redback.managers.reportmanager.ReportManager;

public class Field extends DataUnit {
	protected String label;
	protected float labelFontSize;
	
	public Field(ReportManager rm, ReportConfig rc, DataMap c) throws RedbackException {
		super(rm, rc, c);
		label = config.containsKey("label") ? config.getString("label") : "";
		labelFontSize = 9f;
		height = 35f;
	}

	public Box produce(Map<String, Object> context) throws IOException, RedbackException {
		String valueStr = getSringValue(context);
		Box rb1 = Box.Text(label, font, labelFontSize);
		rb1.color = Color.GRAY;
		Box rb2 = Box.Text(valueStr, font, fontSize);
		rb2.height += 3;
		if(width > -1)
			rb2.width = width;
		rb2.color = color;
		Box c = Box.VContainer(false);
		c.addChild(rb1);
		c.addChild(rb2);
		c.height += 10;
		c.breakBefore = pagebreak;
		return c;
	}

}
