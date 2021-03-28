package io.redback.managers.reportmanager.pdf;

import java.awt.Color;
import java.io.IOException;
import java.util.List;
import java.util.Map;


import io.firebus.utils.DataMap;
import io.redback.RedbackException;
import io.redback.managers.reportmanager.ReportConfig;
import io.redback.managers.reportmanager.ReportManager;
import io.redback.utils.StringUtils;

public abstract class Unit {
	protected DataMap config;
	protected ReportManager reportManager;
	protected ReportConfig reportConfig;
	protected String jsFunctionNameRoot;
	protected List<String> jsParams;
	protected boolean pagebreak; 
	
	public Unit(ReportManager rm, ReportConfig rc, DataMap c) throws RedbackException  {
		config = c;
		reportManager = rm;
		reportConfig = rc;
		jsFunctionNameRoot = "report_" + rc.getName() + "_" + StringUtils.base16(this.hashCode());
		pagebreak = config.containsKey("pagebreak") ? config.getBoolean("pagebreak") : false;
	}
	
	protected static Unit fromConfig(ReportManager rm, ReportConfig rc, DataMap c) throws RedbackException {
		Unit newUnit = null;
		String type = c.getString("type"); 
		if(type.equals("dataset"))
			newUnit = new DataSet(rm, rc, c);
		else if(type.equals("text"))
			newUnit = new Text(rm, rc, c);
		else if(type.equals("multilinetext"))
			newUnit = new MultilineText(rm, rc, c);
		else if(type.equals("field"))
			newUnit = new Field(rm, rc, c);
		else if(type.equals("vlist"))
			newUnit = new VList(rm, rc, c);
		else if(type.equals("vsection"))
			newUnit = new VSection(rm, rc, c);
		else if(type.equals("hsection"))
			newUnit = new HSection(rm, rc, c);
		else if(type.equals("dynamicform"))
			newUnit = new DynamicForm(rm, rc, c);
		else if(type.equals("space"))
			newUnit = new Space(rm, rc, c);
		else if(type.equals("hline"))
			newUnit = new HLine(rm, rc, c);
		else if(type.equals("image"))
			newUnit = new Image(rm, rc, c);		
		return newUnit;
	}
	/*
	protected float getStringWidth(String text, PDFont pdfFont, float fontSize) throws IOException {
		return pdfFont.getStringWidth(text) / 1000f * fontSize;
	}
	*/
	
	protected Color getColor(String c) {
		try {
			final java.lang.reflect.Field f = Color.class.getField(c);
			if(f != null)
				return (Color)f.get(null);
			else
				return Color.DARK_GRAY;
		} catch(Exception e) {
			return Color.DARK_GRAY;
		}
	}
	

	
	public abstract Box produce(Map<String, Object> context) throws IOException, RedbackException;
}
