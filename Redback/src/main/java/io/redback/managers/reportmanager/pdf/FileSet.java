package io.redback.managers.reportmanager.pdf;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.firebus.data.DataMap;
import io.firebus.script.Expression;
import io.firebus.script.exceptions.ScriptException;
import io.redback.client.FileClient;
import io.redback.exceptions.RedbackException;
import io.redback.managers.reportmanager.ReportConfig;
import io.redback.managers.reportmanager.ReportManager;
import io.redback.security.Session;
import io.redback.utils.RedbackFile;

public class FileSet extends ContainerUnit {
	protected String object;
	protected Expression uidExpr;
	
	public FileSet(ReportManager rm, ReportConfig rc, DataMap c) throws RedbackException {
		super(rm, rc, c);
		try {
			object = c.getString("object");
			uidExpr = reportManager.getScriptFactory().createExpression(jsFunctionNameRoot + "_filter", c.getString("uid"));
		} catch(Exception e) {
			throw new RedbackException("Error intialising container unit", e);
		}
	}

	public Box produce(Map<String, Object> context) throws IOException, RedbackException {
		try {
			Object currentObject = context.get("object");
			Object currentMasterObject = context.get("master");
			List<?> currentMasterDataset = (List<?>)context.get("dataset");
			Map<String, Object> jsContext = new HashMap<String, Object>();
			jsContext.put("master", currentObject);
			jsContext.put("filter", context.get("filter"));
			FileClient fc = reportManager.getFileClient();
			Session session = (Session)context.get("session");
			String uid = (String)uidExpr.eval(jsContext);
			List<RedbackFile> files = fc.listFilesFor(session, "formitem", uid);
			context.put("fileset", files);
	
			Box c = Box.VContainer(true);
			c.breakBefore = pagebreak;
			for(Unit unit: contentUnits) {
				c.addChild(unit.produce(context));
			}
			context.put("master", currentMasterObject);
			context.put("object", currentObject);
			context.put("dataset", currentMasterDataset);
			return c;
		} catch(ScriptException e) {
			throw new RedbackException("Error producing file set unit");
		}
	}
}
