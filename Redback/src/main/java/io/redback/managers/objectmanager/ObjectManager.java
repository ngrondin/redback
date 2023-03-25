package io.redback.managers.objectmanager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import io.firebus.Firebus;
import io.firebus.Payload;
import io.firebus.data.DataEntity;
import io.firebus.data.DataException;
import io.firebus.data.DataList;
import io.firebus.data.DataLiteral;
import io.firebus.data.DataMap;
import io.firebus.exceptions.FunctionErrorException;
import io.firebus.exceptions.FunctionTimeoutException;
import io.firebus.logging.Logger;
import io.firebus.script.Function;
import io.firebus.script.ScriptContext;
import io.firebus.script.ScriptFactory;
import io.firebus.script.exceptions.ScriptException;
import io.firebus.script.exceptions.ScriptValueException;
import io.redback.client.AccessManagementClient;
import io.redback.client.ConfigClient;
import io.redback.client.DataClient;
import io.redback.client.DataClient.DataTransaction;
import io.redback.client.DomainClient;
import io.redback.client.FileClient;
import io.redback.client.GeoClient;
import io.redback.client.IntegrationClient;
import io.redback.client.NotificationClient;
import io.redback.client.ProcessClient;
import io.redback.client.ReportClient;
import io.redback.client.js.DomainClientJSWrapper;
import io.redback.client.js.FileClientJSWrapper;
import io.redback.client.js.GeoClientJSWrapper;
import io.redback.client.js.NotificationClientJSWrapper;
import io.redback.client.js.ProcessClientJSWrapper;
import io.redback.client.js.ReportClientJSWrapper;
import io.redback.exceptions.RedbackException;
import io.redback.exceptions.RedbackResourceNotFoundException;
import io.redback.exceptions.RedbackUnauthorisedException;
import io.redback.managers.jsmanager.ExpressionMap;
import io.redback.managers.objectmanager.js.DomainScriptLogger;
import io.redback.managers.objectmanager.js.ObjectManagerJSWrapper;
import io.redback.security.Session;
import io.redback.security.SysUserManager;
import io.redback.security.js.SessionJSWrapper;
import io.redback.security.js.SessionRightsJSFunction;
import io.redback.security.js.UserProfileJSWrapper;
import io.redback.utils.Cache;
import io.redback.utils.CollectionConfig;
import io.redback.utils.ConfigCache;
import io.redback.utils.FunctionInfo;
import io.redback.utils.StringUtils;
import io.redback.utils.js.FirebusJSWrapper;
import io.redback.utils.js.LoggerJSFunction;
import io.redback.utils.js.RedbackUtilsJSWrapper;

public class ObjectManager
{
	protected String name;
	protected Firebus firebus;
	protected ScriptFactory scriptFactory;
	protected boolean loadAllOnInit;
	protected int preCompile;
	protected boolean includeLoaded;
	protected String configServiceName;
	protected String accessManagerServiceName;
	protected String dataServiceName;
	protected String idGeneratorServiceName;
	protected String processServiceName;
	protected String geoServiceName;
	protected String fileServiceName;
	protected String reportServiceName;
	protected String notificationServiceName;
	protected String domainServiceName;
	protected String integrationServiceName;
	protected String objectUpdateChannel;
	protected DataMap globalVariables;
	protected ConfigCache<ObjectConfig> objectConfigs;
	protected ConfigCache<ScriptConfig> globalScripts;
	protected List<ScriptConfig> includeScripts;
	protected HashMap<String, ExpressionMap> readRightsFilters;
	protected CollectionConfig traceCollection;
	protected CollectionConfig scriptLogCollection;
	protected HashMap<Long, List<RedbackObject>> transactions;
	protected AccessManagementClient accessManagementClient;
	protected DataClient dataClient;
	protected ConfigClient configClient;
	protected ProcessClient processClient;
	protected GeoClient geoClient;
	protected FileClient fileClient;
	protected ReportClient reportClient;
	protected NotificationClient notificationClient;
	protected DomainClient domainClient;
	protected IntegrationClient integrationClient;
	protected boolean useMultiDBTransactions;
	protected Cache<DataMap> searchCache;
	protected SysUserManager sysUserManager;

