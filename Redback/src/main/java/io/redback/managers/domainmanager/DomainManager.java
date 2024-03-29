package io.redback.managers.domainmanager;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.firebus.Firebus;
import io.firebus.Payload;
import io.firebus.data.DataEntity;
import io.firebus.data.DataList;
import io.firebus.data.DataMap;
import io.firebus.interfaces.Consumer;
import io.firebus.logging.Logger;
import io.firebus.script.ScriptFactory;
import io.redback.client.ConfigClient;
import io.redback.client.DataClient;
import io.redback.client.FileClient;
import io.redback.client.GatewayClient;
import io.redback.client.GeoClient;
import io.redback.client.IntegrationClient;
import io.redback.client.NotificationClient;
import io.redback.client.ObjectClient;
import io.redback.client.ProcessClient;
import io.redback.client.ReportClient;
import io.redback.client.js.FileClientJSWrapper;
import io.redback.client.js.GatewayClientJSWrapper;
import io.redback.client.js.GeoClientJSWrapper;
import io.redback.client.js.IntegrationClientJSWrapper;
import io.redback.client.js.NotificationClientJSWrapper;
import io.redback.client.js.ObjectClientJSWrapper;
import io.redback.client.js.ProcessClientJSWrapper;
import io.redback.client.js.ReportClientJSWrapper;
import io.redback.exceptions.RedbackException;
import io.redback.managers.domainmanager.js.DomainLoggerJS;
import io.redback.managers.domainmanager.js.DomainManagerJSWrapper;
import io.redback.security.Session;
import io.redback.security.js.SessionJSWrapper;
import io.redback.utils.CollectionConfig;
import io.redback.utils.StringUtils;
import io.redback.utils.js.RedbackUtilsJSWrapper;

public class DomainManager implements Consumer {
	protected Firebus firebus;
	protected ScriptFactory scriptFactory;
	protected boolean includeLoaded;
	protected String configServiceName;
	protected String objectServiceName;
	protected String processServiceName;
	protected String dataServiceName;
	protected String fileServiceName;
	protected String notificationServiceName;
	protected String reportServiceName;
	protected String gatewayServiceName;
	protected String geoServiceName;
	protected String integrationServiceName;
	protected ConfigClient configClient;
	protected ObjectClient objectClient;
	protected ProcessClient processClient;
	protected DataClient dataClient;
	protected FileClient fileClient;
	protected NotificationClient notificationClient;
	protected ReportClient reportClient;
	protected GatewayClient gatewayClient;
	protected GeoClient geoClient;
	protected IntegrationClient integrationClient;
	protected CollectionConfig entryCollection;
	protected CollectionConfig logCollection;
	protected Map<String, DomainEntry> entries;

	public DomainManager(Firebus fb, DataMap config) throws RedbackException {
		try {
			firebus = fb;
			scriptFactory = new ScriptFactory();
			includeLoaded = false;
			configServiceName = config.getString("configservice");
			objectServiceName = config.getString("objectservice");
			processServiceName = config.getString("processservice");
			dataServiceName = config.getString("dataservice");
			fileServiceName = config.getString("fileservice");
			notificationServiceName = config.getString("notificationservice");
			reportServiceName = config.getString("reportservice");
			gatewayServiceName = config.getString("gatewayservice");
			geoServiceName = config.getString("geoservice");
			integrationServiceName = config.getString("integrationservice");
			configClient = new ConfigClient(firebus, configServiceName);
			objectClient = new ObjectClient(firebus, objectServiceName);
			processClient = new ProcessClient(firebus, processServiceName);
			dataClient = new DataClient(firebus, dataServiceName);
			fileClient = new FileClient(firebus, fileServiceName);
			notificationClient = new NotificationClient(firebus, notificationServiceName);
			reportClient = new ReportClient(firebus, reportServiceName);
			gatewayClient = new GatewayClient(firebus, gatewayServiceName);
			geoClient = new GeoClient(firebus, geoServiceName);
			integrationClient = new IntegrationClient(firebus, integrationServiceName);
			entryCollection = new CollectionConfig(config.getObject("entrycollection"), "rbdm_entry");
			logCollection = new CollectionConfig(config.getObject("logcollection"), "rbdm_log");
			entries = new HashMap<String, DomainEntry>();	
			firebus.registerConsumer("_rb_domain_cache_clear", this, 10);
			scriptFactory.setInRootScope("rbutils", new RedbackUtilsJSWrapper());
		} catch(Exception e) {
			throw new RedbackException("Error initialising Domain Manager", e);
		}
	}

