package io.redback.managers.objectmanager;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import io.firebus.data.DataFilter;
import io.firebus.data.DataMap;
import io.firebus.exceptions.FunctionErrorException;
import io.firebus.exceptions.FunctionTimeoutException;
import io.firebus.logging.Logger;
import io.firebus.script.Expression;
import io.firebus.script.Function;
import io.firebus.script.ScriptContext;
import io.firebus.script.exceptions.ScriptException;
import io.firebus.script.exceptions.ScriptValueException;
import io.redback.client.DataClient.DataTransaction;
import io.redback.client.js.DomainClientJSWrapper;
import io.redback.client.js.IntegrationClientJSWrapper;
import io.redback.exceptions.RedbackException;
import io.redback.exceptions.RedbackInvalidConfigException;
import io.redback.exceptions.RedbackInvalidRequestException;
import io.redback.managers.objectmanager.js.RedbackObjectJSWrapper;
import io.redback.security.Session;

public class RedbackObject extends RedbackElement
{
	protected Value uid;
	protected Value domain;
	protected String key;
	protected long lastUpdated;
	protected boolean canRead;
	protected boolean canWrite;
	protected boolean canExecute;
	protected Map<String, Value> data;
	protected Map<String, Value> originalData;
	protected Map<String, RedbackObject> related;
	protected List<String> updatedAttributes;
	protected List<DataMap> traceEvents;
	protected boolean isNewObject;
	protected boolean isDeleted;
	protected DataMap cachedDataMap;

	// Initiate existing object from pre-loaded data
	protected RedbackObject(Session s, ObjectManager om, ObjectConfig cfg, DataMap dbData) throws RedbackException
	{
		init(s, om, cfg);	
		isNewObject = false;
		if(canRead)
		{
			uid = new Value(dbData.getString(config.getUIDDBKey()));
			domain = new Value(config.isDomainManaged() ? dbData.getString(config.getDomainDBKey()) : "root");
			key = config.getName() + ":" + uid.getString();
			if(session.hasTxStore())
				session.getTxStore().add(key, this);
			Iterator<String> it = config.getAttributeNames().iterator();
			while(it.hasNext())
			{
				AttributeConfig attributeConfig = config.getAttributeConfig(it.next());
				String dbKey = attributeConfig.getDBKey();
				if(dbKey != null)
				{
					Value val = new Value(dbData.get(dbKey));
					data.put(attributeConfig.getName(), val);
					originalData.put(attributeConfig.getName(), val);
				}
			}
			postInitScriptContextUpdate();
			updateScriptContext();
			executeFunction("onload", scriptContext);
		}
		else
		{
			throw new RedbackException("User does not have the right to read object " + config.getName());
		}
	}
	
	// Initiate new object
	protected RedbackObject(Session s, ObjectManager om, ObjectConfig cfg, String u, String d) throws RedbackException
	{
		init(s, om, cfg);		
		isNewObject = true;
		if(canWrite)
		{
			try
			{
				if(u != null) 
				{
					List<RedbackObject> others = om.listObjects(s, config.getName(), new DataMap("uid", u), null, null, false, 0, 1);
					if(others.size() == 0)
					{
						uid = new Value(u);
					}
					else
					{
						throw new RedbackException("Another object " + config.getName() + " already exists with uid " + u, null);
					}
				}
				else if(config.getUIDGeneratorName() != null)
				{
					uid = objectManager.getNewID(session, config.getUIDGeneratorName());
				}
				else
				{
					throw new RedbackException("No UID has been provided or no UID Generator has been configured for object " + config.getName() , null);
				}
				
				
				if(!config.isDomainManaged()) 
				{
					domain = new Value("root");
				}
				else if(d != null)
				{
					domain = new Value(d);
				} 
				else if(session.getDomainLock() != null) {
					domain = new Value(session.getDomainLock());
				}
				else
				{
					String defaultDomain = session.getUserProfile().getDefaultDomain();
					if(defaultDomain != null) {
						domain = new Value(defaultDomain);
					} else {
						throw new RedbackException("No domain has been provided and no default domain has been configure for the user");
					}
				}

				key = config.getName() + ":" + uid.getString();
				if(session.hasTxStore())
					session.getTxStore().add(key, this);
				postInitScriptContextUpdate();
				traceEvent("objectcreate", null, null, null);

				Iterator<String> it = config.getAttributeNames().iterator();
				while(it.hasNext())
				{
					AttributeConfig attributeConfig = config.getAttributeConfig(it.next());
					String attributeName = attributeConfig.getName();
					String idGeneratorName = attributeConfig.getIdGeneratorName();
					Expression defaultValue = attributeConfig.getDefaultValue();
					Value value = null;
					if(idGeneratorName != null)
						value = objectManager.getNewID(session, idGeneratorName);
					else if(defaultValue != null)
						value = new Value(defaultValue.eval(scriptContext));
					if(value != null) {
						data.put(attributeName, value);
						updatedAttributes.add(attributeName);	
						executeAttributeFunction(attributeName, "onupdate");
					}
				}
				updateScriptContext();				
				executeFunction("oncreate", scriptContext);
			}
			catch(FunctionTimeoutException | FunctionErrorException | ScriptException e)
			{
				throw new RedbackException("Problem initiating object " + config.getName(), e);
			}
		}
		else
		{
			throw new RedbackException("User does not have the right to create object " + config.getName());
		}		
	}
	
