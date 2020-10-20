package io.redback.managers.domainmanager;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import io.firebus.Firebus;
import io.firebus.Payload;
import io.firebus.interfaces.Consumer;
import io.firebus.utils.DataEntity;
import io.firebus.utils.DataList;
import io.firebus.utils.DataMap;
import io.redback.RedbackException;
import io.redback.client.ConfigurationClient;
import io.redback.client.DataClient;
import io.redback.client.FileClient;
import io.redback.client.GatewayClient;
import io.redback.client.NotificationClient;
import io.redback.client.ObjectClient;
import io.redback.client.ReportClient;
import io.redback.client.js.FileClientJSWrapper;
import io.redback.client.js.GatewayClientJSWrapper;
import io.redback.client.js.NotificationClientJSWrapper;
import io.redback.client.js.ObjectClientJSWrapper;
import io.redback.client.js.ReportClientJSWrapper;
import io.redback.managers.domainmanager.js.DomainLoggerJS;
import io.redback.managers.domainmanager.js.DomainManagerJSWrapper;
import io.redback.managers.jsmanager.JSManager;
import io.redback.security.Session;
import io.redback.security.js.SessionJSWrapper;
import io.redback.utils.CollectionConfig;
import io.redback.utils.StringUtils;
import io.redback.utils.js.JSConverter;

public class DomainManager implements Consumer {
	private Logger logger = Logger.getLogger("io.redback");
	protected Firebus firebus;
	protected JSManager jsManager;
	protected boolean includeLoaded;
	protected String configServiceName;
	protected String objectServiceName;
	protected String dataServiceName;
	protected String fileServiceName;
	protected String notificationServiceName;
	protected String reportServiceName;
	protected String gatewayClientName;
	protected ConfigurationClient configClient;
	protected ObjectClient objectClient;
	protected DataClient dataClient;
	protected FileClient fileClient;
	protected NotificationClient notificationClient;
	protected ReportClient reportClient;
	protected GatewayClient gatewayClient;
	protected CollectionConfig entryCollection;
	protected CollectionConfig logCollection;
	protected Map<String, DomainEntry> entries;

	public DomainManager(Firebus fb, DataMap config) {
		firebus = fb;
		jsManager = new JSManager();
		includeLoaded = false;
		configServiceName = config.getString("configservice");
		objectServiceName = config.getString("objectservice");
		dataServiceName = config.getString("dataservice");
		fileServiceName = config.getString("fileservice");
		notificationServiceName = config.getString("notificationservice");
		reportServiceName = config.getString("reportservice");
		gatewayClientName = config.getString("gatewayservice");
		configClient = new ConfigurationClient(firebus, configServiceName);
		objectClient = new ObjectClient(firebus, objectServiceName);
		dataClient = new DataClient(firebus, dataServiceName);
		fileClient = new FileClient(firebus, fileServiceName);
		notificationClient = new NotificationClient(firebus, notificationServiceName);
		reportClient = new ReportClient(firebus, reportServiceName);
		gatewayClient = new GatewayClient(firebus, gatewayClientName);
		entryCollection = new CollectionConfig(config.getObject("entrycollection"), "rbdm_entry");
		logCollection = new CollectionConfig(config.getObject("logcollection"), "rbdm_log");
		entries = new HashMap<String, DomainEntry>();	
		firebus.registerConsumer("_rb_domain_cache_clear", this, 10);
	}

	protected void loadIncludeScripts() throws RedbackException
	{
		DataMap result = configClient.listConfigs("rbdm", "include");
		DataList resultList = result.getList("result");
		for(int i = 0; i < resultList.size(); i++)
		{
			try 
			{
				jsManager.addSource("include_" + resultList.getObject(i).getString("name"), resultList.getObject(i).getString("script"));
			}
			catch(Exception e) 
			{
				throw new RedbackException("Problem compiling include scripts", e);
			}
		}
		includeLoaded = true;
	}
	
	protected DomainEntry getDomainEntry(String domain, String name) throws RedbackException {
		DomainEntry entry = entries.get(domain + "." + name);
		if(entry == null)
		{
			try
			{
				DataMap filter = new DataMap();
				filter.put(entryCollection.getField("domain"), domain);
				filter.put(entryCollection.getField("name"), name);
				DataMap resp = dataClient.getData(entryCollection.getName(), filter, null);
				DataList result = resp.getList("result");
				if(result.size() > 0) {
					DataMap entryMap = entryCollection.convertObjectToCanonical(result.getObject(0));
					if(entryMap.getString("type").equals("function")) {
						entry = new DomainFunction(this, jsManager, entryMap);
					} else if(entryMap.getString("type").equals("report")) {
						entry = new DomainReport(entryMap);
					} else if(entryMap.getString("type").equals("variable")) {
						entry = new DomainVariable(entryMap);
					}	
					if(entry.canCache()) {
						entries.put(domain + "." + name, entry);
					}
				}
			}
			catch(Exception e)
			{
				logger.severe(e.getMessage());
				throw new RedbackException("Exception getting domain entry", e);
			}
		}
		return entry;
	}
	
