package io.redback.managers.reportmanager.pdf;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import io.firebus.data.DataMap;
import io.redback.exceptions.RedbackException;
import io.redback.managers.reportmanager.ReportConfig;
import io.redback.managers.reportmanager.ReportManager;

public class MultilineText extends DataUnit {
	protected boolean canBreak;

	public MultilineText(ReportManager rm, ReportConfig rc, DataMap c) throws RedbackException {
		super(rm, rc, c);
		canBreak = config.containsKey("canbreak") ? config.getBoolean("canbreak") : true;
	}

	public Box produce(Map<String, Object> context) throws IOException, RedbackException {
		String valueStr = getSringValue(context);
		valueStr = valueStr.replace("\r", "").replace("\t", "   ");
		String[] lines = valueStr.split("\n");
		Box c = Box.VContainer(canBreak);
		c.breakBefore = pagebreak;
		float fs = fontSize(context);
		float widthOverride = width(context);
		if(widthOverride > -1) 
			c.width = widthOverride;	
		for(int i = 0; i < lines.length; i++) {
			if(widthOverride > -1) {
				List<String> sublines = this.cutToLines(fs, lines[i], widthOverride);
				for(String subline : sublines) {
					Box rb = Box.Text(subline, font, fs);
					//rb.height = height;
					rb.color = color;
					c.addChild(rb);							
				}
			} else {
				Box rb = Box.Text(lines[i], font, fs);
				//rb.height = height;
				rb.color = color;
				c.addChild(rb);					
			}
		}
		return c;
	}

}