	protected void init(Session s, ObjectManager om, ObjectConfig cfg) throws RedbackException
	{
		session = s;
		objectManager = om;
		config = cfg;
		lastUpdated = System.currentTimeMillis();
		String objectRightKey = "rb.objects." + config.getName();
		String accessCatKey = "rb.accesscat." + config.getAccessCategory();
		canRead = session.getUserProfile().canRead(objectRightKey) || session.getUserProfile().canRead(accessCatKey);
		canWrite = session.getUserProfile().canWrite(objectRightKey) || session.getUserProfile().canWrite(accessCatKey);
		canExecute = session.getUserProfile().canExecute(objectRightKey) || session.getUserProfile().canExecute(accessCatKey);
		data = new HashMap<String, Value>();
		originalData = new HashMap<String, Value>();
		related = new HashMap<String, RedbackObject>();
		updatedAttributes = new ArrayList<String>();
		traceEvents = new ArrayList<DataMap>();
		scriptContext = session.getScriptContext().createChild();
		try {
			scriptContext.put("self", new RedbackObjectJSWrapper(this));
		} catch(ScriptValueException e) {
			throw new RedbackException("Error setting script context value", e);
		}
	}
	
	protected void postInitScriptContextUpdate() throws RedbackException
	{
		try {
			scriptContext.put("dc", new DomainClientJSWrapper(objectManager.getDomainClient(), session, getDomain().getString()));
			scriptContext.put("ic", new IntegrationClientJSWrapper(objectManager.getIntegrationClient(), session, getDomain().getString()));
			scriptContext.put("uid", getUID().getString());
		} catch(ScriptValueException e) {
			throw new RedbackException("Error setting script context value", e);
		}		
	}
	
	protected void updateScriptContext() throws RedbackException 
	{
		try {
			Iterator<String> it = getAttributeNames().iterator();
			while(it.hasNext())
			{	
				String key = it.next();
				if(getObjectConfig().getAttributeConfig(key).getExpression() == null)
					scriptContext.put(key, get(key).getObject());
			}
		} catch(ScriptValueException e) {
			throw new RedbackException("Error setting script context value", e);
		}	
	}

	public Value getUID()
	{
		return  uid;
	}
	
	public Value getDomain()
	{
		return domain;
	}
	
	public String getKey() 
	{
		return key;
	}
	
	public boolean isNew()
	{
		return isNewObject;
	}
	
	public Set<String> getAttributeNames()
	{
		return config.getAttributeNames();
	}
	
	public Value get(String name) throws RedbackException
	{
		AttributeConfig attributeConfig = config.getAttributeConfig(name);
		if(name.equals("uid"))
		{
			return getUID();
		}
		else if(name.equals("domain"))
		{
			return getDomain();
		}
		else if(attributeConfig != null)
		{
			Expression expression = attributeConfig.getExpression();
			if(data.containsKey(name))
				return data.get(name);
			else if(expression != null) {
				try {
					Object o = expression.eval(scriptContext);
					Value val = new Value(o);
					return val;
				} catch(ScriptException e) {
					throw new RedbackException("Error getting expression attribute '" + name + "' for object '" + getObjectConfig().getName() + "." + getUID().getString() + "'", e);
				}
			} else 
				return new Value(null);
		}		
		return null;
	}
	
	public String getString(String name) throws RedbackException
	{
		return get(name).getString();
	}
	