	protected List<DomainEntry> listDomainEntriesInCategory(String domain, String category) throws RedbackException {
		List<DomainEntry> list = new ArrayList<DomainEntry>();
		try
		{
			DataMap filter = new DataMap();
			filter.put(entryCollection.getField("domain"), domain);
			filter.put(entryCollection.getField("category"), category);
			DataMap resp = dataClient.getData(entryCollection.getName(), filter, null);
			DataList result = resp.getList("result");
			for(int i = 0; i < result.size(); i++) {
				DataMap entryMap = entryCollection.convertObjectToCanonical(result.getObject(i));
				DomainEntry entry = null;
				if(entryMap.getString("type").equals("function")) {
					entry = new DomainFunction(this, jsManager, entryMap);
				} else if(entryMap.getString("type").equals("report")) {
					entry = new DomainReport(entryMap);
				} else if(entryMap.getString("type").equals("variable")) {
					entry = new DomainVariable(entryMap);
				}	
				if(entry != null && entry.canCache()) {
					entries.put(domain + "." + entry.name, entry);
				}
				list.add(entry);
			}
		}
		catch(Exception e)
		{
			logger.severe(e.getMessage());
			throw new RedbackException("Exception listing domain entries", e);
		}
		return list;
	}
	
	protected List<DomainEntry> listAllEntriesWithName(String name) throws RedbackException {
		List<DomainEntry> list = new ArrayList<DomainEntry>();
		try
		{
			DataMap filter = new DataMap();
			filter.put(entryCollection.getField("name"), name);
			DataMap resp = dataClient.getData(entryCollection.getName(), filter, null);
			DataList result = resp.getList("result");
			for(int i = 0; i < result.size(); i++) {
				DataMap entryMap = entryCollection.convertObjectToCanonical(result.getObject(i));
				String domain = entryMap.getString("domain");
				DomainEntry entry = null;
				if(entryMap.getString("type").equals("function")) {
					entry = new DomainFunction(this, jsManager, entryMap);
				} else if(entryMap.getString("type").equals("report")) {
					entry = new DomainReport(entryMap);
				} else if(entryMap.getString("type").equals("variable")) {
					entry = new DomainVariable(entryMap);
				}	
				if(entry != null && entry.canCache()) {
					entries.put(domain + "." + entry.name, entry);
				}
				list.add(entry);
			}
		}
		catch(Exception e)
		{
			logger.severe(e.getMessage());
			throw new RedbackException("Exception listing all entries with name", e);
		}
		return list;
	}
	
	
	protected void putEntry(String domain, String name, DomainEntry entry) throws RedbackException {
		DataMap key = new DataMap();
		key.put("domain", domain);
		key.put("name", name);
		dataClient.putData(entryCollection.getName(), entryCollection.convertObjectToSpecific(key), entryCollection.convertObjectToSpecific(entry.getConfig()));
		if(entry.canCache()) {
			//entries.put(domain + "." + name, entry);
			firebus.publish("_rb_domain_cache_clear", new Payload(key.toString()));
		}
	}
	
	protected void addFunctionLog(Session session, DomainFunction function, String message) {
		try {
			String id = function.getDomain() + "." + function.getName() + "." + System.currentTimeMillis();
			DataMap key = new DataMap("_id", id);
			DataMap log = new DataMap();
			log.put("domain", function.getDomain());
			log.put("name", function.getName());
			log.put("entry", message);
			log.put("username", session.getUserProfile().getUsername());
			log.put("date", new Date());
			dataClient.putData(logCollection.getName(), logCollection.convertObjectToSpecific(key), logCollection.convertObjectToSpecific(log));
		} catch(Exception e) {
			logger.severe(StringUtils.rollUpExceptions(e));
		}
	}
	
	public void consume(Payload payload) {
		try {
			DataMap key = new DataMap(payload.getString());
			entries.remove(key.getString("domain") + "." + key.getString("name"));
			includeLoaded = false;
		} catch(Exception e) {
			logger.severe("Error consuming cache control message: " + e.getMessage());
		}
	}
	
	public void putReport(Session session, String domain, String name, String category, DataMap report) throws RedbackException {
		DataMap entryMap = new DataMap();
		entryMap.put("type", "report");
		entryMap.put("domain", domain);
		entryMap.put("name", name);
		entryMap.put("category", category);
		entryMap.put("roles", new DataList());
		entryMap.put("source", report);
		DomainReport dr = new DomainReport(entryMap);
		putEntry(domain, name, dr);
	}
	
