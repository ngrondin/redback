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
import io.redback.client.QueueClient;
import io.redback.client.ReportClient;
import io.redback.client.js.DomainClientJSWrapper;
import io.redback.client.js.FileClientJSWrapper;
import io.redback.client.js.GeoClientJSWrapper;
import io.redback.client.js.IntegrationClientJSWrapper;
import io.redback.client.js.NotificationClientJSWrapper;
import io.redback.client.js.ProcessClientJSWrapper;
import io.redback.client.js.QueueClientJSWrapper;
import io.redback.client.js.ReportClientJSWrapper;
import io.redback.exceptions.RedbackException;
import io.redback.exceptions.RedbackResourceNotFoundException;
import io.redback.exceptions.RedbackUnauthorisedException;
import io.redback.managers.jsmanager.ExpressionMap;
import io.redback.managers.objectmanager.js.DomainScriptLogger;
import io.redback.managers.objectmanager.js.ObjectManagerJSWrapper;
import io.redback.managers.objectmanager.js.PackStreamJSWrapper;
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
import io.redback.utils.TxStore;
import io.redback.utils.js.FirebusJSWrapper;
import io.redback.utils.js.LoggerJSFunction;
import io.redback.utils.js.RedbackUtilsJSWrapper;
import io.redback.utils.stream.Converter;
import io.redback.utils.stream.ConverterStreamPipeline;
import io.redback.utils.stream.DataStream;
import io.redback.utils.stream.MultiStreamPipeline;
import io.redback.utils.stream.StaticStreamSource;

public class ObjectManager
{
	protected String name;
	protected Firebus firebus;
	protected ScriptFactory scriptFactory = new ScriptFactory();
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
	protected String queueServiceName;
	protected String objectUpdateChannel;
	protected DataMap globalVariables;
	protected ConfigCache<ObjectConfig> objectConfigs;
	protected ConfigCache<ScriptConfig> globalScripts;
	protected ConfigCache<PackConfig> packConfigs;
	protected HashMap<String, ExpressionMap> readRightsFilters = new HashMap<String, ExpressionMap>();
	protected CollectionConfig traceCollection;
	protected CollectionConfig scriptLogCollection;
	protected AccessManagementClient accessManagementClient;
	protected DataClient dataClient;
	protected ConfigClient configClient;
	protected ProcessClient processClient;
	protected GeoClient geoClient;
	protected FileClient fileClient;
	protected ReportClient reportClient;
	protected NotificationClient notificationClient;
	protected QueueClient queueClient;
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
			queueServiceName = config.getString("queueservice");
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
			queueClient = new QueueClient(firebus, queueServiceName);
			integrationClient = new IntegrationClient(firebus, integrationServiceName);
			ObjectManager om = this;
			objectConfigs = new ConfigCache<ObjectConfig>(configClient, "rbo", "object", 3600000, new ConfigCache.ConfigFactory<ObjectConfig>() {
				public ObjectConfig createConfig(DataMap map) throws Exception {
					return new ObjectConfig(om, map);
				}});
			globalScripts = new ConfigCache<ScriptConfig>(configClient, "rbo", "script", 3600000, new ConfigCache.ConfigFactory<ScriptConfig>() {
				public ScriptConfig createConfig(DataMap map) throws Exception {
					return new ScriptConfig(scriptFactory, map);
				}});
			packConfigs = new ConfigCache<PackConfig>(configClient, "rbo", "pack", 3600000, new ConfigCache.ConfigFactory<PackConfig>() {
				public PackConfig createConfig(DataMap map) throws Exception {
					return new PackConfig(om, map);
				}});			
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
	
	public QueueClient getQueueClient()
	{
		return queueClient;
	}
	
	public SysUserManager getSysUserManager() {
		return sysUserManager;
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
			context.put("qc", new QueueClientJSWrapper(getQueueClient(), session));
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
		packConfigs.clear();
		readRightsFilters.clear();
		try {
			loadAllIncludeScripts(new Session());
		} catch(Exception e) {
			Logger.severe("rb.object.loadinclude", e);
		}
	}
	
	public void clearDomainObjectConfig(Session session, String object, String domain) throws RedbackException {
		objectConfigs.clear(object, domain);
	}
	
