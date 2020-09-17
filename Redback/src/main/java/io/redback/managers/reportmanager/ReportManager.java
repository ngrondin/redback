package io.redback.managers.reportmanager;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import io.firebus.Firebus;
import io.firebus.utils.DataMap;
import io.redback.RedbackException;
import io.redback.client.ConfigurationClient;
import io.redback.client.DomainClient;
import io.redback.client.FileClient;
import io.redback.client.ObjectClient;
import io.redback.managers.jsmanager.JSManager;
import io.redback.security.Session;
import io.redback.utils.RedbackFile;

public class ReportManager {
	private Logger logger = Logger.getLogger("io.redback");
	protected Firebus firebus;
	protected JSManager jsManager;
	protected String configServiceName;
	protected String objectServiceName;
	protected String fileServiceName;
	protected String domainServiceName;
	protected ConfigurationClient configClient;
	protected ObjectClient objectClient;
	protected FileClient fileClient;
	protected DomainClient domainClient;
	protected Map<String, ReportConfig> configs;
	protected Map<String, Map<String, ReportConfig>> domainConfigs;

	public ReportManager(Firebus fb, DataMap config) {
		firebus = fb;
		jsManager = new JSManager();
		configServiceName = config.getString("configservice");
		objectServiceName = config.getString("objectservice");
		fileServiceName = config.getString("fileservice");
		domainServiceName = config.getString("domainservice");
		configClient = new ConfigurationClient(firebus, configServiceName);
		objectClient = new ObjectClient(firebus, objectServiceName);
		fileClient = new FileClient(firebus, fileServiceName);
		domainClient = new DomainClient(firebus, domainServiceName);
		configs = new HashMap<String, ReportConfig>();
		domainConfigs = new HashMap<String, Map<String, ReportConfig>>();
	}
	
	protected ReportConfig getConfig(Session session, String domain, String name) throws RedbackException {
		ReportConfig reportConfig = null;
		List<String> domains = session.getUserProfile().getDomains();
		if(domain != null && (domains.contains(domain) || domains.contains("*"))) {
			domains.clear();
			domains.add(domain);
		}
		
		for(int i = 0; i < domains.size() && reportConfig == null; i++) {
			String d = domains.get(i);
			if(!d.equals("*")) {
				Map<String, ReportConfig> dc = domainConfigs.get(d);
				if(dc == null) {
					dc = new HashMap<String, ReportConfig>();
					domainConfigs.put(d, dc);
				}
				if(!dc.containsKey(name)) {
					DataMap reportData = domainClient.getReport(session, d, name);
					if(reportData != null) {
						reportConfig = new ReportConfig(this, reportData);
						dc.put(name, reportConfig);
					} else {
						dc.put(name, null);
					}
				} else {
					reportConfig = dc.get(name);
				}			
			}
		}
		
		if(reportConfig == null) {
			reportConfig = configs.get(name); 
		}
		
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

	public Report produce(Session session, String domain, String name, DataMap filter) throws RedbackException {
		ReportConfig config = getConfig(session, domain, name);
		Report report = null;
		if(config != null) {
			report = new Report(session, this, config);
			report.produce(filter);
		} 
		return report;
	}
	
	public String produceAndStore(Session session, String domain, String name, DataMap filter) throws RedbackException {
		Report report = produce(session, domain, name, filter);
		if(report != null) {
			RedbackFile file = fileClient.putFile(session, name + ".pdf", "application/pdf", session.getUserProfile().getUsername(), report.getBytes());
			return file.uid;
		} else {
			return null;
		}
	}

	public void clearCaches() {
		configs.clear();
	}
}