	public RedbackObject getRelated(String name)
	{
		AttributeConfig attributeConfig = config.getAttributeConfig(name);
		 if(attributeConfig != null)
		 {
			if(attributeConfig.hasRelatedObject()  &&  data.get(name) != null  &&   !data.get(name).isNull())
			{
				if(related.get(name) == null)
				{
					try
					{
						RelatedObjectConfig roc = attributeConfig.getRelatedObjectConfig();
						if(roc.getLinkAttributeName().equals("uid"))
						{
							related.put(name, objectManager.getObject(session, roc.getObjectName(), get(name).getString()));
						}
						else
						{
							List<RedbackObject> resultList = objectManager.listObjects(session, roc.getObjectName(), getRelatedFindFilter(name), null, null, false, 0, 50);
							RedbackObject selected = null;
							int selectedPoints = 0;
							for(RedbackObject o : resultList) {
								int points = o.getDomain().equals(this.getDomain()) ? 2 : o.getDomain().equals("root") ? 1 : 0;
								if(points > selectedPoints) {
									selectedPoints = points;
									selected = o;
								}
							}
							if(selected == null && resultList.size() > 0)
								selected = resultList.get(0);
							return selected;
						}
					}
					catch(RedbackException e)
					{
						//TODO: Handle somehow!
					}
				}
				return related.get(name);
			}
		}
		 return null;
	}
	
	public List<RedbackObject> getRelatedList(String attributeName, DataMap additionalFilter, String searchText, DataMap sort) throws RedbackException
	{
		return getRelatedList(attributeName, additionalFilter, searchText, sort, 0, 50);
	}
	
	public List<RedbackObject> getRelatedList(String attributeName, DataMap additionalFilter, String searchText, DataMap sort, int page, int pageSize) throws RedbackException
	{
		RelatedObjectConfig roc = config.getAttributeConfig(attributeName).getRelatedObjectConfig();
		if(roc != null)
		{
			DataMap relatedObjectListFilter = getRelatedListFilter(attributeName);
			if(additionalFilter != null)
				relatedObjectListFilter.merge(additionalFilter);
			List<RedbackObject> relatedObjectList = objectManager.listObjects(session, roc.getObjectName(), relatedObjectListFilter, searchText, sort, false, page, pageSize);
			return relatedObjectList;		
		} else {
			throw new RedbackInvalidConfigException("Attribute " + attributeName + " does not have a relationship");
		}
	}
	
	public DataMap getRelatedFindFilter(String attributeName) throws RedbackException
	{
		DataMap filter = null;
		RelatedObjectConfig roc = config.getAttributeConfig(attributeName).getRelatedObjectConfig();
		if(roc != null)
		{
			String linkAttribute = roc.getLinkAttributeName();
			if(linkAttribute.equals("uid")) 
			{
				filter = new DataMap("uid", get(attributeName).getObject());
			}
			else
			{
				filter = getRelatedListFilter(attributeName);
				if(filter == null)
					filter = new DataMap();
				filter.put(roc.getLinkAttributeName(), get(attributeName).getObject());
			}
		}
		return filter;
	}
	
	protected DataMap getRelatedListFilter(String attributeName) throws RedbackException
	{
		DataMap filter = null;
		RelatedObjectConfig roc = config.getAttributeConfig(attributeName).getRelatedObjectConfig();
		if(roc != null)
		{
			filter = roc.generateFilter(this);
		}
		return filter;
	}
	
