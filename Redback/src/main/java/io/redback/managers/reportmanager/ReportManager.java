package io.redback.managers.reportmanager;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import io.firebus.Firebus;
import io.firebus.utils.DataMap;
import io.redback.RedbackException;
import io.redback.client.ConfigurationClient;
import io.redback.client.FileClient;
import io.redback.client.ObjectClient;
import io.redback.managers.jsmanager.JSManager;
import io.redback.security.Session;

public class ReportManager {
	private Logger logger = Logger.getLogger("io.redback");
	protected Firebus firebus;
	protected JSManager jsManager;
	protected String configServiceName;
	protected String objectServiceName;
	protected String fileServiceName;
	protected ConfigurationClient configClient;
	protected ObjectClient objectClient;
	protected FileClient fileClient;
	protected Map<String, ReportConfig> configs;

	public ReportManager(Firebus fb, DataMap config) {
		firebus = fb;
		jsManager = new JSManager();
		configServiceName = config.getString("configservice");
		objectServiceName = config.getString("objectservice");
		fileServiceName = config.getString("fileservice");
		configClient = new ConfigurationClient(firebus, configServiceName);
		objectClient = new ObjectClient(firebus, objectServiceName);
		fileClient = new FileClient(firebus, fileServiceName);
		configs = new HashMap<String, ReportConfig>();
	}
	
	protected ReportConfig getConfig(String name) throws RedbackException {
		ReportConfig reportConfig = configs.get(name);
		if(reportConfig == null)
		{
			try
			{
				reportConfig = new ReportConfig(this, configClient.getConfig("rbrs", "report", name));
				configs.put(name, reportConfig);
			}
			catch(Exception e)
			{
				logger.severe(e.getMessage());
				throw new RedbackException("Exception getting report config", e);
			}
		}
		return reportConfig;
	}
	
	public ObjectClient getObjectClient() {
		return objectClient;
	}
	
	public FileClient getFileClient() {
		return fileClient;
	}
	
	public JSManager getJSManager() {
		return jsManager;
	}

	public Report produce(Session session, String name, DataMap filter) throws RedbackException {
		ReportConfig config = getConfig(name);
		Report report = null;
		if(config != null) {
			report = new Report(session, this, config);
			report.produce(filter);
		} 
		return report;
	}

	public void clearCaches() {
		configs.clear();
	}
}