	public void clearDomainScriptConfig(Session session, String object, String domain) throws RedbackException {
		globalScripts.clear(object, domain);
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

	
	protected void addRelatedBulk(Session session, List<RedbackElement> elements) throws RedbackException
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
							Value domainValue = element.get("domain");
							if(linkValue != null && !linkValue.isNull())
							{
								RedbackObject relatedObject = null;
								int selectionPoints = 0;
								for(RedbackObject resultObject: result) {
									Value resultObjectLinkValue = resultObject.get(relatedObjectLinkAttributeName);
									if(linkValue != null  &&  linkValue.equalsIgnoreCase(resultObjectLinkValue)) {
										int points = resultObject.getDomain().equals(domainValue) ? 3 : resultObject.getDomain().equals("root") ? 1 : 2;
										if(points > selectionPoints) {
											selectionPoints = points;
											relatedObject = resultObject;
										}										
									}
								}
								if(relatedObject == null) // Because of a broken link in the DB
								{
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
	
	protected ObjectConfig getConfigIfCanRead(Session session, String objectName) throws RedbackException 
	{
		return getConfigIfCanRead(session, objectName, true);
	}
	
	protected ObjectConfig getConfigIfCanRead(Session session, String objectName, boolean errorIfCantRead) throws RedbackException 
	{
		ObjectConfig objectConfig = objectConfigs.get(session, objectName);
		if(session.getUserProfile().canRead("rb.objects." + objectName) || session.getUserProfile().canRead("rb.accesscat." + objectConfig.getAccessCategory())) {
			return objectConfig;
		} else if(errorIfCantRead) {
			throw new RedbackUnauthorisedException("User does not have the right to read object " + objectName);
		} else {
			return null;
		}
	}

	public RedbackObject getObject(Session session, String objectName, String id) throws RedbackException
	{
		ObjectConfig objectConfig = getConfigIfCanRead(session, objectName);
		String key = objectName + ":" + id;
		RedbackObject object = session.hasTxStore() ? (RedbackObject)session.getTxStore().get(key) : null;
		if(object == null)
		{
			try
			{
				DataMap dbFilter = generateDBFilter(session, objectConfig, new DataMap("uid", id));
				DataMap dbResult = dataClient.getData(objectConfig.getCollection(), dbFilter, null);
				DataList dbResultList = dbResult.getList("result");
				if(dbResultList.size() > 0)
					object = new RedbackObject(session, this, objectConfig, dbResultList.getObject(0));
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
	
	public RedbackObject getRelatedObject(Session session, String objectName, String id, String attribute) throws RedbackException 
	{
		RedbackObject baseObject = getObject(session, objectName, id);
		return baseObject.getRelated(attribute);
	}
	
	@SuppressWarnings("unchecked")
	public List<RedbackObject> listObjects(Session session, String objectName, DataMap filter, String searchText, DataMap sort, boolean addRelated, int page, int pageSize) throws RedbackException
	{
		ObjectConfig objectConfig = getConfigIfCanRead(session, objectName);
		try
		{
			DataMap objectFilter = generateObjectFilter(session, objectName, filter, searchText);
			DataList dbResultList = null;
			if(objectConfig.isPersistent()) 
			{
				DataMap dbFilter = generateDBFilter(session, objectConfig, objectFilter);
				DataMap dbSort = generateDBSort(session, objectConfig, sort);
				DataMap dbResult = dataClient.getData(objectConfig.getCollection(), dbFilter, dbSort, page, pageSize);
				dbResultList = dbResult.getList("result");
			} else {
				dbResultList = generateNonPersistentObjectData(session, objectConfig, filter, searchText, sort, page, pageSize);
			}
			List<RedbackObject> objectList = convertDBDataToObjects(session, objectConfig, objectFilter, dbResultList, page == 0);
			if(addRelated) addRelatedBulk(session, (List<RedbackElement>)(List<?>)objectList);
			return objectList;			
		}
		catch(Exception e)
		{
			throw new RedbackException("Error getting object list", e);
		}
	}

	public void streamObjects(Session session, String objectName, DataMap filter, String searchText, DataMap sort, int chunkSize, int advance, DataStream<RedbackObject> objectStream) throws RedbackException 
	{
		ObjectConfig objectConfig = getConfigIfCanRead(session, objectName);
		try
		{
			DataMap objectFilter = generateObjectFilter(session, objectName, filter, searchText);
			MultiStreamPipeline<RedbackObject> msp = new MultiStreamPipeline<RedbackObject>(objectStream, 2);
			new StaticStreamSource<RedbackObject>(msp.getSourceDataStream(0), getNewOrUpdatedObjects(session, objectConfig, filter));
			ConverterStreamPipeline<RedbackObject, DataMap> csp = new ConverterStreamPipeline<RedbackObject, DataMap>(msp.getSourceDataStream(1), new Converter<RedbackObject, DataMap>() {
				public RedbackObject convert(DataMap data) throws DataException, RedbackException {
					return convertDBDataToObjectIfStillApplies(session, objectConfig, filter, data);
				}});
			if(objectConfig.isPersistent()) 
			{
				DataMap dbFilter = generateDBFilter(session, objectConfig, objectFilter);
				DataMap dbSort = generateDBSort(session, objectConfig, sort);
				dataClient.streamData(objectConfig.getCollection(), dbFilter, dbSort, chunkSize, advance, csp.getSourceDataStream());
			} else { // Non persistent objects
				DataList dbResultList = generateNonPersistentObjectData(session, objectConfig, filter, searchText, sort, 0, 5000);
				new StaticStreamSource<DataMap>(csp.getSourceDataStream(), convertDataListToList(dbResultList));
			}
		}
		catch(Exception e)
		{
			throw new RedbackException("Error getting object list", e);
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
		Iterator<String> it = updateData.keySet().iterator();
		while(it.hasNext())
		{
			String attributeName = it.next();
			object.put(attributeName, new Value(updateData.get(attributeName)));
		}
		return object;
	}
	
	public RedbackObject createObject(Session session, String objectName, String uid, String domain, DataMap initialData) throws RedbackException
	{
		ObjectConfig objectConfig = objectConfigs.get(session, objectName);
		RedbackObject object = new RedbackObject(session, this, objectConfig, uid, domain);
		if(initialData != null)
		{
			session.pushScriptLevel();
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
					object.put(attributeName, new Value(value));
			}
			session.popScriptLevel();
			Logger.fine("rb.object.create", new DataMap("object", object.getObjectConfig().getName(), "uid", object.getUID().getString()));
		}
		return object;				
	}
	
	public void deleteObject(Session session, String objectName, String uid) throws RedbackException
	{
		RedbackObject object = getObject(session, objectName, uid);
		if(object != null) {
			object.delete();
		}
	}	
	
	public long countObjects(Session session, String objectName, DataMap filter, String searchText) throws RedbackException
	{
		ObjectConfig objectConfig = getConfigIfCanRead(session, objectName);
		try
		{
			DataMap objectFilter = generateObjectFilter(session, objectName, filter, searchText);
			if(objectConfig.isPersistent()) 
			{
				DataMap dbFilter = generateDBFilter(session, objectConfig, objectFilter);
				long count = dataClient.countData(objectConfig.getCollection(), dbFilter);
				return count;
			} else {
				throw new RedbackException("Cannot count non-persistent objects '" + objectName + "'");
			}
		}
		catch(Exception e)
		{
			throw new RedbackException("Error getting object list", e);
		}
	}
	
	public RedbackObject executeObjectFunction(Session session, String objectName, String id, String function, DataMap param) throws RedbackException
	{
		RedbackObject object = getObject(session, objectName, id);
		object.executeFunction(function, param);
		return object;
	}
	
	public Object executeFunction(Session session, String functionName, DataMap param) throws RedbackException
	{
		Object ret = null;
		ScriptConfig scriptCfg = globalScripts.get(session, functionName, false);
		if(scriptCfg != null) {
			if(session.getUserProfile().canExecute("rb.scripts." + functionName) || session.getUserProfile().canExecute("rb.accesscat." + scriptCfg.getAccessCategory())) {
				DomainScriptLogger domainScriptLogger = null;	
				try {
					ScriptContext context = session.getScriptContext().createChild();
					context.put("param", param);
					if(scriptCfg.getDomain() != null) {
						session.pushDomainLock(scriptCfg.getDomain());
						domainScriptLogger = new DomainScriptLogger(dataClient, scriptLogCollection, session, scriptCfg.getDomain(), scriptCfg.getName(), "info");
						context.declare("log", domainScriptLogger);
						context.put("dc", new DomainClientJSWrapper(getDomainClient(), session, scriptCfg.getDomain()));
						context.put("ic", new IntegrationClientJSWrapper(getIntegrationClient(), session, scriptCfg.getDomain()));
					} else {
						context.put("ic", new IntegrationClientJSWrapper(getIntegrationClient(), session));
					}
					Function function = scriptCfg.getFunction();
					session.pushScriptLevel();
					ret = function.call(context);
					session.popScriptLevel();
				} catch(Exception e) {
					if(domainScriptLogger != null)
						domainScriptLogger.log(StringUtils.rollUpExceptions(e));
					throw new RedbackException("Error in script " + functionName, e);
				} finally {
					if(domainScriptLogger != null) {
						session.popDomainLock();
						domainScriptLogger.commit();
					}
				}
			} else {
				throw new RedbackUnauthorisedException("No rights to execute script " + functionName);
			}			
		}
		return ret;
	}	
	
	public void streamPack(Session session, String name, DataStream<RedbackObject> objectStream) throws RedbackException 
	{
		final PackConfig packConfig = packConfigs.get(session, name);
		PackStream packStream = new PackStream(session, this, objectStream);
		try {
			if(packConfig.hasQueries()) {
				for(QueryConfig query: packConfig.getQueries()) 
					packStream.addQuery(query.getObjectName(), query.getFilter(session));
			} else {
				Function script = packConfig.getScript();
				ScriptContext context = session.getScriptContext().createChild();
				context.put("stream", new PackStreamJSWrapper(packStream));
				script.call(context);
			}
			packStream.start();
		} catch(Exception e) {
			throw new RedbackException("Error starting pack stream" + name, e);
		} 		
	}
	
	public List<FunctionInfo> listFunctions(Session session, String category) throws RedbackException {
		List<FunctionInfo> retList = new ArrayList<FunctionInfo>();
		List<ScriptConfig> list = globalScripts.list(session, new DataMap("category", category));
		for(ScriptConfig sc: list) {
			if(session.getUserProfile().canExecute("rb.scripts." + sc.getName()) || session.getUserProfile().canExecute("rb.accesscat." + sc.getAccessCategory())) {
				retList.add(new FunctionInfo(sc.getName(), sc.getDescription(), sc.getShowExpression(), sc.getTimeout(), sc.getIcon()));
			}
		}
		return retList;
	}
	
	public void fork(Session session, String objectName, String id, String function, DataMap param) throws RedbackException
	{
		DataMap request = new DataMap();
		request.put("action", "execute");
		request.put("function", function);
		if(objectName != null && id != null) {
			request.put("object", objectName);
			request.put("uid", id);			
		}
		if(param != null) {
			request.put("param", param);
		}
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
		
		ObjectConfig objectConfig = getConfigIfCanRead(session, objectName);
		try
		{
			DataMap objectFilter = generateObjectFilter(session, objectName, filter, searchText);
			if(base != null)
				objectFilter.merge(new DataMap("$or", base));
			DataList dbResultList = null;
			
			if(objectConfig.isPersistent()) 
			{
				DataMap dbFilter = generateDBFilter(session, objectConfig, objectFilter);						
				DataList dbTuple = new DataList();
				for(int i = 0; i < tuple.size(); i++) {
					String attributeName = tuple.get(i) instanceof DataMap ? tuple.getObject(i).getString("attribute") : tuple.getString(i);
					String dbField = attributeName.equals("uid") ? objectConfig.getUIDDBKey() : objectConfig.getAttributeConfig(attributeName).getDBKey();
					if(tuple.get(i) instanceof DataMap) {
						DataMap tupleItem = (DataMap)tuple.getObject(i).getCopy();
						tupleItem.put("attribute", dbField);
						dbTuple.add(tupleItem);
					} else {
						dbTuple.add(dbField);
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
	
	public Value getNewID(Session session, String name) throws FunctionErrorException, FunctionTimeoutException
	{
		Payload request = new Payload(name);
		request.metadata.put("mime", "text/plain");
		request.metadata.put("session", session.getId());
		Payload response = firebus.requestService(idGeneratorServiceName, request); 
		String value = response.getString();
		return new Value(value);
	}
	
	public void initiateCurrentTransaction(Session session, boolean store)  throws RedbackException
	{
		session.setScriptContext(createScriptContext(session));
		if(store)
			session.setTxStore(new TxStore<Object>());
	}
	
	public void commitCurrentTransaction(Session session) throws RedbackException
	{
		if(session.hasTxStore()) {
			int updates = 0;
			List<RedbackObject> list = new ArrayList<RedbackObject>();
			for(Object o : session.getTxStore().getAll())
				list.add((RedbackObject)o);

			List<RedbackObject> updatedList = new ArrayList<RedbackObject>();
			List<DataTransaction> dbtxs = new ArrayList<DataTransaction>();
			for(RedbackObject rbObject: list) {
				if(rbObject.isDeleted) {
					updatedList.add(rbObject);
					dbtxs.add(rbObject.getDBDeleteTransaction());
					updates++;
				} else if(rbObject.isUpdated()) {
					updatedList.add(rbObject);
					rbObject.onSave();
					dbtxs.add(rbObject.getDBUpdateTransaction());
					updates++;
				}
				dbtxs.addAll(rbObject.getDBTraceTransactions());
			}
			
			if(dbtxs.size() > 0) {
				if(this.useMultiDBTransactions) {
					dataClient.multi(dbtxs);
				} else {
					for(DataTransaction tx: dbtxs) {
						dataClient.runTransaction(tx);
					}
				}
			}
			
			for(RedbackObject rbObject: list) {
				if(rbObject.isDeleted) {
					rbObject.afterDelete();
				} else if(rbObject.isUpdated()) {
					rbObject.afterSave();
				}
			}	
			
			signal(updatedList);
			session.setStat("objects", list.size());
			session.setStat("updates", updates);
		}
	}

	protected List<RedbackObject> getNewOrUpdatedObjects(Session session, ObjectConfig objectConfig, DataMap objectFilter) throws RedbackException 
	{
		RedbackObjectList objectList = new RedbackObjectList();
		if(session.getTxStore() != null) {
			List<Object> txList = session.getTxStore().getAll();
			for(Object o: txList) {
				RedbackObject rbo = (RedbackObject)o;
				if(rbo.getObjectConfig().getName().equals(objectConfig.getName()) && rbo.filterApplies(objectFilter) && !rbo.filterOriginallyApplied(objectFilter))
					objectList.add(rbo);
			}			
		}
		return objectList;
	}

	protected List<DataMap> convertDataListToList(DataList dataList) 
	{
		List<DataMap> list = new ArrayList<DataMap>();
		if(dataList != null) 
			for(int i = 0; i < dataList.size(); i++)
				list.add(dataList.getObject(i));
		return list;
	}

	
	protected List<RedbackObject> convertDBDataToObjects(Session session, ObjectConfig objectConfig, DataMap objectFilter, DataList dbResultList, boolean includeNewObjects) throws RedbackException 
	{
		return convertDBDataToObjects(session, objectConfig, objectFilter, convertDataListToList(dbResultList), includeNewObjects);
	}

	protected List<RedbackObject> convertDBDataToObjects(Session session, ObjectConfig objectConfig, DataMap objectFilter, List<DataMap> dbResultList, boolean includeNewObjects) throws RedbackException 
	{
		RedbackObjectList objectList = new RedbackObjectList();
		if(includeNewObjects)  
			objectList.addAll(getNewOrUpdatedObjects(session, objectConfig, objectFilter));

		if(dbResultList != null) { 
			for(DataMap dbData : dbResultList) {
				RedbackObject rbo = convertDBDataToObjectIfStillApplies(session, objectConfig, objectFilter, dbData);
				if(rbo != null)
					objectList.add(rbo);
			}
		}
		return objectList;
	}
	
	protected RedbackObject convertDBDataToObjectIfStillApplies(Session session, ObjectConfig objectConfig, DataMap objectFilter, DataMap dbData) throws RedbackException 
	{
		String key = objectConfig.getName() + ":" + dbData.getString(objectConfig.getUIDDBKey());
		RedbackObject rbo = session.hasTxStore() ? (RedbackObject)session.getTxStore().get(key) : null; 
		if(rbo != null) {
			if(rbo.isDeleted || !rbo.filterApplies(objectFilter))
				rbo = null;
		} else {
			rbo = new RedbackObject(session, this, objectConfig, dbData);
		}
		return rbo;
	}
	
	protected DataList generateNonPersistentObjectData(Session session, ObjectConfig objectConfig, DataMap filter, String searchText, DataMap sort, int page, int pageSize) throws RedbackException, ScriptException
	{
		DataList dbResultList = null;
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
		return dbResultList;
	}
	
	protected DataMap generateObjectFilter(Session session, String objectName, DataMap filter, String searchText) throws RedbackException 
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
		return objectFilter;
	}
	
	protected DataMap generateRightsReadFilter(Session session, String objectName) throws RedbackException 
	{
		DataMap filter = session.getUserProfile().getReadFilter("rb.objects." + objectName);
		if(filter != null) {
			String s = filter.toString(true); //Not ideal to get the cache key;
			ExpressionMap em = readRightsFilters.get(s);
			if(em == null) {
				String funcName = objectName + "_readrightsfilter_" + StringUtils.base16(filter.hashCode());
				em = new ExpressionMap(scriptFactory, funcName, filter);
				readRightsFilters.put(s, em);
			}
			return em.eval(session.getScriptContext());
		} else {
			return null;
		}
	}
	
	protected DataMap generateDBFilter(Session session, ObjectConfig objectConfig, DataMap objectFilter) throws DataException, RedbackException
	{
		DataMap dbFilter = generateDBFilterRecurring(session, objectConfig, objectFilter);
		if(objectConfig.getDomainDBKey() != null) {
			DataMap domainFilter = session.getDomainFilterClause();
			if(domainFilter != null)
				dbFilter.put(objectConfig.getDomainDBKey(), domainFilter);
		}
		return dbFilter;
	}
	
	protected DataMap generateDBFilterRecurring(Session session, ObjectConfig objectConfig, DataMap objectFilter) throws DataException, RedbackException
	{
		DataMap dbFilter = new DataMap();
		Iterator<String> it = objectFilter.keySet().iterator();
		while(it.hasNext())
		{
			String key = it.next();
			if(key.equals("$eq")  ||  key.equals("$gt")  ||  key.equals("$gte")  ||  key.equals("$lt")  ||  key.equals("$lte")  ||  key.equals("$ne") || key.equals("$regex") || key.equals("$where"))
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
					dbList.add(generateDBFilterRecurring(session, objectConfig, list.getObject(i)));
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
						dbFilterValue = generateDBFilterRecurring(session, objectConfig, (DataMap)objectFilterValue);
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
	
	protected DataMap generateDBSort(Session session, ObjectConfig objectConfig, DataMap objectSort) throws DataException, RedbackException
	{
		DataMap dbSort = null;
		if(objectSort != null) {
			dbSort = new DataMap();
			for(int i = 0; objectSort.containsKey(String.valueOf(i)); i++) {
				DataMap sortItem = new DataMap();
				String attribute = objectSort.getObject(String.valueOf(i)).getString("attribute");
				if(attribute != null) {
					String dbKey = null;
					if(attribute.equals("uid"))
						dbKey = objectConfig.getUIDDBKey();
					else if(attribute.equals("domain"))
						dbKey = objectConfig.getDomainDBKey();
					else {
						AttributeConfig attributeConfig = objectConfig.getAttributeConfig(attribute);
						if(attributeConfig != null)
							dbKey = objectConfig.getAttributeConfig(attribute).getDBKey();
					}
					if(dbKey != null) {
						sortItem.put("attribute", dbKey);
						sortItem.put("dir", objectSort.getObject("" + i).getString("dir"));
						dbSort.put("" + i, sortItem);
					} 
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
						ObjectConfig relatedConfig = getConfigIfCanRead(session, relatedObjectName, false);
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

	
	protected void signal(List<RedbackObject> list)
	{
		if(objectUpdateChannel != null && list.size() > 0) 
		{
			try 
			{
				DataList outList = new DataList();
				for(RedbackObject object: list)
					outList.add(object.getDataMap(true, false));
				Payload payload = new Payload(new DataMap("list", outList));
				firebus.publish(objectUpdateChannel, payload);					
			}
			catch(Exception e) 
			{
				Logger.severe("rb.object.signal.error", e);
			}
		}		
	}

}

