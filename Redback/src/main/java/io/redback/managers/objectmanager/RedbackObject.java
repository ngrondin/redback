package io.redback.managers.objectmanager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

import javax.script.ScriptException;

import io.firebus.exceptions.FunctionErrorException;
import io.firebus.exceptions.FunctionTimeoutException;
import io.firebus.utils.DataFilter;
import io.firebus.utils.DataMap;
import io.redback.client.js.DomainClientJSWrapper;
import io.redback.client.js.IntegrationClientJSWrapper;
import io.redback.exceptions.RedbackException;
import io.redback.managers.jsmanager.Expression;
import io.redback.managers.jsmanager.Function;
import io.redback.managers.objectmanager.js.RedbackObjectJSWrapper;
import io.redback.security.Session;
import io.redback.utils.StringUtils;
import io.redback.utils.js.JSConverter;

public class RedbackObject extends RedbackElement
{
	private Logger logger = Logger.getLogger("io.redback");
	protected Value uid;
	protected Value domain;
	protected boolean canRead;
	protected boolean canWrite;
	protected boolean canExecute;
	protected HashMap<String, Value> data;
	protected HashMap<String, RedbackObject> related;
	protected ArrayList<String> updatedAttributes;
	protected boolean isNewObject;
	protected boolean isDeleted;

