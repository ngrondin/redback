package io.redback.managers.reportmanager.units;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import io.firebus.utils.DataMap;
import io.redback.RedbackException;
import io.redback.managers.reportmanager.ReportBox;
import io.redback.managers.reportmanager.ReportConfig;
import io.redback.managers.reportmanager.ReportDataUnit;
import io.redback.managers.reportmanager.ReportManager;

public class MultilineText extends ReportDataUnit {
	protected boolean canBreak;

	public MultilineText(ReportManager rm, ReportConfig rc, DataMap c) throws RedbackException {
		super(rm, rc, c);
		canBreak = config.containsKey("canbreak") ? config.getBoolean("canbreak") : true;
	}

	public ReportBox produce(Map<String, Object> context) throws IOException, RedbackException {
		String valueStr = getSringValue(context);
		valueStr = valueStr.replace("\r", "");
		String[] lines = valueStr.split("\n");
		ReportBox c = ReportBox.VContainer(canBreak);
		if(width > -1) 
			c.width = width;	
		for(int i = 0; i < lines.length; i++) {
			if(width > -1) {
				List<String> sublines = this.cutToLines(lines[i], width);
				for(String subline : sublines) {
					ReportBox rb = ReportBox.Text(subline, font, fontSize);
					//rb.height = height;
					rb.color = color;
					c.addChild(rb);							
				}
			} else {
				ReportBox rb = ReportBox.Text(lines[i], font, fontSize);
				//rb.height = height;
				rb.color = color;
				c.addChild(rb);					
			}
		}
		return c;
	}

}
