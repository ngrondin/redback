package io.redback.managers.reportmanager;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import io.firebus.utils.DataList;
import io.firebus.utils.DataMap;
import io.redback.RedbackException;

public class ReportConfig {
	protected ReportManager reportManager;
	protected DataMap config;
	protected String name;
	protected List<ReportUnit> rootUnits;
	
	public ReportConfig(ReportManager rm, DataMap c) throws RedbackException {
		config = c;
		reportManager = rm;
		name = config.getString("name");
		DataList content = config.getList("content");
		rootUnits = new ArrayList<ReportUnit>();
		for(int i = 0; i < content.size(); i++) {
			rootUnits.add(ReportUnit.fromConfig(reportManager, this, content.getObject(i)));
		}
	}
	
	public String getName() {
		return name;
	}
	
	public ReportBox produce(Map<String, Object> context) throws IOException, RedbackException {
		ReportBox root = ReportBox.VContainer(true);
		for(int i = 0; i < rootUnits.size(); i++) {
			root.addChild(rootUnits.get(i).produce(context));
		}
		return root;
	}

}