	// Initiate existing object from pre-loaded data
	protected RedbackObject(Session s, ObjectManager om, ObjectConfig cfg, DataMap dbData) throws RedbackException, ScriptException
	{
		init(s, om, cfg);	
		isNewObject = false;
		if(canRead)
		{
			uid = new Value(dbData.getString(config.getUIDDBKey()));
			domain = new Value(config.isDomainManaged() ? dbData.getString(config.getDomainDBKey()) : "root");
			Iterator<String> it = config.getAttributeNames().iterator();
			while(it.hasNext())
			{
				AttributeConfig attributeConfig = config.getAttributeConfig(it.next());
				String dbKey = attributeConfig.getDBKey();
				if(dbKey != null)
				{
					Value val = new Value(dbData.get(dbKey));
					data.put(attributeConfig.getName(), val);
				}
			}
			postInitScriptContextUpdate();
			updateScriptContext();
			executeScriptsForEvent("onload");
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
				else if(session.getUserProfile().getAttribute("rb.defaultdomain") != null)
				{
					domain = new Value(session.getUserProfile().getAttribute("rb.defaultdomain"));
				}
				else if(session.getUserProfile().getDomains().size() > 0)
				{
					domain = new Value(session.getUserProfile().getDomains().get(0));
				}
				else
				{
					throw new RedbackException("No domain has been provided and no default domain has been configure for the user");
				}
				postInitScriptContextUpdate();

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
						executeAttributeScriptsForEvent(attributeName, "onupdate");
					}
				}
				updateScriptContext();				
				executeScriptsForEvent("oncreate");
			}
			catch(FunctionTimeoutException | FunctionErrorException e)
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
		canRead = session.getUserProfile().canRead("rb.objects." + config.getName());
		canWrite = session.getUserProfile().canWrite("rb.objects." + config.getName());
		canExecute = session.getUserProfile().canExecute("rb.objects." + config.getName());
		data = new HashMap<String, Value>();
		related = new HashMap<String, RedbackObject>();
		updatedAttributes = new ArrayList<String>();
		scriptContext = objectManager.createScriptContext(session);
		scriptContext.put("self", new RedbackObjectJSWrapper(this));
	}
	
	protected void postInitScriptContextUpdate() throws RedbackException
	{
		scriptContext.put("dc", new DomainClientJSWrapper(objectManager.getDomainClient(), session, getDomain().getString()));
		scriptContext.put("ic", new IntegrationClientJSWrapper(objectManager.getIntegrationClient(), session, getDomain().getString()));
		scriptContext.put("uid", getUID().getString());
	}
	
	protected void updateScriptContext() throws RedbackException 
	{
		Iterator<String> it = getAttributeNames().iterator();
		while(it.hasNext())
		{	
			String key = it.next();
			if(getObjectConfig().getAttributeConfig(key).getExpression() == null)
				scriptContext.put(key, JSConverter.toJS(get(key).getObject()));
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
				//Timer t = new Timer("expr");
				Object o = expression.eval(scriptContext);
				//t.mark();
				Value val = new Value(o);
				return val;
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
		List<RedbackObject> relatedObjectList = null;
		RelatedObjectConfig roc = config.getAttributeConfig(attributeName).getRelatedObjectConfig();
		if(roc != null)
		{
			DataMap relatedObjectListFilter = getRelatedListFilter(attributeName);
			if(additionalFilter != null)
				relatedObjectListFilter.merge(additionalFilter);
			relatedObjectList = objectManager.listObjects(session, roc.getObjectName(), relatedObjectListFilter, searchText, sort, false, page, pageSize);
		}
		return relatedObjectList;		
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
					//updateScriptContext();
					if(attributeConfig.getExpression() == null) 
						scriptContext.put(name, JSConverter.toJS(actualValue.getObject()));
					scriptContext.put("previousValue", currentValue.getObject());
					executeAttributeScriptsForEvent(name, "onupdate");
					scriptContext.remove("previousValue");
				}
				else
				{
					if(canWrite)
						throw new RedbackException("User '" + session.getUserProfile().getUsername() + "' does not have the right to update attribute '" + name + "' on object '" + config.getName() + ":" + getUID().getString() + "'");
					else
						throw new RedbackException("User '" + session.getUserProfile().getUsername() + "' does not have the right to update object '" + config.getName() + ":" + getUID().getString() + "'");
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
	
	public void clear(String name) throws ScriptException, RedbackException
	{
		put(name, new Value(null));
	}
	
	public boolean isEditable(String name) throws RedbackException
	{
		if(domain != null && domain.equals("root"))
			return false;
		else
		{
			Expression expression = config.getAttributeConfig(name).getEditableExpression(); 
			Object o = expression.eval(scriptContext);
			if(o instanceof Boolean)
				return (Boolean)o;
			else
				return false;
		}
	}
	
	public boolean isMandatory(String name) throws RedbackException
	{
		Expression expression = config.getAttributeConfig(name).getMandatoryExpression(); 
		Object o = expression.eval(scriptContext);
		if(o instanceof Boolean)
			return (Boolean)o;
		else
			return false;
	}
		
	
	public boolean canDelete() throws RedbackException
	{
		Expression expression = config.getCanDeleteExpression();
		Object o = expression.eval(scriptContext);
		if(o instanceof Boolean)
			return (Boolean)o;
		else
			return false;
	}
	
	public void delete() throws RedbackException
	{
		if(canDelete()) {
			isDeleted = true;
			executeScriptsForEvent("ondelete");
		} else {
			throw new RedbackException("The object '" + config.getName() + ":" + getUID().getString() + "' cannot be deleted");
		}
	}
	
	public List<String> getUpdatedAttributes() 
	{
		return updatedAttributes;
	}
	
	public void save() throws ScriptException, RedbackException
	{
		if(isDeleted)
		{
			DataMap key = new DataMap();
			key.put(config.getUIDDBKey(), getUID().getObject());
			objectManager.getDataClient().deleteData(config.getCollection(), key);
		}
		else if(updatedAttributes.size() > 0  ||  isNewObject == true)
		{
			if(canWrite)
			{
				executeScriptsForEvent("onsave");
				DataMap key = new DataMap();
				key.put(config.getUIDDBKey(), getUID().getObject());

				DataMap dbData = new DataMap();
				if(isNewObject && config.isDomainManaged())
					dbData.put(config.getDomainDBKey(), domain.getObject());
				
				for(int i = 0; i < updatedAttributes.size(); i++)
				{
					AttributeConfig attributeConfig = config.getAttributeConfig(updatedAttributes.get(i));
					String attributeName = attributeConfig.getName();
					String attributeDBKey = attributeConfig.getDBKey();
					if(attributeDBKey != null)
					{
						dbData.put(attributeDBKey, get(attributeName).getObject());
					}
				}
				objectManager.getDataClient().putData(config.getCollection(), key, dbData);
				objectManager.signal(this);
			}
			else
			{
				throw new RedbackException("User does not have the right to update object " + config.getName());
			}
		}
	}
	
	public void afterSave() 
	{
		if(isDeleted != true && (updatedAttributes.size() > 0  ||  isNewObject == true) && canWrite)
		{
			try {
				executeScriptsForEvent("aftersave");
				if(isNewObject)
				{
					executeScriptsForEvent("aftercreate");
					isNewObject = false;
				}
			} catch(Exception e) {
				logger.severe(StringUtils.getStackTrace(e));
			}				
			updatedAttributes.clear();
		}		
	}
	
	public Object execute(String eventName) throws ScriptException, RedbackException
	{
		if(canExecute)
		{
			return executeScriptsForEvent(eventName);
		}
		else
		{
			throw new RedbackException("User does not have the right to execute functions in object " + config.getName());
		}
	}
	
	public DataMap getJSON(boolean addValidation, boolean addRelated) throws RedbackException
	{
		DataMap object = new DataMap();
		
		object.put("objectname", config.getName());
		object.put("uid", uid.getObject());
		object.put("domain", domain.getObject());

		DataMap dataNode = new DataMap();
		DataMap validatonNode = new DataMap();
		DataMap relatedNode = new DataMap();

		validatonNode.put("_candelete", this.canDelete());
		Iterator<String> it = config.getAttributeNames().iterator();
		while(it.hasNext())
		{
			AttributeConfig attributeConfig = config.getAttributeConfig(it.next());
			String attrName = attributeConfig.getName();
			//Value attrValue = new Value("allo");
			Value attrValue = get(attrName);

			DataMap attributeValidation = new DataMap();
			attributeValidation.put("editable", isEditable(attrName));
			attributeValidation.put("mandatory", isMandatory(attrName));
			attributeValidation.put("updatescript", attributeConfig.getScriptForEvent("onupdate") != null);

			if(attributeConfig.hasRelatedObject())
			{
				DataMap relatedObjectValidation = new DataMap();
				relatedObjectValidation.put("object",  attributeConfig.getRelatedObjectConfig().getObjectName());
				relatedObjectValidation.put("link",  attributeConfig.getRelatedObjectConfig().getLinkAttributeName());
				attributeValidation.put("related", relatedObjectValidation);
			}
			
			validatonNode.put(attrName, attributeValidation);
			
			if(addRelated  &&  attributeConfig.hasRelatedObject())
			{
				RedbackObject relatedObject = getRelated(attrName);
				if(relatedObject != null)
					relatedNode.put(attrName, relatedObject.getJSON(false, false));
			}

			dataNode.put(attrName, attrValue.getObject());
		}
		object.put("data", dataNode);
		
		if(addValidation)
			object.put("validation", validatonNode);
		if(addRelated)
			object.put("related", relatedNode);
			
		return object;
	}
	

	protected Object executeScriptsForEvent(String event) throws RedbackException
	{
		Function script  = config.getScriptForEvent(event);
		if(script != null)
			return executeScript(script, getObjectConfig().getName() + ":" + getUID().getString() + "." + event);
		else
			return null;
	}

	protected void executeAttributeScriptsForEvent(String attributeName, String event) throws RedbackException
	{
		Function script  = config.getAttributeConfig(attributeName).getScriptForEvent(event);
		if(script != null)
				executeScript(script, getObjectConfig().getName() + ":" + getUID().getString() + "." + attributeName + "." + event);
	}
	
	protected Object executeScript(Function script, String name) throws RedbackException
	{
		Object retVal = null;
		logger.finer("Start executing script : " + name);
		try
		{
			retVal = script.execute(scriptContext);
		} 
		catch (RedbackException e)
		{
			throw new RedbackException("Problem occurred executing script " + name, e);
		}		
		logger.finer("Finish executing script : " + name);
		return retVal;
	}
	
	public boolean filterApplies(DataMap objectFilter) {
		DataMap dataView = new DataMap();
		Iterator<String> it = data.keySet().iterator();
		while(it.hasNext()) {
			String key = it.next();
			dataView.put(key, data.get(key).getObject());
		}
		dataView.put("uid", uid.getObject());
		dataView.put("domain", domain.getObject());
		DataFilter filter = new DataFilter(objectFilter);
		return filter.apply(dataView);		
	}
	
	public String toString()
	{
		try
		{
			return getJSON(false, false).toString();
		}
		catch(RedbackException e)
		{
			return e.getMessage();
		}
	}

}
