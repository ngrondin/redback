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
import io.redback.client.ObjectClient;
import io.redback.client.js.FileClientJSWrapper;
import io.redback.managers.jsmanager.JSManager;
import io.redback.security.Session;
import io.redback.security.js.SessionJSWrapper;
import io.redback.utils.CollectionConfig;

public class DomainManager implements Consumer {
	private Logger logger = Logger.getLogger("io.redback");
	protected Firebus firebus;
	protected JSManager jsManager;
	protected String configServiceName;
	protected String objectServiceName;
	protected String dataServiceName;
	protected String fileServiceName;
	protected ConfigurationClient configClient;
	protected ObjectClient objectClient;
	protected DataClient dataClient;
	protected FileClient fileClient;
	protected CollectionConfig collection;
	protected Map<String, DomainEntry> entries;

	public DomainManager(Firebus fb, DataMap config) {
		firebus = fb;
		jsManager = new JSManager();
		configServiceName = config.getString("configservice");
		objectServiceName = config.getString("objectservice");
		dataServiceName = config.getString("dataservice");
		fileServiceName = config.getString("fileservice");
		configClient = new ConfigurationClient(firebus, configServiceName);
		objectClient = new ObjectClient(firebus, objectServiceName);
		dataClient = new DataClient(firebus, dataServiceName);
		fileClient = new FileClient(firebus, fileServiceName);
		collection = new CollectionConfig(config.getObject("collection"));
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
				filter.put(collection.getField("_id"), name);
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
		key.put(collection.getField("domain"), domain);
		key.put(collection.getField("_id"), name);
		dataClient.putData(collection.getName(), key, collection.convertObjectToSpecific(entry.getConfig()));
		if(entry.canCache()) {
			entries.put(domain + "." + name, entry);
			firebus.publish("_rb_domain_cache_clear", new Payload(key.toString()));
		}
	}
	
	public void consume(Payload payload) {
		try {
			DataMap msg = new DataMap(payload.getString());
			entries.remove(msg.getString("domain") + "." + msg.getString("name"));
		} catch(Exception e) {
			logger.severe("Error consuming cache control message: " + e.getMessage());
		}
	}
	
	public void putReport(Session session, String name, String category, DataMap report) throws RedbackException {
		String domain = session.getUserProfile().getAttribute("rb.defaultdomain");
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
	
	public void putVariable(Session session, String name, String category, DataEntity var) throws RedbackException {
		String domain = session.getUserProfile().getAttribute("rb.defaultdomain");
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
	
	public void putFunction(Session session, String name, String function) throws RedbackException {
		String domain = session.getUserProfile().getAttribute("rb.defaultdomain");
		DataMap entryMap = new DataMap();
		entryMap.put("type", "variable");
		entryMap.put("domain", domain);
		entryMap.put("name", name);
		entryMap.put("roles", new DataList());
		entryMap.put("function", function);
		DomainFunction df = new DomainFunction(this, jsManager, entryMap);
		putEntry(domain, name, df);
	}
	
	public DataMap getReport(Session session, String name) throws RedbackException {
		String domain = session.getUserProfile().getAttribute("rb.defaultdomain");
		DomainReport dr = (DomainReport)getEntry(domain, name);
		return dr.getReportConfig();
	}
	
	public List<DataMap> listReports(Session session, String category) throws RedbackException {
		String domain = session.getUserProfile().getAttribute("rb.defaultdomain");
		List<DomainEntry> entries = listEntries(domain, category);
		List<DataMap> reportConfigs = entries.stream().map(entry -> ((DomainReport)entry).getReportConfig()).collect(Collectors.toList());
		return reportConfigs;
	}
	
	public DataEntity getVariable(Session session, String name) throws RedbackException {
		String domain = session.getUserProfile().getAttribute("rb.defaultdomain");
		DomainVariable dv = (DomainVariable)getEntry(domain, name);
		return dv.getVariable();
	}
	
	public void executeFunction(Session session, String name, DataMap param) throws RedbackException {
		String domain = session.getUserProfile().getAttribute("rb.defaultdomain");
		DomainFunction df = (DomainFunction)getEntry(domain, name);
		Map<String, Object> context = new HashMap<String, Object>();
		context.put("session", new SessionJSWrapper(session));
		//context.put("dm", new SessionJSWrapper(session));
		//context.put("oc", new FileClientJSWrapper(fileClient, session));
		context.put("fc", new FileClientJSWrapper(fileClient, session));
		df.execute(context);
	}
}