	public ObjectManager(String n, DataMap config, Firebus fb) throws RedbackException
	{
		try {
			name = n;
			firebus = fb;
			includeLoaded = false;
			loadAllOnInit = config.containsKey("loadalloninit") ? config.getBoolean("loadalloninit") : true;
			preCompile = config.containsKey("precompile") ? config.getNumber("precompile").intValue() : 0;
			scriptFactory = new ScriptFactory();
			configServiceName = config.getString("configservice");
			accessManagerServiceName = config.getString("accessmanagementservice");
			dataServiceName = config.getString("dataservice");
			idGeneratorServiceName = config.getString("idgeneratorservice");
			processServiceName = config.getString("processservice");
			geoServiceName = config.getString("geoservice");
			fileServiceName = config.getString("fileservice");
			reportServiceName = config.getString("reportservice");
			notificationServiceName = config.getString("notificationservice");
			domainServiceName = config.getString("domainservice");
			integrationServiceName = config.getString("integrationservice");
			objectUpdateChannel = config.getString("objectupdatechannel");
			globalVariables = config.getObject("globalvariables");
			traceCollection = config.containsKey("tracecollection") ? new CollectionConfig(config.getObject("tracecollection")) : null;
			scriptLogCollection = config.containsKey("scriptlogcollection") ? new CollectionConfig(config.getObject("scriptlogcollection")) : null;
			useMultiDBTransactions = config.containsKey("multidbtransactions") ? config.getBoolean("multidbtransactions") : false;
			accessManagementClient = new AccessManagementClient(firebus, accessManagerServiceName);
			dataClient = new DataClient(firebus, dataServiceName);
			configClient = new ConfigClient(firebus, configServiceName);
			processClient = new ProcessClient(firebus, processServiceName);
			geoClient = new GeoClient(firebus, geoServiceName);
			fileClient = new FileClient(firebus, fileServiceName);
			reportClient = new ReportClient(firebus, reportServiceName);
			notificationClient = new NotificationClient(firebus, notificationServiceName);
			domainClient = new DomainClient(firebus, domainServiceName);
			integrationClient = new IntegrationClient(firebus, integrationServiceName);
			ObjectManager om = this;
			objectConfigs = new ConfigCache<ObjectConfig>(configClient, "rbo", "object", new ConfigCache.ConfigFactory<ObjectConfig>() {
				public ObjectConfig createConfig(DataMap map) throws Exception {
					return new ObjectConfig(om, map);
				}});
			globalScripts = new ConfigCache<ScriptConfig>(configClient, "rbo", "script", new ConfigCache.ConfigFactory<ScriptConfig>() {
				public ScriptConfig createConfig(DataMap map) throws Exception {
					return new ScriptConfig(scriptFactory, map);
				}});
			readRightsFilters = new HashMap<String, ExpressionMap>();
			transactions = new HashMap<Long, List<RedbackObject>>();
			searchCache = new Cache<DataMap>(5000);	
			sysUserManager = new SysUserManager(accessManagementClient, config);
			scriptFactory.setGlobals(globalVariables);
			scriptFactory.setInRootScope("log", new LoggerJSFunction());
			scriptFactory.setInRootScope("rbutils", new RedbackUtilsJSWrapper());
		} catch(Exception e) {
			throw new RedbackException("Error initialising Object Manager", e);
		}
	}
	
	public Firebus getFirebus()
	{
		return firebus;
	}
	
	public ScriptFactory getScriptFactory()
	{
		return scriptFactory;
	}
	
	public DataClient getDataClient()
	{
		return dataClient;
	}
	
	public ProcessClient getProcessClient()
	{
		return processClient;
	}

	public GeoClient getGeoClient()
	{
		return geoClient;
	}
	
	public FileClient getFileClient()
	{
		return fileClient;
	}
	
	public ReportClient getReportClient()
	{
		return reportClient;
	}

	public NotificationClient getNotificationClient()
	{
		return notificationClient;
	}
	
	public DomainClient getDomainClient()
	{
		return domainClient;
	}
	
	public IntegrationClient getIntegrationClient()
	{
		return integrationClient;
	}

	
	public ScriptContext createScriptContext(Session session) throws RedbackException
	{
		ScriptContext context = getScriptFactory().createScriptContext();
		try {
			context.put("session", new SessionJSWrapper(session));
			context.put("userprofile", new UserProfileJSWrapper(session.getUserProfile()));
			context.put("firebus", new FirebusJSWrapper(firebus, session));
			context.put("om", new ObjectManagerJSWrapper(this, session));
			context.put("pm", new ProcessClientJSWrapper(getProcessClient(), session));
			context.put("pc", new ProcessClientJSWrapper(getProcessClient(), session));
			context.put("geo", new GeoClientJSWrapper(geoClient, session));
			context.put("fc", new FileClientJSWrapper(getFileClient(), session));
			context.put("rc", new ReportClientJSWrapper(getReportClient(), session));
			context.put("nc", new NotificationClientJSWrapper(getNotificationClient(), session));
			context.put("canRead", new SessionRightsJSFunction(session, "read"));
			context.put("canWrite", new SessionRightsJSFunction(session, "write"));
			context.put("canExecute", new SessionRightsJSFunction(session, "execute"));
		} catch(ScriptValueException e) {
			throw new RedbackException("Error creating script context", e);
		}
		return context;
	}
	
	public void refreshAllConfigs()
	{	
		includeLoaded = false;
		objectConfigs.clear();
		globalScripts.clear();
		readRightsFilters.clear();
		try {
			loadAllIncludeScripts(new Session());
		} catch(Exception e) {
			Logger.severe("rb.object.loadinclude", e);
		}
	}

