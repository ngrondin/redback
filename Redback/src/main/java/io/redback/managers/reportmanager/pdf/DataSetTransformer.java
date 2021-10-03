package io.redback.managers.reportmanager.pdf;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.firebus.data.DataList;
import io.firebus.data.DataMap;
import io.firebus.script.Function;
import io.redback.client.RedbackObjectRemote;
import io.redback.client.js.RedbackObjectRemoteJSWrapper;
import io.redback.exceptions.RedbackException;
import io.redback.managers.reportmanager.ReportConfig;
import io.redback.managers.reportmanager.ReportManager;

public class DataSetTransformer extends ContainerUnit {
	protected Function function;

	public DataSetTransformer(ReportManager rm, ReportConfig rc, DataMap c) throws RedbackException {
		super(rm, rc, c);
		try {
			if(c.containsKey("src")) {
				function = reportManager.getScriptFactory().createFunction(jsFunctionNameRoot + "_transformer", new String[] {"dataset", "object"}, c.getString("src"));
			}
		} catch(Exception e) {
			throw new RedbackException("Error intialising container unit", e);
		}
	}

	@SuppressWarnings("unchecked")
	public Box produce(Map<String, Object> context) throws RedbackException {
		try {
			Object currentObject = context.get("object");
			List<RedbackObjectRemote> currentDataSet = (List<RedbackObjectRemote>)context.get("dataset");
			Map<String, Object> jsContext = new HashMap<String, Object>();
			jsContext.put("dataset", RedbackObjectRemoteJSWrapper.convertList(currentDataSet));
			jsContext.put("object", new RedbackObjectRemoteJSWrapper((RedbackObjectRemote)currentObject));
			Object newDataSet = function.call(jsContext);
			if(newDataSet instanceof DataList) {
				List<Object> list = new ArrayList<Object>();
				for(int i = 0; i < ((DataList)newDataSet).size(); i++)
					list.add(((DataList)newDataSet).get(i));
				newDataSet = list;
			}
			context.put("dataset", newDataSet);
			Box c = Box.VContainer(true);
			for(Unit unit: contentUnits) {
				c.addChild(unit.produce(context));
			}
			//context.put("object", currentObject);
			context.put("dataset", currentDataSet);
			return c;
		} catch(Exception e) {
			throw new RedbackException("Error producing data transformation unit", e);
		}
	}

}
