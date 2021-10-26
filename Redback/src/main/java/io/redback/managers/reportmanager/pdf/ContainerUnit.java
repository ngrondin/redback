package io.redback.managers.reportmanager.pdf;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.firebus.data.DataList;
import io.firebus.data.DataMap;
import io.firebus.script.Expression;
import io.redback.client.RedbackObjectRemote;
import io.redback.client.js.RedbackObjectRemoteJSWrapper;
import io.redback.exceptions.RedbackException;
import io.redback.managers.reportmanager.ReportConfig;
import io.redback.managers.reportmanager.ReportManager;

public abstract class ContainerUnit extends Unit {
	protected List<Unit> contentUnits;
	protected boolean canBreak;
	protected Expression showExpr;
	
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
}