	protected synchronized void loadAllIncludeScripts(Session session) throws RedbackException
	{
		if(includeLoaded == false) {
			DataMap result = configClient.listConfigs(session, "rbo", "include");
			DataList resultList = result.getList("result");
			for(int i = 0; i < resultList.size(); i++)
			{
				DataMap cfg = resultList.getObject(i);
				try {
					scriptFactory.executeInRootScope("include_" + cfg.getString("name"), cfg.getString("script"));
				} catch(ScriptException e) {
					throw new RedbackException("Error loading include scripts", e);
				}
			}
			includeLoaded = true;
		}
	}

	
	protected void addRelatedBulk(Session session, List<RedbackElement> elements) throws RedbackException, ScriptException
	{
		if(elements != null  && elements.size() > 0)
		{
			ObjectConfig objectConfig = elements.get(0).getObjectConfig();
			Iterator<String> it = objectConfig.getAttributeNames().iterator();
			while(it.hasNext())
			{
				AttributeConfig attributeConfig = objectConfig.getAttributeConfig(it.next());
				String attributeName = attributeConfig.getName();
				if(attributeConfig.hasRelatedObject())
				{
					RelatedObjectConfig relatedObjectConfig = attributeConfig.getRelatedObjectConfig();
					DataList orList = new DataList();
					for(int j = 0; j < elements.size(); j++)
					{
						RedbackElement element = elements.get(j);
						Value linkValue = element.get(attributeName);
						if(linkValue != null && !linkValue.isNull())
						{
							DataMap findFilter = element.getRelatedFindFilter(attributeName);
							if(!orList.contains(findFilter))
								orList.add(findFilter);
						}
					}
					if(orList.size() > 0)
					{
						DataMap relatedObjectFilter = new DataMap();
						relatedObjectFilter.put("$or", orList);
						List<RedbackObject> result = listObjects(session, relatedObjectConfig.getObjectName(), relatedObjectFilter, null, null, false, 0, 1000);
						
						String relatedObjectLinkAttributeName = relatedObjectConfig.getLinkAttributeName();
						for(int j = 0; j < elements.size(); j++)
						{
							RedbackElement element = elements.get(j);
							Value linkValue = element.get(attributeName);
							if(linkValue != null && !linkValue.isNull())
							{
								RedbackObject relatedObject = null;
								for(int k = 0; k < result.size() && relatedObject == null; k++)
								{
									RedbackObject resultObject = result.get(k);
									Value resultObjectLinkValue = resultObject.get(relatedObjectLinkAttributeName);
									if(linkValue != null  &&  linkValue.equalsIgnoreCase(resultObjectLinkValue))
										relatedObject = resultObject;
								}
								if(relatedObject == null) // Because of a broken link in the DB
								{
									//logger.info("Broken data link for object '" + objectConfig.getName() + (element instanceof RedbackObject ? ":" + ((RedbackObject)element).getUID().getString() : "") + "." + attributeName);
									//ObjectConfig zombieObjectConfig = getObjectConfig(session, relatedObjectConfig.getObjectName());
									ObjectConfig zombieObjectConfig = objectConfigs.get(session, relatedObjectConfig.getObjectName());
									String zombieDBKey = (relatedObjectLinkAttributeName.equals("uid") ? zombieObjectConfig.getUIDDBKey() : zombieObjectConfig.getAttributeConfig(relatedObjectLinkAttributeName).getDBKey());
									relatedObject = new RedbackObject(session, this, zombieObjectConfig, new DataMap(zombieDBKey, linkValue.getObject()));
								}
								element.setRelated(attributeName, relatedObject);							
							}
						}
					}
				}
			}
		}
	}

	public RedbackObject getObject(Session session, String objectName, String id) throws RedbackException
	{
		ObjectConfig objectConfig = objectConfigs.get(session, objectName);
		if(session.getUserProfile().canRead("rb.objects." + objectName) || session.getUserProfile().canRead("rb.accesscat." + objectConfig.getAccessCategory()))
		{
			RedbackObject object = getFromCurrentTransaction(objectName, id);
			if(object == null)
			{
				try
				{
					DataMap dbFilter = new DataMap("{\"" + objectConfig.getUIDDBKey() + "\":\"" + id +"\"}");
					if(objectConfig.getDomainDBKey() != null  &&  !session.getUserProfile().hasAllDomains())
						dbFilter.put(objectConfig.getDomainDBKey(), session.getUserProfile().getDBFilterDomainClause());
					DataMap dbResult = dataClient.getData(objectConfig.getCollection(), dbFilter, null);
					DataList dbResultList = dbResult.getList("result");
					if(dbResultList.size() > 0)
					{
						DataMap dbData = dbResultList.getObject(0);
						object = new RedbackObject(session, this, objectConfig, dbData);
						putInCurrentTransaction(object);
					}
				}
				catch(Exception e)
				{
					throw new RedbackException("Problem initiating object " + objectName, e);
				}		
			}
			if(object == null)
				throw new RedbackResourceNotFoundException("No " + objectName + " object exists with uid " + id);
			return object;
		}
		else
		{
			throw new RedbackUnauthorisedException("User does not have the right to read object " + objectName);
		}			
	}
	
