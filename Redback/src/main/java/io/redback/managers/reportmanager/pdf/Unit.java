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
	protected Expression heightExpr;

	
	public Unit(ReportManager rm, ReportConfig rc, DataMap c) throws RedbackException  {
		config = c;
		reportManager = rm;
		reportConfig = rc;
		jsFunctionNameRoot = "report_" + rc.getName() + "_" + StringUtils.base16(this.hashCode());
		try {
			pagebreak = config.containsKey("pagebreak") ? config.getBoolean("pagebreak") : false;
			widthExpr = config.containsKey("width") ? reportManager.getScriptFactory().createExpression(jsFunctionNameRoot + "_section_width", config.getString("width")) : null;
			heightExpr = config.containsKey("height") ? reportManager.getScriptFactory().createExpression(jsFunctionNameRoot + "_section_height", config.getString("height")) : null;
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
	
	@SuppressWarnings("unchecked")
	protected Map<String, Object> getJSContext(Map<String, Object> context) {
		Map<String, Object> jsContext = new HashMap<String, Object>();
		jsContext.put("oc", new ObjectClientJSWrapper(reportManager.getObjectClient(), (Session)context.get("session")));
		jsContext.put("object", DataSet.convertToScript(context.get("object")));
		jsContext.put("page", context.get("page"));
		jsContext.put("vars", context.get("vars"));
		/*Map<String, Object> vars = (Map<String, Object>)context.get("variables");
		if(vars != null) {
			for(String varName: vars.keySet()) {
				jsContext.put(varName, vars.get(varName));
			}				
		}*/
		return jsContext;
	}
	
	protected float width(Map<String, Object> context) throws RedbackException {
		if(widthExpr != null) {
			try {
				return ((Number)widthExpr.eval(getJSContext(context))).floatValue();
			} catch(Exception e) {
				return -1f;
			}
		} else {
			return -1f;
		}
	}
	
	protected float height(Map<String, Object> context) throws RedbackException {
		if(heightExpr != null) {
			try {
				return ((Number)heightExpr.eval(getJSContext(context))).floatValue();
			} catch(Exception e) {
				return -1f;
			}
		} else {
			return -1f;
		}
	}
	
	protected void overrideHeight(Box box, Map<String, Object> context) throws RedbackException {
		float overrideHeight = height(context);
		if(overrideHeight > -1f) box.height = overrideHeight;
	}

	protected void overrideWidth(Box box, Map<String, Object> context) throws RedbackException {
		float overrideWidth = width(context);
		if(overrideWidth > -1f) box.width = overrideWidth;
	}
	
	public abstract Box produce(Map<String, Object> context) throws IOException, RedbackException;
}
