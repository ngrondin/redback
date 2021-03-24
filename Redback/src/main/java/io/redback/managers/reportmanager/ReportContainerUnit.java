package io.redback.managers.reportmanager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.firebus.utils.DataList;
import io.firebus.utils.DataMap;
import io.redback.RedbackException;
import io.redback.client.RedbackObjectRemote;
import io.redback.client.js.RedbackObjectRemoteJSWrapper;
import io.redback.managers.jsmanager.Expression;

public abstract class ReportContainerUnit extends ReportUnit {
	protected List<ReportUnit> contentUnits;
	protected boolean canBreak;
	protected Expression showExpr;
	
	public ReportContainerUnit(ReportManager rm, ReportConfig rc, DataMap c) throws RedbackException {
		super(rm, rc, c);
		DataList content = config.getList("content");
		contentUnits = new ArrayList<ReportUnit>();
		for(int i = 0; i < content.size(); i++) {
			contentUnits.add(ReportUnit.fromConfig(reportManager, reportConfig, content.getObject(i)));
		}
		canBreak = config.containsKey("canbreak") ? config.getBoolean("canbreak") : true;
		jsParams = Arrays.asList(new String[] {"params", "object", "page"});
		showExpr = config.containsKey("show") ? new Expression(reportManager.getJSManager(), jsFunctionNameRoot + "_container_show", jsParams, config.getString("show")) : null;
		
	}
	
	
	protected boolean show(Map<String, Object> context) throws RedbackException {
		if(showExpr != null) {
			Map<String, Object> jsContext = new HashMap<String, Object>();
			RedbackObjectRemote object = (RedbackObjectRemote)context.get("object");
			jsContext.put("object", new RedbackObjectRemoteJSWrapper(object));
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