	@SuppressWarnings("unchecked")
	public List<RedbackObject> listObjects(Session session, String objectName, DataMap filter, String searchText, DataMap sort, boolean addRelated, int page, int pageSize) throws RedbackException
	{
		ObjectConfig objectConfig = objectConfigs.get(session, objectName);
		if(session.getUserProfile().canRead("rb.objects." + objectName) || session.getUserProfile().canRead("rb.accesscat." + objectConfig.getAccessCategory()))
		{
			try
			{
				DataMap objectFilter = new DataMap();
				DataList objectFilterAndList = new DataList();
				if(filter != null)
					objectFilterAndList.add(filter);
				if(searchText != null  &&  searchText.length() > 0)
					objectFilterAndList.add(generateSearchFilter(session, objectName, searchText.trim()));
				DataMap rightsReadFilter = generateRightsReadFilter(session, objectName);
				if(rightsReadFilter != null)
					objectFilterAndList.add(rightsReadFilter);
				if(objectFilterAndList.size() == 0) 
					objectFilter = new DataMap();
				else if(objectFilterAndList.size() == 1)
					objectFilter = objectFilterAndList.getObject(0);
				else if(objectFilterAndList.size() > 1)
					objectFilter = new DataMap("$and", objectFilterAndList);
				List<RedbackObject> objectList = this.listFromCurrentTransaction(objectName, objectFilter);
				DataList dbResultList = null;
				if(objectConfig.isPersistent()) 
				{
					DataMap dbFilter = generateDBFilter(session, objectConfig, objectFilter);
					if(objectConfig.getDomainDBKey() != null  &&  !session.getUserProfile().hasAllDomains() && !objectFilter.containsKey("domain"))
						dbFilter.put(objectConfig.getDomainDBKey(), session.getUserProfile().getDBFilterDomainClause());
					DataMap dbSort = generateDBSort(session, objectConfig, sort);
					DataMap dbResult = dataClient.getData(objectConfig.getCollection(), dbFilter, dbSort, page, pageSize);
					dbResultList = dbResult.getList("result");
				} else {
					Function gs = objectConfig.getGenerationScript();
					if(gs != null) {
						ScriptContext context = session.getScriptContext().createChild();
						context.put("dc", new DomainClientJSWrapper(getDomainClient(), session, session.getUserProfile().getDefaultDomain()));
						context.put("filter", filter);
						context.put("sort", sort);
						context.put("search", searchText);
						context.put("action", "list");
						context.put("page", page);
						context.put("pageSize", pageSize);
						Object o = gs.call(context);
						if(o instanceof DataList)
							dbResultList = (DataList)o;
					}
				}
				
				if(dbResultList != null) 
				{
					for(int i = 0; i < dbResultList.size(); i++)
					{
						DataMap dbData = dbResultList.getObject(i);
						RedbackObject objectInTransaction = getFromCurrentTransaction(objectName, dbData.getString(objectConfig.getUIDDBKey()));
						if(objectInTransaction != null && !objectList.contains(objectInTransaction) && !objectInTransaction.isDeleted()) 
						{
							Logger.warning("rb.object.list.memorymiss", new DataMap("object", objectName, "uid", objectInTransaction.getUID().stringValue, "filter", objectFilter));
							objectList.add(objectInTransaction);
						}
						if(objectInTransaction == null)
						{
							objectInTransaction = new RedbackObject(session, this, objectConfig, dbData);
							putInCurrentTransaction(objectInTransaction);
							objectList.add(objectInTransaction);
						}
					}
					if(addRelated)
						addRelatedBulk(session, (List<RedbackElement>)(List<?>)objectList);
				}
				return objectList;			
			}
			catch(Exception e)
			{
				throw new RedbackException("Error getting object list", e);
			}
		}
		else
		{
			throw new RedbackUnauthorisedException("User does not have the right to read object " + objectName);
		}
	}
	
	public List<RedbackObject> listRelatedObjects(Session session, String objectName, String uid, String attributeName, DataMap filterData, String searchText, DataMap sort, boolean addRelated) throws RedbackException
	{
		return listRelatedObjects(session, objectName, uid, attributeName, filterData, searchText, sort, addRelated, 0, 50);
	}
	
	public List<RedbackObject> listRelatedObjects(Session session, String objectName, String uid, String attributeName, DataMap filterData, String searchText, DataMap sort, boolean addRelated, int page, int pageSize) throws RedbackException
	{
		RedbackObject object = getObject(session, objectName, uid);
		return object.getRelatedList(attributeName, filterData, searchText, sort, page, pageSize);
	}
	
	public RedbackObject updateObject(Session session, String objectName, String id, DataMap updateData) throws RedbackException
	{
		RedbackObject object = getObject(session, objectName, id);
		boolean isAutomated = session.getUserProfile().getUsername().equals(sysUserManager.getUsername());
		Iterator<String> it = updateData.keySet().iterator();
		while(it.hasNext())
		{
			String attributeName = it.next();
			object.put(attributeName, new Value(updateData.get(attributeName)), !isAutomated);
		}
		return object;
	}
	
	public RedbackObject createObject(Session session, String objectName, String uid, String domain, DataMap initialData) throws RedbackException
	{
		ObjectConfig objectConfig = objectConfigs.get(session, objectName);
		RedbackObject object = new RedbackObject(session, this, objectConfig, uid, domain);
		putInCurrentTransaction(object);
		if(initialData != null)
		{
			boolean isAutomated = session.getUserProfile().getUsername().equals(sysUserManager.getUsername());
			for(String attributeName: initialData.keySet())
			{
				boolean isFilter = false;
				Object value = initialData.get(attributeName);
				if(attributeName.startsWith("$")) 
					isFilter = true;
				else if(value instanceof DataMap)
					for(String key: ((DataMap)value).keySet())
						if(key.startsWith("$"))
							isFilter = true;
				if(!isFilter)
					object.put(attributeName, new Value(value), !isAutomated);
			}
			Logger.fine("rb.object.create", new DataMap("object", object.getObjectConfig().getName(), "uid", object.getUID().getString()));
		}
		return object;				
	}
	
