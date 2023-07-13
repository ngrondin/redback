package io.redback.managers.objectmanager;

import io.firebus.data.DataMap;
import io.redback.exceptions.RedbackException;
import io.redback.managers.jsmanager.ExpressionMap;
import io.redback.security.Session;

public class QueryConfig {
	protected ObjectManager objectManager;
	protected String objectName;
	protected ExpressionMap filterExpressionMap;
	
	public QueryConfig(ObjectManager om, String n, DataMap cfg) throws RedbackException {
		objectManager = om;
		objectName = cfg.getString("object");
		filterExpressionMap = new ExpressionMap(objectManager.getScriptFactory(), n, cfg.getObject("filter"));
	}
	
	public String getObjectName() {
		return objectName;
	}
	
	public DataMap getFilter(Session session) throws RedbackException {
		return filterExpressionMap.eval(session.getScriptContext());
	}
}
