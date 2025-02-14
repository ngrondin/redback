package io.redback.managers.reportmanager.pdf;

import java.awt.Color;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import io.firebus.data.DataMap;
import io.redback.exceptions.RedbackException;
import io.redback.managers.reportmanager.ReportConfig;
import io.redback.managers.reportmanager.ReportManager;
import io.redback.utils.HTMLParser;

public class HTML extends DataUnit {
	protected boolean canBreak;

	public HTML(ReportManager rm, ReportConfig rc, DataMap c) throws RedbackException {
		super(rm, rc, c);
		canBreak = config.containsKey("canbreak") ? config.getBoolean("canbreak") : true;
	}

	public Box produce(Map<String, Object> context) throws IOException, RedbackException {
		String valueStr = getSringValue(context);
		Box c = Box.VContainer(canBreak);
		c.breakBefore = pagebreak;
		float fs = fontSize(context);
		overrideWidth(c, context);
		overrideHeight(c, context);
		if(valueStr.trim().startsWith("<html>")) {
			new HTMLParser(valueStr) {
				public Box curLine = Box.HContainer(false);
				
				public void onTag(String tag) {
					if(tag.equals("li")) {
						newLine();
						add(Box.Text(new String(Character.toChars(0x2022)) + " ", font, fs));
					} else if(tag.equals("br") || tag.equals("p")) {
						newLine();
					}
				}

				public void onText(String text) {
					try {
						String remText = text;
						while(remText.length() > 0) {
							float remWidth = c.width - curLine.width;
							String cutText = cutToWidth(remText, fs, remWidth);
							if(cutText.length() > 0) {
								remText = remText.substring(cutText.length());
								if(remText.startsWith(" ")) remText = remText.substring(1);
								Color c = (Color)getContext("color");
								Boolean isBold = (Boolean)getContext("bold");
								Box tb = Box.Text(cutText, isBold != null && isBold == true ? boldFont : font, fs);
								if(c != null) tb.color = c;
								add(tb);
							} else if(remText.length() > 0) {	
								newLine();
							}
						}
					} catch(Exception e) {
						//add(Box.Text(e.getMessage(), boldFont, fs));
					}
				}
				
				public void onComplete() {
					c.addChild(curLine);
				}
				
				public void add(Box box) {
					curLine.addChild(box);
				}
				
				public void newLine() {
					c.addChild(curLine);
					curLine = Box.HContainer(false);
				}
			}.parse();
		} else {
			valueStr = valueStr.replace("\r", "").replace("\t", "   ");
			String[] lines = valueStr.split("\n");
			for(String line: lines) {
				String remText = line;
				while(remText.length() > 0) {
					String cutText = cutToWidth(remText, fs, c.width);
					if(cutText.length() > 0) {
						remText = remText.substring(cutText.length());
						if(remText.startsWith(" ")) remText = remText.substring(1);
						Box rb = Box.Text(cutText, font, fs);
						rb.color = color;
						c.addChild(rb);
					} else {
						break;
					}
				}
			}			
		}
		return c;
	}

}