	public void deleteObject(Session session, String objectName, String uid) throws RedbackException
	{
		RedbackObject object = getObject(session, objectName, uid);
		if(object != null)
			object.delete();
	}	
	
	public RedbackObject executeObjectFunction(Session session, String objectName, String id, String function, DataMap param) throws RedbackException
	{
		RedbackObject object = getObject(session, objectName, id);
		object.execute(function);
		return object;
	}
	
	public Object executeFunction(Session session, String function, DataMap param) throws RedbackException
	{
		Object ret = null;
		ScriptConfig scriptCfg = globalScripts.get(session, function);
		if(session.getUserProfile().canExecute("rb.scripts." + function) || session.getUserProfile().canExecute("rb.accesscat." + scriptCfg.getAccessCategory())) {
			DomainScriptLogger domainScriptLogger = scriptCfg.getDomain() != null ? new DomainScriptLogger(dataClient, scriptLogCollection, session, scriptCfg.getDomain(), scriptCfg.getName(), "info") : null;	
			try {
				ScriptContext context = session.getScriptContext().createChild();
				context.put("param", param);
				if(domainScriptLogger != null)
					context.put("log", domainScriptLogger);
				ret = scriptCfg.execute(context);
				return ret;
			} catch(Exception e) {
				if(domainScriptLogger != null)
					domainScriptLogger.log(StringUtils.rollUpExceptions(e));
				throw new RedbackException("Error in script " + function, e);
			} finally {
				if(domainScriptLogger != null)
					domainScriptLogger.commit();
			}
		} else {
			throw new RedbackUnauthorisedException("No rights to execute script " + function);
		}
	}	
	
	public List<FunctionInfo> listFunctions(Session session, String category) throws RedbackException {
		List<FunctionInfo> retList = new ArrayList<FunctionInfo>();
		List<ScriptConfig> list = globalScripts.list(session, new DataMap("category", category));
		for(ScriptConfig sc: list) {
			if(session.getUserProfile().canExecute("rb.scripts." + sc.getName()) || session.getUserProfile().canExecute("rb.accesscat." + sc.getAccessCategory())) {
				retList.add(new FunctionInfo(sc.getName(), sc.getDescription(), sc.getTimeout()));
			}
		}
		return retList;
	}
	
	public void fork(Session session, String objectName, String id, String function) throws RedbackException
	{
		DataMap request = new DataMap();
		request.put("action", "execute");
		request.put("object", objectName);
		request.put("uid", id);
		request.put("function", function);
		Payload reqP = new Payload(request);
		reqP.metadata.put("session", session.id);
		reqP.metadata.put("token", session.token);
		if(session.getTimezone() != null)
			reqP.metadata.put("timezone", session.getTimezone());
		try {
			firebus.requestServiceAndForget(name, reqP);
		} catch(Exception e) {
			throw new RedbackException("Error forking execution");
		}
	}
	
	public void elevateSession(Session session) throws RedbackException {
		session.setElevatedUserProfile(sysUserManager.getProfile(session));
	}
	