	public void put(String name, Value value) throws RedbackException
	{
		AttributeConfig attributeConfig = getObjectConfig().getAttributeConfig(name);
		if(attributeConfig != null)
		{
			Value actualValue = value;
			if(value.getObject() instanceof DataMap && attributeConfig.getRelatedObjectConfig() != null) 
			{
				DataMap filter = (DataMap)value.getObject();
				List<RedbackObject> list = objectManager.listRelatedObjects(session, getObjectConfig().getName(), uid.getString(), name, filter, null, null, false);
				if(list.size() > 0) 
					actualValue = new Value(list.get(0).get(getObjectConfig().getAttributeConfig(name).getRelatedObjectConfig().getLinkAttributeName()).getString());
				else
					actualValue = new Value(null);
			}
			Value currentValue = get(name);
			if(!currentValue.equals(actualValue))
			{
				if(canWrite  &&  (isEditable(name) || isNewObject))
				{
					data.put(name, actualValue);
					updatedAttributes.add(name);
					if(!attributeConfig.noTrace())
						traceEvent("objectupdate", name, actualValue.toString(), null);
					lastUpdated = System.currentTimeMillis();
					try {
						if(attributeConfig.getExpression() == null) 
							scriptContext.put(name, actualValue.getObject());
						ScriptContext attributeUpdateScriptContext = scriptContext.createChild();
						attributeUpdateScriptContext.put("previousValue", currentValue.getObject());
						executeAttributeFunction(name, "onupdate", attributeUpdateScriptContext);
					} catch(ScriptValueException e) {
						throw new RedbackException("Error setting script context value", e);
					}
				}
				else
				{
					if(canWrite)
						throw new RedbackInvalidRequestException("User '" + session.getUserProfile().getUsername() + "' does not have the right to update attribute '" + name + "' on object '" + config.getName() + ":" + getUID().getString() + "'");
					else
						throw new RedbackInvalidRequestException("User '" + session.getUserProfile().getUsername() + "' does not have the right to update object '" + config.getName() + ":" + getUID().getString() + "'");
				}
			}
		}
		else
		{
			throw new RedbackException("This attribute '" + name + "' does not exist");
		}
	}

	public void put(String name, String value) throws RedbackException
	{
		put(name, new Value(value));
	}
	
	public void put(String name, RedbackObject relatedObject) throws RedbackException
	{
		if(config.getAttributeConfig(name).hasRelatedObject())
		{
			RelatedObjectConfig roc = config.getAttributeConfig(name).getRelatedObjectConfig();
			if(relatedObject.getObjectConfig().getName().equals(roc.getObjectName()))
			{
				String relatedObjectLinkAttribute = roc.getLinkAttributeName();
				Value linkValue = relatedObject.get(relatedObjectLinkAttribute);
				put(name, linkValue);
				related.put(name, relatedObject);
			}			
		}
	}
	
	public void setRelated(String name, RedbackObject relatedObject) throws RedbackException
	{
		if(config.getAttributeConfig(name).hasRelatedObject())
		{
			RelatedObjectConfig roc = config.getAttributeConfig(name).getRelatedObjectConfig();
			if(relatedObject.getObjectConfig().getName().equals(roc.getObjectName()))
				related.put(name, relatedObject);
		}
	}
	
	public void clear(String name) throws ScriptException, RedbackException
	{
		put(name, new Value(null));
	}
	
	public boolean isEditable(String name) throws RedbackException
	{
		if((domain != null && domain.equals("root")) || !canWrite) {
			return false;
		} else {
			try {
				Expression expression = config.getAttributeConfig(name).getEditableExpression(); 
				Object o = expression.eval(scriptContext);
				if(o instanceof Boolean)
					return (Boolean)o;
				else
					return false;
			} catch(ScriptException e) {
				throw new RedbackException("Error evaluation isEditable expression", e);
			}
		}
	}
	
	public boolean isMandatory(String name) throws RedbackException
	{
		try {
			Expression expression = config.getAttributeConfig(name).getMandatoryExpression(); 
			Object o = expression.eval(scriptContext);
			if(o instanceof Boolean)
				return (Boolean)o;
			else
				return false;
		} catch(ScriptException e) {
			throw new RedbackException("Error evaluating isMandatory expression", e);
		}		
	}
	
	public boolean isDeleted() 
	{
		return isDeleted;
	}
		
	public boolean isUpdated() 
	{
		return canWrite && (updatedAttributes.size() > 0  ||  isNewObject == true);
	}
	
	public boolean canDelete() throws RedbackException
	{
		if((domain != null && domain.equals("root")) || !canWrite) {
			return false;
		} else {
			try {
				Expression expression = config.getCanDeleteExpression();
				Object o = expression.eval(scriptContext);
				return o instanceof Boolean ? (Boolean)o : false;
			} catch(ScriptException e) {
				throw new RedbackException("Error evaluating canDelete expression", e);
			}	
		}
	}
	
	public void delete() throws RedbackException
	{
		if(canDelete()) {
			isDeleted = true;
			lastUpdated = System.currentTimeMillis();
			executeFunction("ondelete", scriptContext);
		} else {
			throw new RedbackException("The object '" + config.getName() + ":" + getUID().getString() + "' cannot be deleted");
		}
	}
	
	public List<String> getUpdatedAttributes() 
	{
		return updatedAttributes;
	}
	
