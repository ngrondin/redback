package io.redback.managers.reportmanager.pdf;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.firebus.data.DataList;
import io.firebus.data.DataMap;
import io.redback.exceptions.RedbackException;
import io.redback.managers.jsmanager.Function;
import io.redback.managers.reportmanager.ReportConfig;
import io.redback.managers.reportmanager.ReportManager;
import io.redback.utils.js.JSConverter;

public class DataSetTransformer extends ContainerUnit {
	protected Function function;

	public DataSetTransformer(ReportManager rm, ReportConfig rc, DataMap c) throws RedbackException {
		super(rm, rc, c);
		jsParams = Arrays.asList(new String[] {"dataset", "object"});
		if(c.containsKey("src")) {
			function = new Function(reportManager.getJSManager(), jsFunctionNameRoot + "_transformer", jsParams, c.getString("src"));
		}
	}

	public Box produce(Map<String, Object> context) throws IOException, RedbackException {
		Object currentObject = context.get("object");
		List<?> currentDataSet = (List<?>)context.get("dataset");
		Map<String, Object> jsContext = new HashMap<String, Object>();
		jsContext.put("dataset", JSConverter.toJS(currentDataSet));
		jsContext.put("object", JSConverter.toJS(currentObject));
		Object newDataSet = function.execute(jsContext);
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
		
	}

}
