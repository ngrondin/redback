package io.redback.managers.reportmanager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.firebus.Firebus;
import io.firebus.Payload;
import io.firebus.data.DataList;
import io.firebus.data.DataMap;
import io.firebus.interfaces.Consumer;
import io.firebus.script.ScriptFactory;
import io.redback.client.ConfigClient;
import io.redback.client.DataClient;
import io.redback.client.FileClient;
import io.redback.client.ObjectClient;
import io.redback.exceptions.RedbackException;
import io.redback.managers.reportmanager.csv.CSVReport;
import io.redback.managers.reportmanager.excel.ExcelReport;
import io.redback.managers.reportmanager.pdf.PDFReport;
import io.redback.managers.reportmanager.txt.TXTReport;
import io.redback.security.Session;
import io.redback.utils.ConfigCache;
import io.redback.utils.RedbackFileMetaData;
import io.redback.utils.js.RedbackUtilsJSWrapper;

public class ReportManager implements Consumer {
	protected Firebus firebus;
	protected ScriptFactory scriptFactory;
	protected String configServiceName;
	protected String objectServiceName;
	protected String dataServiceName;
	protected String fileServiceName;
	protected ConfigClient configClient;
	protected ObjectClient objectClient;
	protected DataClient dataClient;
	protected FileClient fileClient;
	protected boolean includeLoaded;
	protected ConfigCache<ReportConfig> configs;
	protected Map<Integer, List<ReportConfig>> listsQueried;

	public ReportManager(Firebus fb, DataMap config) throws RedbackException {
		try {
			firebus = fb;
			scriptFactory = new ScriptFactory();
			includeLoaded = false;
			configServiceName = config.getString("configservice");
			objectServiceName = config.getString("objectservice");
			dataServiceName = config.getString("dataservice");
			fileServiceName = config.getString("fileservice");
			configClient = new ConfigClient(firebus, configServiceName);
			objectClient = new ObjectClient(firebus, objectServiceName);
			dataClient = new DataClient(firebus, dataServiceName);
			fileClient = new FileClient(firebus, fileServiceName);
			ReportManager rm = this;
			configs = new ConfigCache<ReportConfig>(configClient, "rbrs", "report", 3600000, new ConfigCache.ConfigFactory<ReportConfig>() {
				public ReportConfig createConfig(DataMap map) throws Exception {
					return new ReportConfig(rm, map);
				}
			});
			listsQueried = new HashMap<Integer, List<ReportConfig>>();
			firebus.registerConsumer("_rb_domain_cache_clear", this, 10);
			scriptFactory.setInRootScope("rbutils", new RedbackUtilsJSWrapper());
		} catch(Exception e) {
			throw new RedbackException("Error initialising Report Manager", e);
		}
	}

	public void consume(Payload payload) {
		try {
			DataMap key = payload.getDataMap();
			configs.clear(key.getString("name"), key.getString("domain"));
			listsQueried.clear();
		} catch(Exception e) {
		}
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

	public ObjectClient getObjectClient() {
		return objectClient;
	}
	
	public FileClient getFileClient() {
		return fileClient;
	}
	
	public ScriptFactory getScriptFactory() {
		return scriptFactory;
	}

	public Report produce(Session session, String name, String object, DataMap filter, String search) throws RedbackException {
		ReportConfig config = configs.get(session, name);
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
			else if(config.getType().equals("txt"))
				report = new TXTReport(session, this, config);
			if(report != null)
				report.produce(object, filter, search);
			else 
				throw new RedbackException("Unknown report type");
		} 
		return report;
	}
	
	public String produceAndStore(Session session, String name, String object, DataMap filter, String search) throws RedbackException {
		Report report = produce(session, name, object, filter, search);
		if(report != null) {
			RedbackFileMetaData filemd = fileClient.putFile(session, name + ".pdf", "application/pdf", session.getUserProfile().getUsername(), report.getBytes());
			return filemd.fileuid;
		} else {
			return null;
		}
	}
	
	public List<ReportInfo> list(Session session, String category) throws RedbackException {
		List<ReportConfig> confgiList = configs.list(session, new DataMap("category", category));
		List<ReportInfo> infos = new ArrayList<ReportInfo>();
		for(ReportConfig rc: confgiList) {
			infos.add(new ReportInfo(rc.getName(), rc.getDescription(), rc.getType(), rc.getDomain()));
		}
		return infos;		
	}
	
	public void clearCaches() {
		configs.clear();
		configs.clear();
		listsQueried.clear();
	}
	
	public void clearDomainCache(Session session, String domain, String name) throws RedbackException {
		DataMap key = new DataMap();
		key.put("domain", domain);
		key.put("name", name);
		firebus.publish("_rb_domain_cache_clear", new Payload(key));
	}

}