	public void demoteSession(Session session) throws RedbackException {
		session.setElevatedUserProfile(null);
	}
	
	
	@SuppressWarnings("unchecked")
	public List<RedbackAggregate> aggregateObjects(Session session, String objectName, DataMap filter, String searchText, DataList tuple, DataList metrics, DataMap sort, DataList base, boolean addRelated, int page, int pageSize) throws RedbackException
	{
		List<RedbackAggregate> list = new ArrayList<RedbackAggregate>();
		ObjectConfig objectConfig = objectConfigs.get(session, objectName);
		if(session.getUserProfile().canRead("rb.objects." + objectName) || session.getUserProfile().canRead("rb.accesscat." + objectConfig.getAccessCategory()))
		{
			try
			{
				DataMap objectFilter = new DataMap();
				if(filter != null)
					objectFilter.merge(filter);
				if(searchText != null  &&  searchText.length() > 0)
					objectFilter.merge(generateSearchFilter(session, objectName, searchText.trim()));
				if(base != null)
					objectFilter.merge(new DataMap("$or", base));
				DataList dbResultList = null;
				
				if(objectConfig.isPersistent()) 
				{
					DataMap dbFilter = generateDBFilter(session, objectConfig, objectFilter);
					if(objectConfig.getDomainDBKey() != null  &&  !session.getUserProfile().hasAllDomains())
						dbFilter.put(objectConfig.getDomainDBKey(), session.getUserProfile().getDBFilterDomainClause());
					DataList dbTuple = new DataList();
					for(int i = 0; i < tuple.size(); i++) {
						if(tuple.get(i) instanceof DataMap) {
							DataMap tupleItem = (DataMap)tuple.getObject(i).getCopy();
							tupleItem.put("attribute", objectConfig.getAttributeConfig(tupleItem.getString("attribute")).getDBKey());
							dbTuple.add(tupleItem);
						} else {
							dbTuple.add(objectConfig.getAttributeConfig(tuple.getString(i)).getDBKey());
						}
					}
					DataList dbMetrics = new DataList();
					for(int i = 0; i < metrics.size(); i++)
					{
						DataMap metric = metrics.getObject(i);
						String metricName = metric.getString("name");
						if(metricName != null && objectConfig.getAttributeConfig(metricName) == null) {
							String function = metric.getString("function");
							if(function.equals("count") || function.equals("sum") || function.equals("max") || function.equals("min"))
							{
								DataMap dbMetric = new DataMap();
								dbMetric.put("function", function);
								if(!function.equals("count"))
								{
									String attribute = metric.getString("attribute");
									if(attribute != null)
										dbMetric.put("field", objectConfig.getAttributeConfig(attribute).getDBKey());
								}
								dbMetric.put("name", metricName);
								dbMetrics.add(metric);
							} 
							else
							{
								throw new RedbackException("The metric function hasn't been provided");
							}
						} 
						else 
						{
							throw new RedbackException("A metric cannot have the same name as one of the object's attribute");
						}
					}
					DataMap dbSort = generateDBSort(session, objectConfig, sort);
					DataMap dbResult = dataClient.aggregateData(objectConfig.getCollection(), dbFilter, dbTuple, dbMetrics, dbSort, page, pageSize);
					dbResultList = dbResult.getList("result");
				} 
				else 
				{
					Function gs = objectConfig.getGenerationScript();
					if(gs != null) {
						ScriptContext context = session.getScriptContext().createChild();
						context.put("filter", filter);
						context.put("tuple", tuple);
						context.put("metrics", metrics);
						context.put("action", "aggregate");
						Object o = gs.call(context);
						if(o instanceof DataList)
							dbResultList = (DataList)o;
						else
							dbResultList = new DataList();
					}
				}		
				
				if(base != null) {
					DataList baseDb = new DataList();
					for(int i = 0; i < base.size(); i++) {
						DataMap baseItem = base.getObject(i);
						DataMap baseDbItem = new DataMap();
						for(String baseAttr: baseItem.keySet()) {
							String baseDbAttr = objectConfig.getAttributeConfig(baseAttr).getDBKey();
							baseDbItem.put(baseDbAttr, baseItem.getString(baseAttr));
						}
						DataMap dbDataMatch = null;
						for(int j = 0; j < dbResultList.size(); j++) {
							DataMap dbData = dbResultList.getObject(j);
							boolean match = true;
							for(String baseDbAttr: baseDbItem.keySet()) {
								if(!baseDbItem.getString(baseDbAttr).equals(dbData.getString(baseDbAttr)))
									match = false;
							}
							if(match) {
								dbDataMatch = dbData;
								break;
							}
						}
						
						for(int j = 0; j < metrics.size(); j++)
						{
							DataMap metric = metrics.getObject(j);
							String metricName = metric.getString("name");
							baseDbItem.put(metricName, dbDataMatch != null ? dbDataMatch.getNumber(metricName) : 0);
						}
						baseDb.add(baseDbItem);
					}
					dbResultList = baseDb;
				}
									
				for(int i = 0; i < dbResultList.size(); i++)
				{
					DataMap dbData = dbResultList.getObject(i);
					RedbackAggregate aggregate = new RedbackAggregate(session, this, objectConfig, dbData);
					list.add(aggregate);
				}
				
				if(addRelated)
					addRelatedBulk(session, (List<RedbackElement>)(List<?>)list);
				return list;	
			}
			catch(Exception e)
			{
				throw new RedbackException("Error aggregating objects", e);
			}
		}
		else
		{
			throw new RedbackException("No object config is available for '" + objectName + "'");	
		}
	}
	
	public Value getNewID(Session session, String name) throws FunctionErrorException, FunctionTimeoutException
	{
		Payload request = new Payload(name);
		request.metadata.put("mime", "text/plain");
		request.metadata.put("session", session.getId());
		Payload response = firebus.requestService(idGeneratorServiceName, request); 
		String value = response.getString();
		return new Value(value);
	}
	
	public void initiateCurrentTransaction(Session session)  throws RedbackException
	{
		long txId = Thread.currentThread().getId();
		synchronized(transactions)
		{
			transactions.put(txId, new ArrayList<RedbackObject>());
		}
		session.setScriptContext(createScriptContext(session));
	}
	
	protected RedbackObject getFromCurrentTransaction(String objectName, String uid)
	{
		long txId = Thread.currentThread().getId();
		if(transactions.containsKey(txId))
		{
			List<RedbackObject> list = transactions.get(txId);
			for(RedbackObject object: list) {
				if(object.getObjectConfig().getName().equals(objectName) && object.getUID().getString().equals(uid))
					return object;
			}
		}
		return null;
	}

	protected List<RedbackObject> listFromCurrentTransaction(String objectName, DataMap objectFilter) throws RedbackException
	{
		List<RedbackObject> list = new ArrayList<RedbackObject>();
		long txId = Thread.currentThread().getId();
		if(transactions.containsKey(txId))
		{
			List<RedbackObject> objectList = transactions.get(txId);
			if(objectList.size() > 0) {
				for(RedbackObject rbo: objectList) {
					if(rbo.getObjectConfig().getName().equals(objectName)) {
						if(rbo.filterApplies(objectFilter) && !rbo.isDeleted())
							list.add(rbo);
					}
				}
			}
		}
		return list;
	}
	
