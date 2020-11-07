package io.redback.managers.reportmanager.units;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.firebus.utils.DataEntity;
import io.firebus.utils.DataLiteral;
import io.firebus.utils.DataMap;
import io.redback.RedbackException;
import io.redback.client.ObjectClient;
import io.redback.client.RedbackObjectRemote;
import io.redback.client.js.RedbackObjectRemoteJSWrapper;
import io.redback.managers.jsmanager.Expression;
import io.redback.managers.jsmanager.ExpressionMap;
import io.redback.managers.reportmanager.ReportBox;
import io.redback.managers.reportmanager.ReportConfig;
import io.redback.managers.reportmanager.ReportContainerUnit;
import io.redback.managers.reportmanager.ReportManager;
import io.redback.managers.reportmanager.ReportUnit;
import io.redback.security.Session;
import io.redback.utils.js.JSConverter;


public class DataSet extends ReportContainerUnit {
	protected String object;
	protected Expression filterExp;
	protected ExpressionMap filterExpMap;
	protected Expression sortExp;
	protected ExpressionMap sortExpMap;
	
	public DataSet(ReportManager rm, ReportConfig rc, DataMap c) throws RedbackException {
		super(rm, rc, c);
		jsParams = Arrays.asList(new String[] {"filter", "object"});
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
				sortExp = new Expression(reportManager.getJSManager(), jsFunctionNameRoot + "_filter", jsParams, ((DataLiteral)sort).getString());	
		}		
	}

	public ReportBox produce(Map<String, Object> context) throws IOException, RedbackException {
		RedbackObjectRemote currentObject = (RedbackObjectRemote)context.get("object");
		List<?> currentDataSet = (List<?>)context.get("dataset");
		Map<String, Object> jsContext = new HashMap<String, Object>();
		jsContext.put("filter", JSConverter.toJS(context.get("filter")));
		jsContext.put("object", new RedbackObjectRemoteJSWrapper(currentObject));
		ObjectClient oc = reportManager.getObjectClient();
		DataMap filter = (filterExp != null ? (DataMap)filterExp.eval(jsContext) : filterExpMap.eval(jsContext));
		DataMap sort = (sortExp != null ? (DataMap)sortExp.eval(jsContext) : sortExpMap != null ? sortExpMap.eval(jsContext) : null);
		Session session = (Session)context.get("session");
		List<RedbackObjectRemote> rors = oc.listAllObjects(session, object, filter, sort, true);
		context.put("dataset", rors);

		ReportBox c = ReportBox.VContainer(true);

		for(ReportUnit unit: contentUnits) {
			c.addChild(unit.produce(context));
		}
		context.put("object", currentObject);
		context.put("dataset", currentDataSet);
		return c;
	}

}
