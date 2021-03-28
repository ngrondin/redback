package io.redback.services.impl;

import java.util.HashMap;
import java.util.List;

import javax.script.ScriptException;

import io.firebus.Firebus;
import io.firebus.utils.DataList;
import io.firebus.utils.DataMap;
import io.redback.RedbackException;
import io.redback.managers.objectmanager.ObjectConfig;
import io.redback.managers.objectmanager.ObjectManager;
import io.redback.managers.objectmanager.RedbackAggregate;
import io.redback.managers.objectmanager.RedbackObject;
import io.redback.security.Session;
import io.redback.services.ObjectServer;

public class RedbackObjectServer extends ObjectServer
{
	protected ObjectManager objectManager;
	protected HashMap<String, ObjectConfig> objectConfigs;

	public RedbackObjectServer(String n, DataMap c, Firebus f) {
		super(n, c, f);
		objectManager = new ObjectManager(n, config, firebus);
	}

	protected RedbackObject get(Session session, String objectName, String uid) throws RedbackException
	{
		RedbackObject object = null;
		try {
			objectManager.initiateCurrentTransaction();
			object = objectManager.getObject(session, objectName, uid);
			objectManager.commitCurrentTransaction();
		} catch (ScriptException e) {
			throw new RedbackException("Error getting object", e);
		}
		return object;
	}

	
	protected List<RedbackObject> list(Session session, String objectName, DataMap filter, String search, DataMap sort, boolean addRelated, int page, int pageSize) throws RedbackException 
	{
		List<RedbackObject> objects = null;
		try {
			if(filter == null)
				filter = new DataMap();
			
			objectManager.initiateCurrentTransaction();
			objects = objectManager.listObjects(session, objectName, filter, search, sort, addRelated, page, pageSize);
			objectManager.commitCurrentTransaction();
		} catch(ScriptException e) {
			throw new RedbackException("Error listing objects", e);
		}
		return objects;	
	}

	protected List<RedbackObject> listRelated(Session session, String objectName, String uid, String attribute, DataMap filter, String search, DataMap sort, boolean addRelated, int page, int pageSize) throws RedbackException 
	{
		List<RedbackObject> objects = null;
		try {
			if(filter == null)
				filter = new DataMap();

			objectManager.initiateCurrentTransaction();
			objects = objectManager.listRelatedObjects(session, objectName, uid, attribute, filter, search, sort, addRelated, page, pageSize);
			objectManager.commitCurrentTransaction();
		} catch(ScriptException e) {
			throw new RedbackException("Error listing objects", e);
		}
		return objects;	
	}

	protected RedbackObject update(Session session, String objectName, String uid, DataMap data) throws RedbackException
	{
		RedbackObject object = null;
		try {
			objectManager.initiateCurrentTransaction();
			object = objectManager.updateObject(session, objectName, uid, data);
			if(object != null) {
				objectManager.commitCurrentTransaction();
			} else {
				throw new RedbackException("No such object to update");
			}
		} catch(ScriptException e) {
			throw new RedbackException("Error listing objects", e);
		}
		return object;
	}

	protected RedbackObject create(Session session, String objectName, String uid, String domain, DataMap data) throws RedbackException
	{
		RedbackObject object = null;
		try {
			objectManager.initiateCurrentTransaction();
			object = objectManager.createObject(session, objectName, uid, domain, data);
			objectManager.commitCurrentTransaction();
		} catch(ScriptException e) {
			throw new RedbackException("Error creating object", e);
		}
		return object;
	}

	protected void delete(Session session, String objectName, String uid) throws RedbackException {
		try {
			objectManager.initiateCurrentTransaction();
			objectManager.deleteObject(session, objectName, uid);
			objectManager.commitCurrentTransaction();
		} catch(ScriptException e) {
			throw new RedbackException("Error creating object", e);
		}
	}

	protected RedbackObject execute(Session session, String objectName, String uid, String function, DataMap param) throws RedbackException
	{
		RedbackObject object = null;
		try {
			objectManager.initiateCurrentTransaction();
			object = objectManager.executeFunction(session, objectName, uid, function, param);
			objectManager.commitCurrentTransaction();
		} catch(ScriptException e) {
			throw new RedbackException("Error executing function on object", e);
		}
		return object;
	}
	
	protected RedbackObject execute(Session session, String function, DataMap param) throws RedbackException {
		try {
			objectManager.initiateCurrentTransaction();
			objectManager.executeFunction(session, function, param);
			objectManager.commitCurrentTransaction();
		} catch(ScriptException e) {
			throw new RedbackException("Error executing function on object", e);
		}

		return null;
	}

	public void clearCaches()
	{
		objectManager.refreshAllConfigs();
	}

	protected List<RedbackAggregate> aggregate(Session session, String objectName, DataMap filter, String searchText, DataList tuple, DataList metrics, DataMap sort, boolean addRelated, int page, int pageSize) throws RedbackException {
		List<RedbackAggregate> aggregates = null;
		try {
			objectManager.initiateCurrentTransaction();
			aggregates = objectManager.aggregateObjects(session, objectName, filter, searchText, tuple, metrics, sort, addRelated, page, pageSize);
			objectManager.commitCurrentTransaction();
		} catch(ScriptException e) {
			throw new RedbackException("Error listing objects", e);
		}
		return aggregates;	
	}

}