	public void putVariable(Session session, String domain, String name, DataEntity var) throws RedbackException {
		DataMap entryMap = new DataMap();
		entryMap.put("type", "variable");
		entryMap.put("domain", domain);
		entryMap.put("name", name);
		entryMap.put("roles", new DataList());
		entryMap.put("source", var);
		DomainVariable dv = new DomainVariable(entryMap);
		putEntry(domain, name, dv);
	}
	
	public void putFunction(Session session, String domain, String name, String function) throws RedbackException {
		DataMap entryMap = new DataMap();
		entryMap.put("type", "variable");
		entryMap.put("domain", domain);
		entryMap.put("name", name);
		entryMap.put("roles", new DataList());
		entryMap.put("source", function);
		DomainFunction df = new DomainFunction(this, jsManager, entryMap);
		putEntry(domain, name, df);
	}
	
	public DataMap getReport(Session session, String domain, String name) throws RedbackException {
		DomainReport dr = (DomainReport)getDomainEntry(domain, name);
		if(dr != null)
			return dr.getReportConfig();
		else
			return null;
	}
	
	public List<DataMap> listReports(Session session, String category) throws RedbackException {
		List<DomainEntry> entries = new ArrayList<DomainEntry>();
		for(String domain :session.getUserProfile().getDomains()) {
			List<DomainEntry> domainEntries = listDomainEntriesInCategory(domain, category);
			entries.addAll(domainEntries);	
		}
		List<DataMap> reportConfigs = entries.stream().map(entry -> ((DomainReport)entry).getReportConfig()).collect(Collectors.toList());
		return reportConfigs;
	}
	
	public DataEntity getVariable(Session session, String domain, String name) throws RedbackException {
		DomainVariable dv = (DomainVariable)getDomainEntry(domain, name);
		if(dv != null)
			return dv.getVariable();
		else 
			return null;
	}

	
	protected Object execute(Session session, DomainFunction df, DataMap param) throws RedbackException {
		Object result = null;
		DomainLoggerJS dl = new DomainLoggerJS(session, this, df);
		Map<String, Object> context = new HashMap<String, Object>();
		context.put("session", new SessionJSWrapper(session));
		context.put("oc", new ObjectClientJSWrapper(objectClient, session));
		context.put("fc", new FileClientJSWrapper(fileClient, session));
		context.put("nc", new NotificationClientJSWrapper(notificationClient, session));
		context.put("rc", new ReportClientJSWrapper(reportClient, session));
		context.put("gc", new GatewayClientJSWrapper(gatewayClient));
		context.put("param", JSConverter.toJS(param));
		context.put("dm", new DomainManagerJSWrapper(this, session, df.getDomain()));
		context.put("domain", df.getDomain());
		context.put("log", dl);

		if(!includeLoaded)
			loadIncludeScripts();

		try {
			result = df.execute(context);
			dl.commit();
		} catch(Exception e) {
			dl.log(StringUtils.rollUpExceptions(e));
			dl.commit();
			throw new RedbackException("Error executing domain script", e);
		}
		return result;
	}
	
	protected void executeAsync(Session session, DomainFunction df, DataMap param) {
		Thread worker = new Thread() {
			public void run() {
				try {
					execute(session, df, param);
				} catch(Exception e) {}
			}
		};
		worker.start();	
	}
	
	public Object executeFunction(Session session, String domain, String name, DataMap param, boolean async) throws RedbackException {
		Object result = null;
		DomainEntry de = getDomainEntry(domain, name);
		if(de != null && de instanceof DomainFunction) {
			DomainFunction df = (DomainFunction)de;
			if(async)
				executeAsync(session, df, param);
			else
				result = execute(session, df, param);
		}
		return result;
	}
	
	public void executeFunctionInAllDomains(Session session, String name, DataMap param) {
		try {
			if(!includeLoaded) 
				loadIncludeScripts();
			
			List<DomainEntry> functions = new ArrayList<DomainEntry>();
			List<DomainEntry> allFunctions = listAllEntriesWithName(name);
			if(session.getUserProfile().getDomains().contains("*")) {
				functions.addAll(allFunctions);
			} else {
				List<String> domains = session.getUserProfile().getDomains();
				for(String d: domains) {
					for(DomainEntry de: allFunctions) {
						if(de.getDomain().equals(d))
							functions.add(de);
					}
				}
			}
			
			if(functions.size() > 0) {
				for(DomainEntry de: functions) {
					DomainFunction df = (DomainFunction)de;
					executeAsync(session, df, param);
				}
			} 			
		} catch(Exception e) {
			logger.severe(StringUtils.rollUpExceptions(e));
		}
	}
	
	public void clearCache(Session session, String domain, String name) {
		DataMap key = new DataMap();
		key.put("domain", domain);
		key.put("name", name);
		firebus.publish("_rb_domain_cache_clear", new Payload(key.toString()));
	}
}
