package io.redback.managers.reportmanager.pdf;

import java.util.HashMap;
import java.util.Map;

import io.firebus.data.DataMap;
import io.firebus.script.Expression;
import io.redback.exceptions.RedbackException;
import io.redback.managers.reportmanager.ReportConfig;
import io.redback.managers.reportmanager.ReportManager;

public class Variables extends VSection {
	protected Map<String, Expression> varMap;

	public Variables(ReportManager rm, ReportConfig rc, DataMap c) throws RedbackException {
		super(rm, rc, c);
		try {
			varMap = new HashMap<String, Expression>();
			DataMap vars = config.getObject("vars");
			if(vars != null) {
				for(String key: vars.keySet()) {
					Expression expr = reportManager.getScriptFactory().createExpression(jsFunctionNameRoot + "_variable_" + key, vars.getString(key));
					varMap.put(key, expr);
				}
			}
		} catch(Exception e) {
			throw new RedbackException("Error intialising variables", e);
		}
	}

	@SuppressWarnings("unchecked")
	public Box produce(Map<String, Object> context) throws RedbackException {
		try {
			Map<String, Object> vars = (Map<String, Object>)context.get("vars");
			if(vars == null) {
				vars = new HashMap<String, Object>();
				context.put("vars", vars);
			}
			Map<String, Object> jsContext = getJSContext(context);
			for(String key: varMap.keySet()) {
				Object value = varMap.get(key).eval(jsContext);
				vars.put(key, value);
			}
			Box c = super.produce(context);
			return c; 
		} catch(Exception e) {
			throw new RedbackException("Error producing variables", e);
		}
	}

}
