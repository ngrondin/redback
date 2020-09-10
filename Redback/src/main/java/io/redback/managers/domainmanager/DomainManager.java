package io.redback.managers.domainmanager;

import java.util.ArrayList;
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
import io.redback.client.NotificationClient;
import io.redback.client.ObjectClient;
import io.redback.client.ReportClient;
import io.redback.client.js.FileClientJSWrapper;
import io.redback.client.js.NotificationClientJSWrapper;
import io.redback.client.js.ObjectClientJSWrapper;
import io.redback.client.js.ReportClientJSWrapper;
import io.redback.managers.domainmanager.js.DomainManagerJSWrapper;
import io.redback.managers.jsmanager.JSManager;
import io.redback.security.Session;
import io.redback.security.js.SessionJSWrapper;
import io.redback.utils.CollectionConfig;
import io.redback.utils.js.JSConverter;
import io.redback.utils.js.LoggerJSFunction;

public class DomainManager implements Consumer {
	private Logger logger = Logger.getLogger("io.redback");
	protected Firebus firebus;
	protected JSManager jsManager;
	protected String configServiceName;
	protected String objectServiceName;
	protected String dataServiceName;
	protected String fileServiceName;
	protected String notificationServiceName;
	protected String reportServiceName;
	protected ConfigurationClient configClient;
	protected ObjectClient objectClient;
	protected DataClient dataClient;
	protected FileClient fileClient;
	protected NotificationClient notificationClient;
	protected ReportClient reportClient;
	protected CollectionConfig collection;
	protected Map<String, DomainEntry> entries;

	public DomainManager(Firebus fb, DataMap config) {
		firebus = fb;
		jsManager = new JSManager();
		configServiceName = config.getString("configservice");
		objectServiceName = config.getString("objectservice");
		dataServiceName = config.getString("dataservice");
		fileServiceName = config.getString("fileservice");
		notificationServiceName = config.getString("notificationservice");
		reportServiceName = config.getString("reportservice");
		configClient = new ConfigurationClient(firebus, configServiceName);
		objectClient = new ObjectClient(firebus, objectServiceName);
		dataClient = new DataClient(firebus, dataServiceName);
		fileClient = new FileClient(firebus, fileServiceName);
		notificationClient = new NotificationClient(firebus, notificationServiceName);
		reportClient = new ReportClient(firebus, reportServiceName);
		collection = new CollectionConfig(config.getObject("collection"), "rbdm_entry");
		entries = new HashMap<String, DomainEntry>();	
		firebus.registerConsumer("_rb_domain_cache_clear", this, 10);
	}

	protected DomainEntry getEntry(String domain, String name) throws RedbackException {
		DomainEntry entry = entries.get(domain + "." + name);
		if(entry == null)
		{
			try
			{
				DataMap filter = new DataMap();
				filter.put(collection.getField("domain"), domain);
				filter.put(collection.getField("name"), name);
				DataMap resp = dataClient.getData(collection.getName(), filter, null);
				DataList result = resp.getList("result");
				if(result.size() > 0) {
					DataMap entryMap = collection.convertObjectToCanonical(result.getObject(0));
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
	
	protected List<DomainEntry> listEntries(String domain, String category) throws RedbackException {
		List<DomainEntry> list = new ArrayList<DomainEntry>();
		try
		{
			DataMap filter = new DataMap();
			filter.put(collection.getField("domain"), domain);
			filter.put(collection.getField("category"), category);
			DataMap resp = dataClient.getData(collection.getName(), filter, null);
			DataList result = resp.getList("result");
			for(int i = 0; i < result.size(); i++) {
				DataMap entryMap = collection.convertObjectToCanonical(result.getObject(i));
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
			}
		}
		catch(Exception e)
		{
			logger.severe(e.getMessage());
			throw new RedbackException("Exception listing domain entries", e);
		}
		return list;
	}
	
	
	protected void putEntry(String domain, String name, DomainEntry entry) throws RedbackException {
		DataMap key = new DataMap();
		key.put("domain", domain);
		key.put("name", name);
		dataClient.putData(collection.getName(), collection.convertObjectToSpecific(key), collection.convertObjectToSpecific(entry.getConfig()));
		if(entry.canCache()) {
			//entries.put(domain + "." + name, entry);
			firebus.publish("_rb_domain_cache_clear", new Payload(key.toString()));
		}
	}
	
	public void consume(Payload payload) {
		try {
			DataMap key = new DataMap(payload.getString());
			entries.remove(key.getString("domain") + "." + key.getString("name"));
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
		entryMap.put("report", report);
		DomainReport dr = new DomainReport(entryMap);
		putEntry(domain, name, dr);
	}
	
	public void putVariable(Session session, String domain, String name, String category, DataEntity var) throws RedbackException {
		DataMap entryMap = new DataMap();
		entryMap.put("type", "variable");
		entryMap.put("domain", domain);
		entryMap.put("name", name);
		entryMap.put("category", category);
		entryMap.put("roles", new DataList());
		entryMap.put("variable", var);
		DomainVariable dv = new DomainVariable(entryMap);
		putEntry(domain, name, dv);
	}
	
	public void putFunction(Session session, String domain, String name, String function) throws RedbackException {
		DataMap entryMap = new DataMap();
		entryMap.put("type", "variable");
		entryMap.put("domain", domain);
		entryMap.put("name", name);
		entryMap.put("roles", new DataList());
		entryMap.put("function", function);
		DomainFunction df = new DomainFunction(this, jsManager, entryMap);
		putEntry(domain, name, df);
	}
	
	public DataMap getReport(Session session, String domain, String name) throws RedbackException {
		DomainReport dr = (DomainReport)getEntry(domain, name);
		return dr.getReportConfig();
	}
	
	public List<DataMap> listReports(Session session, String category) throws RedbackException {
		List<DomainEntry> entries = new ArrayList<DomainEntry>();
		for(String domain :session.getUserProfile().getDomains()) {
			List<DomainEntry> domainEntries = listEntries(domain, category);
			entries.addAll(domainEntries);	
		}
		List<DataMap> reportConfigs = entries.stream().map(entry -> ((DomainReport)entry).getReportConfig()).collect(Collectors.toList());
		return reportConfigs;
	}
	
	public DataEntity getVariable(Session session, String domain, String name) throws RedbackException {
		DomainVariable dv = (DomainVariable)getEntry(domain, name);
		return dv.getVariable();
	}
	
	public DataMap executeFunction(Session session, String domain, String name, DataMap param) throws RedbackException {
		DomainFunction df = (DomainFunction)getEntry(domain, name);
		if(df != null) {
			Map<String, Object> context = new HashMap<String, Object>();
			context.put("log", new LoggerJSFunction());
			context.put("session", new SessionJSWrapper(session));
			context.put("dm", new DomainManagerJSWrapper(this, session, domain));
			context.put("oc", new ObjectClientJSWrapper(objectClient, session));
			context.put("fc", new FileClientJSWrapper(fileClient, session));
			context.put("nc", new NotificationClientJSWrapper(notificationClient, session));
			context.put("rc", new ReportClientJSWrapper(reportClient, session));
			context.put("param", JSConverter.toJS(param));
			Object o = df.execute(context);
			if(o instanceof DataMap)
				return (DataMap)o;
			else
				return null;
		} else {
			return null;
		}
	}
	
	public void clearCache(Session session, String domain, String name) {
		DataMap key = new DataMap();
		key.put("domain", domain);
		key.put("name", name);
		firebus.publish("_rb_domain_cache_clear", new Payload(key.toString()));
	}
}