	protected void putInCurrentTransaction(RedbackObject obj)
	{
		long txId = Thread.currentThread().getId();
		synchronized(transactions)
		{
			if(!transactions.containsKey(txId))
				transactions.put(txId, new ArrayList<RedbackObject>());
		}
		transactions.get(txId).add(obj);
	}
	
	public void commitCurrentTransaction(Session session) throws RedbackException
	{
		long txId = Thread.currentThread().getId();
		if(transactions.containsKey(txId))
		{
			List<RedbackObject> list = new ArrayList<RedbackObject>();
			synchronized(transactions)
			{
				List<RedbackObject> txObjects = transactions.get(txId);
				for(RedbackObject object: txObjects)
					list.add(object);
			}
			
			List<DataTransaction> txs = new ArrayList<DataTransaction>();
			for(RedbackObject object: list) {
				if(object.isDeleted) {
					txs.add(object.getDBDeleteTransaction());
				} else if(object.isUpdated()) {
					object.onSave();
					txs.add(object.getDBUpdateTransaction());
					List<DataTransaction> traceTxs = object.getDBTraceTransactions();
					if(traceTxs != null) 
						for(DataTransaction ttx: traceTxs)
							txs.add(ttx);
				}
			}
			
			if(txs.size() > 0) {
				if(this.useMultiDBTransactions) {
					dataClient.multi(txs);
				} else {
					for(DataTransaction tx: txs) {
						dataClient.runTransaction(tx);
					}
				}
			}
			
			for(RedbackObject object: list) {
				if(object.isDeleted) {
					object.afterDelete();
				} else if(object.isUpdated()) {
					object.afterSave();
					signal(object);
				}
			}
							
			synchronized(transactions)
			{
				transactions.remove(txId);
			}
		}		
	}
	
	public DataMap generateRightsReadFilter(Session session, String objectName) throws RedbackException 
	{
		DataMap filter = session.getUserProfile().getReadFilter("rb.objects." + objectName);
		if(filter != null) {
			String s = filter.toString(0, true);
			ExpressionMap em = readRightsFilters.get(s);
			if(em == null) {
				String funcName = objectName + "_readrightsfilter_" + StringUtils.base16(filter.hashCode());
				em = new ExpressionMap(scriptFactory, funcName, filter);
				readRightsFilters.put(s, em);
			}
			return em.eval(createScriptContext(session));
		} else {
			return null;
		}
	}
	
	public DataMap generateDBFilter(Session session, ObjectConfig objectConfig, DataMap objectFilter) throws DataException, FunctionErrorException, RedbackException
	{
		DataMap dbFilter = new DataMap();
		Iterator<String> it = objectFilter.keySet().iterator();
		while(it.hasNext())
		{
			String key = it.next();
			if(key.equals("$eq")  ||  key.equals("$gt")  ||  key.equals("$gte")  ||  key.equals("$lt")  ||  key.equals("$lte")  ||  key.equals("$ne") || key.equals("$regex"))
			{
				dbFilter.put(key, objectFilter.get(key));
			}
			else if(key.equals("$in")  ||  key.equals("$nin"))
			{
				DataList list = objectFilter.getList(key);
				if(list == null) list = new DataList();
				dbFilter.put(key, list);
			}
			else if(key.equals("$or") || key.equals("$and"))
			{
				DataList list = objectFilter.getList(key);
				DataList dbList = new DataList();
				for(int i = 0; i < list.size(); i++)
				{
					dbList.add(generateDBFilter(session, objectConfig, list.getObject(i)));
				}
				dbFilter.put(key, dbList);
			}
			else if(key.contains(".")) 
			{
				String rootAttribute = key.substring(0, key.indexOf("."));
				String remainder = key.substring(key.indexOf(".") + 1);
				AttributeConfig attributeConfig = objectConfig.getAttributeConfig(rootAttribute);
				if(attributeConfig.hasRelatedObject())
				{
					DataList dbList = new DataList();
					RelatedObjectConfig roc = attributeConfig.getRelatedObjectConfig();
					ObjectConfig nextObjectConfig = objectConfigs.get(session, roc.getObjectName());
					List<RedbackObject> list = listObjects(session, nextObjectConfig.getName(), new DataMap(remainder, objectFilter.get(key)), null, null, false, 0, 1000);
					if(list.size() > 0) {
						for(int k = 0; k < list.size(); k++)
						{
							RedbackObject resultObject = list.get(k);
							Value resultObjectLinkValue = resultObject.get(roc.getLinkAttributeName());
							dbList.add(resultObjectLinkValue.getObject());
						}
						dbFilter.put(rootAttribute, new DataMap("$in", dbList));
					} else {
						dbFilter.put(rootAttribute, "");
					}
				}
			}
			else
			{
				String attributeDBKey = null; 
				AttributeConfig attributeConfig = objectConfig.getAttributeConfig(key);
				if(key.equals("uid"))
					attributeDBKey = objectConfig.getUIDDBKey();
				else if(key.equals("domain"))
					attributeDBKey = objectConfig.getDomainDBKey();
				else if(attributeConfig != null)
					attributeDBKey = attributeConfig.getDBKey();
				
				if(attributeDBKey != null)
				{
					DataEntity objectFilterValue = objectFilter.get(key);
					DataEntity dbFilterValue = null;
					if(objectFilterValue instanceof DataMap)
					{
						dbFilterValue = generateDBFilter(session, objectConfig, (DataMap)objectFilterValue);
					}
					else if(objectFilterValue instanceof DataLiteral)
					{
						String objectFilterValueString = ((DataLiteral)objectFilterValue).getString();
						if(objectFilterValueString != null  &&  objectFilterValueString.startsWith("*")  &&  objectFilterValueString.endsWith("*")  &&  objectFilterValueString.length() >= 2)
							dbFilterValue =  new DataMap("$regex",  objectFilterValueString.substring(1, objectFilterValueString.length() - 1));
						else
							dbFilterValue = ((DataLiteral)objectFilterValue).getCopy();
					}
					dbFilter.put(attributeDBKey, dbFilterValue);
				}
			}
		}

		return dbFilter;
	}
	
