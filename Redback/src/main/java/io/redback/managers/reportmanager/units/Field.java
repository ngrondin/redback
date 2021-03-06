package io.redback.managers.reportmanager.units;

import java.awt.Color;
import java.io.IOException;
import java.util.Map;

import io.firebus.utils.DataMap;
import io.redback.RedbackException;
import io.redback.managers.reportmanager.ReportBox;
import io.redback.managers.reportmanager.ReportConfig;
import io.redback.managers.reportmanager.ReportDataUnit;
import io.redback.managers.reportmanager.ReportManager;

public class Field extends ReportDataUnit {
	protected String label;
	protected float labelFontSize;
	
	public Field(ReportManager rm, ReportConfig rc, DataMap c) throws RedbackException {
		super(rm, rc, c);
		label = config.containsKey("label") ? config.getString("label") : "";
		labelFontSize = 9f;
		height = 35f;
	}

	public ReportBox produce(Map<String, Object> context) throws IOException, RedbackException {
		String valueStr = getSringValue(context);
		ReportBox rb1 = ReportBox.Text(label, font, labelFontSize);
		rb1.color = Color.GRAY;
		ReportBox rb2 = ReportBox.Text(valueStr, font, fontSize);
		rb2.height += 3;
		if(width > -1)
			rb2.width = width;
		rb2.color = color;
		ReportBox c = ReportBox.VContainer(false);
		c.addChild(rb1);
		c.addChild(rb2);
		c.height += 10;
		return c;
	}

}