	protected synchronized void loadIncludeScripts(Session session) throws RedbackException
	{
		if(includeLoaded == false) {
			DataMap result = configClient.listConfigs(session, "rbdm", "include");
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
						entry = new DomainFunction(this, scriptFactory, entryMap);
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
				throw new RedbackException("Exception getting domain entry '" + domain + "." + name + "'", e);
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
					entry = new DomainFunction(this, scriptFactory, entryMap);
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
			throw new RedbackException("Exception listing domain entries for '" + domain + "'", e);
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
					entry = new DomainFunction(this, scriptFactory, entryMap);
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
			throw new RedbackException("Exception listing all entries with name '" + name + "'", e);
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
			firebus.publish("_rb_domain_cache_clear", new Payload(key));
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
			Logger.severe("rb.domain.addlog", "Error adding log entry", e);
		}
	}
	
	public void consume(Payload payload) {
		try {
			DataMap key = payload.getDataMap();
			entries.remove(key.getString("domain") + "." + key.getString("name"));
			includeLoaded = false;
		} catch(Exception e) {
			Logger.severe("rb.domain.consume", "Error consuming cache control message", e);
		}
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
	
	public DataEntity getVariable(Session session, String domain, String name) throws RedbackException {
		DomainVariable dv = (DomainVariable)getDomainEntry(domain, name);
		if(dv != null)
			return dv.getVariable();
		else 
			return null;
	}

	public void putFunction(Session session, String domain, String name, String function) throws RedbackException {
		DataMap entryMap = new DataMap();
		entryMap.put("type", "variable");
		entryMap.put("domain", domain);
		entryMap.put("name", name);
		entryMap.put("roles", new DataList());
		entryMap.put("source", function);
		DomainFunction df = new DomainFunction(this, scriptFactory, entryMap);
		putEntry(domain, name, df);
	}
	
	public List<DomainFunctionInfo> listFunctions(Session session, String domain, String category) throws RedbackException {
		List<DomainEntry> list = null;
		if(domain != null) {
			list = listDomainEntriesInCategory(domain, category);
		} else {
			list = new ArrayList<DomainEntry>();
			for(String d: session.getUserProfile().getDomains())
				if(!d.equals("*"))
					list.addAll(listDomainEntriesInCategory(d, category));
		}
		
		List<DomainFunctionInfo> retList = new ArrayList<DomainFunctionInfo>();
		for(DomainEntry de: list) {
			if(de instanceof DomainFunction)
				retList.add(((DomainFunction)de).getInfo());
		}
		return retList;
	}
		
	protected Object execute(Session session, DomainFunction df, DataMap param) throws RedbackException {
		Object result = null;
		DomainLogger dl = new DomainLogger(session, this, df);
		DomainLoggerJS dljs = new DomainLoggerJS(dl);
		Map<String, Object> context = new HashMap<String, Object>();
		context.put("session", new SessionJSWrapper(session));
		context.put("oc", new ObjectClientJSWrapper(objectClient, session, df.getDomain()));
		context.put("pc", new ProcessClientJSWrapper(processClient, session, df.getDomain()));
		context.put("fc", new FileClientJSWrapper(fileClient, session));
		context.put("nc", new NotificationClientJSWrapper(notificationClient, session));
		context.put("rc", new ReportClientJSWrapper(reportClient, session, df.getDomain()));
		context.put("gc", new GatewayClientJSWrapper(gatewayClient));
		context.put("geo", new GeoClientJSWrapper(geoClient, session));
		context.put("ic", new IntegrationClientJSWrapper(integrationClient, session, df.getDomain()));
		context.put("param", param);
		context.put("dm", new DomainManagerJSWrapper(this, session, df.getDomain()));
		context.put("domain", df.getDomain());
		context.put("log", dljs);

		if(!includeLoaded)
			loadIncludeScripts(session);

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

	
	public Object executeFunction(Session session, String domain, String name, DataMap param, boolean async) throws RedbackException {
		Object result = null;
		DomainEntry de = getDomainEntry(domain, name);
		if(de != null && de instanceof DomainFunction) {
			DomainFunction df = (DomainFunction)de;
			result = execute(session, df, param);
		} 
		return result;
	}
	
	public void executeFunctionInAllDomains(Session session, String name, DataMap param) {
		try {
			if(!includeLoaded) 
				loadIncludeScripts(session);
			
			List<DomainEntry> functions = new ArrayList<DomainEntry>();
			List<DomainEntry> allFunctions = listAllEntriesWithName(name);
			if(session.getUserProfile().hasAllDomains()) {
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
					execute(session, df, param);
				}
			} 			
		} catch(Exception e) {
			Logger.severe("rb.domain.execute", "Error executing function", e);
		}
	}
	
	public void clearCache(Session session, String domain, String name) {
		DataMap key = new DataMap();
		key.put("domain", domain);
		key.put("name", name);
		firebus.publish("_rb_domain_cache_clear", new Payload(key));
	}
}
