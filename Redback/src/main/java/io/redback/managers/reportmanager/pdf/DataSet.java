package io.redback.managers.reportmanager.pdf;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.firebus.data.DataEntity;
import io.firebus.data.DataLiteral;
import io.firebus.data.DataMap;
import io.firebus.script.Expression;
import io.redback.client.ObjectClient;
import io.redback.client.RedbackObjectRemote;
import io.redback.client.js.RedbackObjectRemoteJSWrapper;
import io.redback.exceptions.RedbackException;
import io.redback.managers.jsmanager.ExpressionMap;
import io.redback.managers.reportmanager.ReportConfig;
import io.redback.managers.reportmanager.ReportManager;
import io.redback.security.Session;


public class DataSet extends ContainerUnit {
	protected String object;
	protected Expression filterExp;
	protected ExpressionMap filterExpMap;
	protected Expression sortExp;
	protected ExpressionMap sortExpMap;
	
	public DataSet(ReportManager rm, ReportConfig rc, DataMap c) throws RedbackException {
		super(rm, rc, c);
		try {
			jsParams = Arrays.asList(new String[] {"filter", "master"});
			object = c.getString("object");
			if(c.containsKey("filter")) {
				DataEntity filter = c.get("filter");
				if(filter instanceof DataMap)
					filterExpMap = new ExpressionMap(reportManager.getScriptFactory(), jsFunctionNameRoot + "_filter", ((DataMap)filter));
				else if(filter instanceof DataLiteral)
					filterExp = reportManager.getScriptFactory().createExpression(jsFunctionNameRoot + "_filter", ((DataLiteral)filter).getString());	
			}
			if(c.containsKey("sort")) {
				DataEntity sort = c.get("sort");
				if(sort instanceof DataMap)
					sortExpMap = new ExpressionMap(reportManager.getScriptFactory(), jsFunctionNameRoot + "_filter", ((DataMap)sort));
				else if(sort instanceof DataLiteral)
					sortExp = reportManager.getScriptFactory().createExpression(jsFunctionNameRoot + "_sort", ((DataLiteral)sort).getString());	
			}
		} catch(Exception e) {
			throw new RedbackException("Error intialising container unit", e);
		}
	}

	public Box produce(Map<String, Object> context) throws RedbackException {
		try {
			Object currentObject = context.get("object");
			Object currentMasterObject = context.get("master");
			List<?> currentMasterDataset = (List<?>)context.get("dataset");
			Map<String, Object> jsContext = new HashMap<String, Object>();
			jsContext.put("master", new RedbackObjectRemoteJSWrapper((RedbackObjectRemote)currentObject));
			jsContext.put("filter", context.get("filter"));
			ObjectClient oc = reportManager.getObjectClient();
			DataMap filter = (filterExp != null ? (DataMap)filterExp.eval(jsContext) : filterExpMap.eval(jsContext));
			DataMap sort = (sortExp != null ? (DataMap)sortExp.eval(jsContext) : sortExpMap != null ? sortExpMap.eval(jsContext) : null);
			Session session = (Session)context.get("session");
			List<RedbackObjectRemote> rors = oc.listAllObjects(session, object, filter, sort, true);
			context.put("master", currentObject);
			context.put("object", null);
			context.put("dataset", rors);
	
			Box c = Box.VContainer(true);
			c.breakBefore = pagebreak;
			for(Unit unit: contentUnits) {
				c.addChild(unit.produce(context));
			}
			context.put("master", currentMasterObject);
			context.put("object", currentObject);
			context.put("dataset", currentMasterDataset);
			return c;
		} catch(Exception e) {
			throw new RedbackException("Error producing dataset unit", e);
		}
	}

}
