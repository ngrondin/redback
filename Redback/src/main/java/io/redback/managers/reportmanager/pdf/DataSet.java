package io.redback.managers.reportmanager.pdf;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.firebus.data.DataEntity;
import io.firebus.data.DataLiteral;
import io.firebus.data.DataMap;
import io.redback.client.ObjectClient;
import io.redback.client.RedbackObjectRemote;
import io.redback.exceptions.RedbackException;
import io.redback.managers.jsmanager.Expression;
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
		jsParams = Arrays.asList(new String[] {"filter", "master"});
		object = c.getString("object");
		if(c.containsKey("filter")) {
			DataEntity filter = c.get("filter");
			if(filter instanceof DataMap)
				filterExpMap = new ExpressionMap(reportManager.getJSManager(), jsFunctionNameRoot + "_filter", jsParams, ((DataMap)filter));
			else if(filter instanceof DataLiteral)
				filterExp = new Expression(reportManager.getJSManager(), jsFunctionNameRoot + "_filter", jsParams, ((DataLiteral)filter).getString());	
		}
		if(c.containsKey("sort")) {
			DataEntity sort = c.get("sort");
			if(sort instanceof DataMap)
				sortExpMap = new ExpressionMap(reportManager.getJSManager(), jsFunctionNameRoot + "_filter", jsParams, ((DataMap)sort));
			else if(sort instanceof DataLiteral)
				sortExp = new Expression(reportManager.getJSManager(), jsFunctionNameRoot + "_sort", jsParams, ((DataLiteral)sort).getString());	
		}		
	}

	public Box produce(Map<String, Object> context) throws IOException, RedbackException {
		Object currentObject = context.get("object");
		Object currentMasterObject = context.get("master");
		List<?> currentMasterDataset = (List<?>)context.get("dataset");
		Map<String, Object> jsContext = new HashMap<String, Object>();
		jsContext.put("master", currentObject);
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
	}

}
