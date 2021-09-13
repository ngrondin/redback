package io.redback.managers.reportmanager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
//import java.util.logging.Logger;

import io.firebus.Firebus;
import io.firebus.data.DataList;
import io.firebus.data.DataMap;
import io.firebus.script.ScriptFactory;
import io.redback.client.ConfigurationClient;
import io.redback.client.DataClient;
import io.redback.client.FileClient;
import io.redback.client.ObjectClient;
import io.redback.exceptions.RedbackException;
import io.redback.managers.reportmanager.csv.CSVReport;
import io.redback.managers.reportmanager.excel.ExcelReport;
import io.redback.managers.reportmanager.pdf.PDFReport;
import io.redback.security.Session;
import io.redback.utils.CollectionConfig;
import io.redback.utils.RedbackFileMetaData;

public class ReportManager {
	//private Logger logger = Logger.getLogger("io.redback");
	protected Firebus firebus;
	protected ScriptFactory scriptFactory;
	protected String configServiceName;
	protected String objectServiceName;
	protected String dataServiceName;
	protected String fileServiceName;
	protected ConfigurationClient configClient;
	protected ObjectClient objectClient;
	protected DataClient dataClient;
	protected FileClient fileClient;
	protected CollectionConfig collection;
	protected boolean includeLoaded;
	protected Map<String, ReportConfig> configs;
	protected Map<String, Map<String, ReportConfig>> domainConfigs;
	protected Map<Integer, List<ReportConfig>> listsQueried;

	public ReportManager(Firebus fb, DataMap config) {
		firebus = fb;
		scriptFactory = new ScriptFactory();
		includeLoaded = false;
		configServiceName = config.getString("configservice");
		objectServiceName = config.getString("objectservice");
		dataServiceName = config.getString("dataservice");
		fileServiceName = config.getString("fileservice");
		configClient = new ConfigurationClient(firebus, configServiceName);
		objectClient = new ObjectClient(firebus, objectServiceName);
		dataClient = new DataClient(firebus, dataServiceName);
		fileClient = new FileClient(firebus, fileServiceName);
		collection = new CollectionConfig(config.getObject("collection"));
		configs = new HashMap<String, ReportConfig>();
		domainConfigs = new HashMap<String, Map<String, ReportConfig>>();
		listsQueried = new HashMap<Integer, List<ReportConfig>>();
	}
	
	protected void loadIncludeScripts(Session session) throws RedbackException
	{
		DataMap result = configClient.listConfigs(session, "rbrs", "include");
		DataList resultList = result.getList("result");
		for(int i = 0; i < resultList.size(); i++)
		{
			try 
			{
				scriptFactory.executeInRootScope("include_" + resultList.getObject(i).getString("name"), resultList.getObject(i).getString("script"));
			}
			catch(Exception e) 
			{
				throw new RedbackException("Problem compiling include scripts", e);
			}
		}
		includeLoaded = true;
	}
	
	protected ReportConfig getConfig(Session session, String domain, String name) throws RedbackException {
		if(domain != null) {
			List<String> userDomains = session.getUserProfile().getDomains();
			if(userDomains.contains(domain) || userDomains.contains("*")) {
				if(!domainConfigs.containsKey(domain)) 
					domainConfigs.put(domain, new HashMap<String, ReportConfig>());
				if(domainConfigs.get(domain).containsKey(name)) {
					return domainConfigs.get(domain).get(name);
				} else {
					DataMap key = new DataMap();
					key.put("domain", domain);
					key.put("name", name);
					DataMap res = dataClient.getData(collection.getName(), collection.convertObjectToSpecific(key), null);
					if(res.containsKey("result") && res.getList("result").size() > 0) {
						DataMap cfg = collection.convertObjectToCanonical(res.getList("result").getObject(0));
						ReportConfig rc = new ReportConfig(this, cfg);
						domainConfigs.get(domain).put(name, rc);
						return rc;
					} else {
						return null;
					}
				} 
			} else {
				return null;
			}
		} else {
			if(configs.containsKey(name)) {
				return configs.get(name);
			} else {
				ReportConfig rc = new ReportConfig(this, configClient.getConfig(session, "rbrs", "report", name));
				configs.put(name, rc);
				return rc;
			}
		}
	}
	
	protected List<ReportConfig> listConfigs(Session session, String category) throws RedbackException {
		DataMap filter = new DataMap();
		filter.put("domain", session.getUserProfile().getDBFilterDomainClause());
		filter.put("category", category);
		int h = filter.toString().hashCode();
		if(!listsQueried.containsKey(h)) {
			List<ReportConfig> list = new ArrayList<ReportConfig>();
			DataMap res = dataClient.getData(collection.getName(), collection.convertObjectToSpecific(filter), null);
			if(res.containsKey("result")) {
				DataList resList = res.getList("result");
				for(int i = 0; i < resList.size(); i++) {
					DataMap cfg = collection.convertObjectToCanonical(resList.getObject(i));
					list.add(new ReportConfig(this, cfg));
				}
			} 
			res = configClient.listConfigs(session, "rbrs", "report", new DataMap("category", category));
			if(res.containsKey("result")) {
				DataList resList = res.getList("result");
				for(int i = 0; i < resList.size(); i++) {
					DataMap cfg = collection.convertObjectToCanonical(resList.getObject(i));
					list.add(new ReportConfig(this, cfg));
				}
			} 
			listsQueried.put(h, list);
			return list;
		} else {
			return listsQueried.get(h);
		}
	}

	public ObjectClient getObjectClient() {
		return objectClient;
	}
	
	public FileClient getFileClient() {
		return fileClient;
	}
	
	public ScriptFactory getScriptFactory() {
		return scriptFactory;
	}

	public Report produce(Session session, String domain, String name, DataMap filter) throws RedbackException {
		ReportConfig config = getConfig(session, domain, name);
		if(!includeLoaded)
			loadIncludeScripts(session);		
		Report report = null;
		if(config != null) {
			if(config.getType().equals("pdf"))
				report = new PDFReport(session, this, config);
			else if(config.getType().equals("csv"))
				report = new CSVReport(session, this, config);
			else if(config.getType().equals("excel"))
				report = new ExcelReport(session, this, config);
			if(report != null)
				report.produce(filter);
			else 
				throw new RedbackException("Unknown report type");
		} 
		return report;
	}
	
	public String produceAndStore(Session session, String domain, String name, DataMap filter) throws RedbackException {
		Report report = produce(session, domain, name, filter);
		if(report != null) {
			RedbackFileMetaData filemd = fileClient.putFile(session, name + ".pdf", "application/pdf", session.getUserProfile().getUsername(), report.getBytes());
			return filemd.fileuid;
		} else {
			return null;
		}
	}
	
	public List<ReportInfo> list(Session session, String category) throws RedbackException {
		List<ReportConfig> configs = listConfigs(session, category);
		List<ReportInfo> infos = new ArrayList<ReportInfo>();
		for(ReportConfig rc: configs) {
			infos.add(new ReportInfo(rc.getName(), rc.getDescription(), rc.getType(), rc.getDomain()));
		}
		return infos;		
	}
	
	public void clearCaches() {
		configs.clear();
		domainConfigs.clear();
		listsQueried.clear();
	}
	
	public void clearDomainCache(Session session, String domain, String name) throws RedbackException {
		if(domainConfigs.get(domain) != null)
			domainConfigs.get(domain).remove(name);
	}
}
