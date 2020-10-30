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
	protected List<ReportUnit> footerUnits;
	
	public ReportConfig(ReportManager rm, DataMap c) throws RedbackException {
		config = c;
		reportManager = rm;
		name = config.getString("name");
		DataList content = config.getList("content");
		rootUnits = new ArrayList<ReportUnit>();
		for(int i = 0; i < content.size(); i++) {
			rootUnits.add(ReportUnit.fromConfig(reportManager, this, content.getObject(i)));
		}
		DataList footer = config.getList("footer");
		if(footer != null) {
			footerUnits = new ArrayList<ReportUnit>();
			for(int i = 0; i < footer.size(); i++) {
				footerUnits.add(ReportUnit.fromConfig(reportManager, this, footer.getObject(i)));
			}		
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
	
	public ReportBox produceFooter(Map<String, Object> context) throws IOException, RedbackException {
		ReportBox footer = ReportBox.VContainer(false);
		if(footerUnits != null) {
			for(int i = 0; i < footerUnits.size(); i++) {
				footer.addChild(footerUnits.get(i).produce(context));
			}
		}
		return footer;
	}
	
}
