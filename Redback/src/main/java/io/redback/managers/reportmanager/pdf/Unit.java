package io.redback.managers.reportmanager.pdf;

import java.awt.Color;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


import io.firebus.data.DataMap;
import io.firebus.script.Expression;
import io.redback.client.js.ObjectClientJSWrapper;
import io.redback.exceptions.RedbackException;
import io.redback.managers.reportmanager.ReportConfig;
import io.redback.managers.reportmanager.ReportManager;
import io.redback.security.Session;
import io.redback.utils.StringUtils;

public abstract class Unit {
	protected DataMap config;
	protected ReportManager reportManager;
	protected ReportConfig reportConfig;
	protected String jsFunctionNameRoot;
	protected List<String> jsParams;
	protected boolean pagebreak; 
	protected Expression widthExpr;
	protected Expression minWidthExpr;
	protected Expression maxWidthExpr;
	protected Expression heightExpr;
	protected Expression minHeightExpr;
	protected Expression maxHeightExpr;
	protected Expression colorExpr;
	
	public Unit(ReportManager rm, ReportConfig rc, DataMap c) throws RedbackException  {
		config = c;
		reportManager = rm;
		reportConfig = rc;
		jsFunctionNameRoot = "report_" + rc.getName() + "_" + StringUtils.base16(this.hashCode());
		try {
			pagebreak = config.containsKey("pagebreak") ? config.getBoolean("pagebreak") : false;
			widthExpr = config.containsKey("width") ? reportManager.getScriptFactory().createExpression(jsFunctionNameRoot + "_unit_width", config.getString("width")) : null;
			minWidthExpr = config.containsKey("minwidth") ? reportManager.getScriptFactory().createExpression(jsFunctionNameRoot + "_unit_minwidth", config.getString("minwidth")) : null;
			maxWidthExpr = config.containsKey("maxwidth") ? reportManager.getScriptFactory().createExpression(jsFunctionNameRoot + "_unit_maxwidth", config.getString("maxwidth")) : null;
			heightExpr = config.containsKey("height") ? reportManager.getScriptFactory().createExpression(jsFunctionNameRoot + "_unit_height", config.getString("height")) : null;
			minHeightExpr = config.containsKey("minheight") ? reportManager.getScriptFactory().createExpression(jsFunctionNameRoot + "_unit_minheight", config.getString("minheight")) : null;
			maxHeightExpr = config.containsKey("maxheight") ? reportManager.getScriptFactory().createExpression(jsFunctionNameRoot + "_unit_maxheight", config.getString("maxheight")) : null;
			colorExpr = config.containsKey("color") ? reportManager.getScriptFactory().createExpression(jsFunctionNameRoot + "_unit_color", config.getString("color")) : null;
		} catch(Exception e) {
			throw new RedbackException("Error intialising unit", e);
		}
	}
	
	protected static Unit fromConfig(ReportManager rm, ReportConfig rc, DataMap c) throws RedbackException {
		Unit newUnit = null;
		String type = c.getString("type"); 
		if(type.equals("dataset"))
			newUnit = new DataSet(rm, rc, c);
		else if(type.equals("transformer"))
			newUnit = new DataSetTransformer(rm, rc, c);
		else if(type.equals("text"))
			newUnit = new Text(rm, rc, c);
		else if(type.equals("multilinetext"))
			newUnit = new MultilineText(rm, rc, c);
		else if(type.equals("html"))
			newUnit = new HTML(rm, rc, c);
		else if(type.equals("field"))
			newUnit = new Field(rm, rc, c);
		else if(type.equals("checkbox"))
			newUnit = new Checkbox(rm, rc, c);
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
		else if(type.equals("vars"))
			newUnit = new Variables(rm, rc, c);
		else if(type.equals("image"))
			newUnit = new Image(rm, rc, c);		
		else if(type.equals("fileset"))
			newUnit = new FileSet(rm, rc, c);		
		else if(type.equals("filelist"))
			newUnit = new FileList(rm, rc, c);				
		return newUnit;
	}

	
	protected Color decodeColor(String c) {
		if(c.startsWith("#")) {
			return new Color(
	            Integer.valueOf( c.substring( 1, 3 ), 16 ),
	            Integer.valueOf( c.substring( 3, 5 ), 16 ),
	            Integer.valueOf( c.substring( 5, 7 ), 16 ) );
		} else {		
			try {
				final java.lang.reflect.Field f = Color.class.getField(c);
				if(f != null)
					return (Color)f.get(null);	
			} catch(Exception e) {}
		}
		return Color.DARK_GRAY;
	}
	
	protected Map<String, Object> getJSContext(Map<String, Object> context) {
		Map<String, Object> jsContext = new HashMap<String, Object>();
		jsContext.put("oc", new ObjectClientJSWrapper(reportManager.getObjectClient(), (Session)context.get("session")));
		jsContext.put("filterobjectname", context.get("filterobjectname"));
		jsContext.put("filter", context.get("filter"));
		jsContext.put("object", DataSet.convertToScript(context.get("object")));
		jsContext.put("page", context.get("page"));
		jsContext.put("vars", context.get("vars"));
		return jsContext;
	}
	
	protected float evalFloat(Expression expr, Map<String, Object> context, float def)  throws RedbackException {
		if(expr != null) {
			try {
				return ((Number)expr.eval(getJSContext(context))).floatValue();
			} catch(Exception e) {
				return def;
			}
		} else {
			return def;
		}
	}
	
	protected void overrideHeight(Box box, Map<String, Object> context) throws RedbackException {
		float height = evalFloat(heightExpr, context, -1f);
		if(height > -1f) {
			box.height = height;
		} else {
			float minHeight = evalFloat(minHeightExpr, context, -1f);
			float maxHeight = evalFloat(maxHeightExpr, context, -1f);
			if(minHeight > -1f && box.height < minHeight) box.height = minHeight;
			if(maxHeight > -1f && box.height > minHeight) box.height = maxHeight;
		}
	}

	protected void overrideWidth(Box box, Map<String, Object> context) throws RedbackException {
		float width = evalFloat(widthExpr, context, -1f);
		if(width > -1f) {
			box.width = width;
		} else {
			float minWidth = evalFloat(minWidthExpr, context, -1f);
			float maxWidth = evalFloat(maxWidthExpr, context, -1f);
			if(minWidth > -1f && box.width < minWidth) box.width = minWidth;
			if(maxWidth > -1f && box.width > maxWidth) box.width = maxWidth;
		}
	}
	
	protected Color color(Map<String, Object> context) throws RedbackException {
		return color(context, null);
	}

	
	protected Color color(Map<String, Object> context, Color def) throws RedbackException {
		if(colorExpr != null) {
			try {
				return Color.decode((String)colorExpr.eval(getJSContext(context)));
			} catch(Exception e) {
				return null;
			}
		} else {
			return def;
		}
	}
	
	public abstract Box produce(Map<String, Object> context) throws IOException, RedbackException;
}