	public DataTransaction getDBDeleteTransaction()
	{
		if(isDeleted()) {
			return objectManager.getDataClient().createDelete(config.getCollection(), new DataMap(config.getUIDDBKey(), getUID().getObject()));
		} else {
			return null;
		}
	}
	
	public DataTransaction getDBUpdateTransaction() throws RedbackException 
	{
		if(isUpdated()) {
			DataMap key = new DataMap(config.getUIDDBKey(), getUID().getObject());
			DataMap dbData = new DataMap();
			if(isNewObject && config.isDomainManaged())
				dbData.put(config.getDomainDBKey(), domain.getObject());
			for(String attributeName: updatedAttributes)
			{
				AttributeConfig attributeConfig = config.getAttributeConfig(attributeName);
				String attributeDBKey = attributeConfig.getDBKey();
				if(attributeDBKey != null)
				{
					Object val = get(attributeName).getObject();
					dbData.put(attributeDBKey, val);
				}
			}
			return objectManager.getDataClient().createPut(config.getCollection(), key, dbData, null);
		} else {
			return null;
		}
	}
	
	public List<DataTransaction> getDBTraceTransactions() throws RedbackException 
	{
		List<DataTransaction> traceTxs = new ArrayList<DataTransaction>();
		for(DataMap event: traceEvents) {
			event.put("date", new Date());
			event.put("username", session.getUserProfile().getUsername());
			event.put("domain", getDomain().getString());
			event.put("object", config.getName());
			event.put("uid", uid.getString());
			traceTxs.add(objectManager.getDataClient().createPut(
					objectManager.traceCollection.getName(), 
					objectManager.traceCollection.convertObjectToSpecific(new DataMap("_id", UUID.randomUUID().toString())),
					objectManager.traceCollection.convertObjectToSpecific(event), 
					null));
		}
		return traceTxs;
	}
	
	public void onSave() throws RedbackException
	{
		if(isDeleted != true && (updatedAttributes.size() > 0  ||  isNewObject == true) && canWrite)
		{
			executeFunction("onsave", scriptContext);
		}
	}
	
	public void afterSave() 
	{
		if(isDeleted != true && (updatedAttributes.size() > 0  ||  isNewObject == true) && canWrite)
		{
			try {
				executeFunction("aftersave", scriptContext);
				if(isNewObject)
				{
					executeFunction("aftercreate", scriptContext);
					isNewObject = false;
				}
			} catch(Exception e) {
				Logger.severe("rb.object.aftersave", "Error in after save trigger", e);
			}				
			updatedAttributes.clear();
		}		
	}
	
	public void afterDelete()
	{
		
	}
	
	public Object executeFunction(String functionName) throws RedbackException
	{
		return executeFunction(functionName, (DataMap)null);
	}
	
	public Object executeFunction(String functionName, DataMap param) throws RedbackException
	{
		if(canExecute)
		{
			ScriptContext context = scriptContext;
			if(param != null) {
				try {
					context = scriptContext.createChild();
					context.put("param", param);
				} catch(Exception e) {
					throw new RedbackException("Error creating the script context", e);
				}
			} 
			return executeFunction(functionName, context);
		}
		else
		{
			throw new RedbackException("User does not have the right to execute functions in object " + config.getName());
		}
	}

	protected Object executeFunction(String functionName, ScriptContext context) throws RedbackException
	{
			Function function  = config.getScriptForEvent(functionName);
			if(function != null) {
				String name = getObjectConfig().getName() + ":" + getUID().getString() + "." + functionName;
				return execute(function, name, context);
			} else {
				return null; //Return null instead of throwing an error so the object can automatically try to launch events even if they don't exist
			}
	}
	
	protected void executeAttributeFunction(String attributeName, String event) throws RedbackException
	{
		executeAttributeFunction(attributeName, event, scriptContext);
	}

	protected void executeAttributeFunction(String attributeName, String functionName, ScriptContext context) throws RedbackException
	{
		Function script  = config.getAttributeConfig(attributeName).getScriptForEvent(functionName);
		if(script != null) {
			String name = getObjectConfig().getName() + ":" + getUID().getString() + "." + attributeName + "." + functionName;
			execute(script, name, context);
		}
	}
	
	protected Object execute(Function function, String functionName, ScriptContext context) throws RedbackException
	{
		Object retVal = null;
		try
		{
			session.pushDomainLock(getDomain().getString());
			session.pushScriptLevel();
			retVal = function.call(context);
			session.popScriptLevel();
			session.popDomainLock();
		} 
		catch (ScriptException e)
		{
			throw new RedbackException("Problem occurred executing script " + functionName, e);
		}		
		return retVal;
	}
	