	public DataMap generateDBSort(Session session, ObjectConfig objectConfig, DataMap objectSort) throws DataException, FunctionErrorException, RedbackException
	{
		DataMap dbSort = null;
		if(objectSort != null) {
			dbSort = new DataMap();
			for(int i = 0; objectSort.containsKey(String.valueOf(i)); i++) {
				DataMap sortItem = new DataMap();
				String attribute = objectSort.getObject("" + i).getString("attribute");
				String dbKey = null;
				if(attribute.equals("uid"))
					dbKey = objectConfig.getUIDDBKey();
				else if(attribute.equals("domain"))
					dbKey = objectConfig.getDomainDBKey();
				else
					dbKey = objectConfig.getAttributeConfig(attribute).getDBKey(); 
				if(dbKey != null) {
					sortItem.put("attribute", dbKey);
					sortItem.put("dir", objectSort.getObject("" + i).getString("dir"));
					dbSort.put("" + i, sortItem);
				}
			}
		}
		return dbSort;
	}
	
	protected DataMap generateSearchFilter(Session session, String objectName, String searchText) throws RedbackException
	{
		String key = session.getUserProfile().getUsername() + ":" + objectName + ":" + searchText;
		DataMap filter = searchCache.get(key);
		if(filter == null) {
			String regexExpr = "(?i)" + StringUtils.escapeRegex(searchText) + "";
			filter = new DataMap();
			DataList orList = new DataList();
			ObjectConfig config = objectConfigs.get(session, objectName);
			Iterator<String> it = config.getAttributeNames().iterator();
			while(it.hasNext())
			{
				AttributeConfig attributeConfig = config.getAttributeConfig(it.next());
				if(attributeConfig.getDBKey() != null && attributeConfig.canBeSearched())
				{
					RelatedObjectConfig roc = attributeConfig.getRelatedObjectConfig();
					if(roc == null)
					{
						DataMap orTerm = new DataMap();
						orTerm.put(attributeConfig.getName(), new DataMap("$regex", regexExpr));
						orList.add(orTerm);
					}
					else
					{
						String relatedObjectName = roc.getObjectName();
						ObjectConfig relatedConfig = objectConfigs.get(session, relatedObjectName);
						if(relatedConfig != null)
						{
							DataList relatedOrList = new DataList();
							Iterator<String> it2 = relatedConfig.getAttributeNames().iterator();
							while(it2.hasNext())
							{
								AttributeConfig relatedAttributeConfig = relatedConfig.getAttributeConfig(it2.next());
								if(relatedAttributeConfig.getDBKey() != null  &&  !relatedAttributeConfig.hasRelatedObject() && relatedAttributeConfig.canBeSearched())
								{
									DataMap orTerm = new DataMap();
									orTerm.put(relatedAttributeConfig.getName(), new DataMap("$regex", regexExpr));
									relatedOrList.add(orTerm);
								}
							}
							if(relatedOrList.size() > 0)
							{
								DataMap relatedFilter = new DataMap("$or", relatedOrList);
								List<RedbackObject> result = listObjects(session, relatedObjectName, relatedFilter, null, null, false, 0, 1000);
								if(result.size() > 0)
								{
									DataMap orTerm = new DataMap();
									DataList inList = new DataList();
									for(int k = 0; k < result.size(); k++)
									{
										RedbackObject resultObject = result.get(k);
										Value resultObjectLinkValue = resultObject.get(roc.getLinkAttributeName());
										inList.add(resultObjectLinkValue.getObject());
									}
									orTerm.put(attributeConfig.getName(), new DataMap("$in", inList));
									orList.add(orTerm);
								}
							}
						}
					}
				}
			}
			if(orList.size() == 1)
				filter = orList.getObject(0);
			if(orList.size() > 1)
				filter.put("$or", orList);
			searchCache.put(key, filter);
		} 
		return filter;
	}

	
	protected void signal(RedbackObject object)
	{
		if(objectUpdateChannel != null) 
		{
			try 
			{
				Payload payload = new Payload(object.getDataMap(true, true));
				firebus.publish(objectUpdateChannel, payload);
			}
			catch(Exception e) 
			{
				Logger.severe("rb.object.signal.error", e);
			}
		}		
	}

}

