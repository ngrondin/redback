package io.redback.managers.reportmanager.pdf;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.firebus.data.DataList;
import io.firebus.data.DataMap;
import io.firebus.script.Expression;
import io.redback.exceptions.RedbackException;
import io.redback.managers.reportmanager.ReportConfig;
import io.redback.managers.reportmanager.ReportManager;

public abstract class ContainerUnit extends Unit {
	protected List<Unit> contentUnits;
	protected boolean canBreak;
	protected Expression showExpr;
	protected Expression colorExpr;
	protected Expression borderColorExpr;
	
	public ContainerUnit(ReportManager rm, ReportConfig rc, DataMap c) throws RedbackException {
		super(rm, rc, c);
		try {
			DataList content = config.getList("content");
			contentUnits = new ArrayList<Unit>();
			for(int i = 0; i < content.size(); i++) {
				Unit unit = Unit.fromConfig(reportManager, reportConfig, content.getObject(i));
				if(unit != null)
					contentUnits.add(unit);
			}
			canBreak = config.containsKey("canbreak") ? config.getBoolean("canbreak") : true;
			jsParams = Arrays.asList(new String[] {"params", "object", "page"});
			showExpr = config.containsKey("show") ? reportManager.getScriptFactory().createExpression(jsFunctionNameRoot + "_container_show", config.getString("show")) : null;
			colorExpr = config.containsKey("color") ? reportManager.getScriptFactory().createExpression(jsFunctionNameRoot + "_container_color", config.getString("color")) : null;
			borderColorExpr = config.containsKey("bordercolor") ? reportManager.getScriptFactory().createExpression(jsFunctionNameRoot + "_container_bordercolor", config.getString("bordercolor")) : null;
		} catch(Exception e) {
			throw new RedbackException("Error intialising container unit", e);
		}
	}
	
	
	protected boolean show(Map<String, Object> context) throws RedbackException {
		if(showExpr != null) {
			Map<String, Object> jsContext = new HashMap<String, Object>();
			jsContext.put("object", DataSet.convertToScript(context.get("object")));
			jsContext.put("page", context.get("page"));
			Object value = null;
			try {
				value = showExpr.eval(jsContext);
			} catch(Exception e) {
			}
			return (Boolean)value;			
		} else {
			return true;
		}
	}
	
	protected Color color(Map<String, Object> context) throws RedbackException {
		if(colorExpr != null) {
			Map<String, Object> jsContext = new HashMap<String, Object>();
			jsContext.put("object", DataSet.convertToScript(context.get("object")));
			jsContext.put("page", context.get("page"));
			Object value = null;
			try {
				value = colorExpr.eval(jsContext);
			} catch(Exception e) {
			}
			return Color.decode(value.toString()) ;		
		} else {
			return null;
		}
	}
	
	protected Color borderColor(Map<String, Object> context) throws RedbackException {
		if(borderColorExpr != null) {
			Map<String, Object> jsContext = new HashMap<String, Object>();
			jsContext.put("object", DataSet.convertToScript(context.get("object")));
			jsContext.put("page", context.get("page"));
			Object value = null;
			try {
				value = borderColorExpr.eval(jsContext);
			} catch(Exception e) {
			}
			return Color.decode(value.toString()) ;		
		} else {
			return null;
		}
	}
}