	protected void traceEvent(String action, String attribute, String value, String function) throws RedbackException {
		if(config.traceUpdates()
				&& objectManager.traceCollection != null 
				&& !session.getUserProfile().getUsername().equals(objectManager.sysUserManager.getUsername())
				&& !session.isInScript()) {
			DataMap event = new DataMap("action", action);
			if(attribute != null) {
				event.put("attribute", attribute);
				event.put("value", get(attribute).getObject());
			}
			if(function != null) {
				event.put("function", function);
			}
			this.traceEvents.add(event);
		}
	}
	
	public DataMap getDataMap(boolean addValidation, boolean addRelated) throws RedbackException
	{
		return getDataMap(addValidation, addRelated, false);
	}
	
	public DataMap getDataMap(boolean addValidation, boolean addRelated, boolean cache) throws RedbackException
	{
		if(cache == true && cachedDataMap != null) {
			return cachedDataMap;
		} else {
			DataMap object = new DataMap();
			
			object.put("objectname", config.getName());
			object.put("uid", uid.getObject());
			object.put("domain", domain.getObject());
			object.put("ts", lastUpdated);
			if(isDeleted()) {
				object.put("deleted", true);
			} else {
				DataMap dataNode = new DataMap();
				DataMap validatonNode = new DataMap();
				DataMap relatedNode = new DataMap();

				if(addValidation)
					validatonNode.put("_candelete", this.canDelete());
				
				Iterator<String> it = config.getAttributeNames().iterator();
				while(it.hasNext())
				{
					AttributeConfig attributeConfig = config.getAttributeConfig(it.next());
					if(attributeConfig.isInternal() == false) {
						String attrName = attributeConfig.getName();
						Value attrValue = get(attrName);
		
						if(addValidation) {
							DataMap attributeValidation = new DataMap();
							attributeValidation.put("editable", isEditable(attrName));
							attributeValidation.put("mandatory", isMandatory(attrName));
							attributeValidation.put("updatescript", attributeConfig.getScriptForEvent("onupdate") != null);
							if(attributeConfig.hasRelatedObject())
							{
								DataMap relatedObjectValidation = new DataMap();
								relatedObjectValidation.put("object",  attributeConfig.getRelatedObjectConfig().getObjectName());
								relatedObjectValidation.put("link",  attributeConfig.getRelatedObjectConfig().getLinkAttributeName());
								relatedObjectValidation.put("listfilter",  getRelatedListFilter(attributeConfig.getName()));
								boolean shouldUIResolve = attributeConfig.getRelatedObjectConfig().shouldUIResolve();
								if(!shouldUIResolve) relatedObjectValidation.put("uiresolve", shouldUIResolve);
								attributeValidation.put("related", relatedObjectValidation);
							}
							validatonNode.put(attrName, attributeValidation);
						}
						
						if(addRelated  &&  attributeConfig.hasRelatedObject())
						{
							RedbackObject relatedObject = getRelated(attrName);
							if(relatedObject != null)
								relatedNode.put(attrName, relatedObject.getDataMap(false, false, cache));
						}
		
						dataNode.put(attrName, attrValue.getObject());
					}
				}
				object.put("data", dataNode);
				
				if(addValidation)
					object.put("validation", validatonNode);
				if(addRelated)
					object.put("related", relatedNode);
			}
			if(cache)
				cachedDataMap = object;
			return object;			
		}

	}
	

	
	public boolean filterApplies(DataMap objectFilter) {
		return filterAppliesToDataView(objectFilter, data);
	
	}
	
	public boolean filterOriginallyApplied(DataMap objectFilter) {
		return filterAppliesToDataView(objectFilter, originalData);
	}
	
	protected boolean filterAppliesToDataView(DataMap objectFilter, Map<String, Value> d) {
		DataMap dataView = new DataMap();
		for(String key: d.keySet()) 
			dataView.put(key, d.get(key).getObject());
		dataView.put("uid", uid.getObject());
		dataView.put("domain", domain.getObject());
		DataFilter filter = new DataFilter(objectFilter);
		return filter.apply(dataView);		
	}
	
	public String toString()
	{
		try
		{
			return getDataMap(false, false).toString();
		}
		catch(RedbackException e)
		{
			return e.getMessage();
		}
	}

}
