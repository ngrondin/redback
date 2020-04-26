package io.redback.services.impl;

import java.util.ArrayList;
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
		objectManager = new ObjectManager(firebus, config);
	}

	protected RedbackObject get(Session session, String objectName, String uid) throws RedbackException
	{
		RedbackObject object = null;
		try {
			objectManager.initiateCurrentTransaction();
			object = objectManager.getObject(session, objectName, uid);
			objectManager.commitCurrentTransaction();
		} catch (ScriptException e) {
			error("Error getting object", e);
		}
		return object;
	}

	
	protected List<RedbackObject> list(Session session, String objectName, DataMap filter, String search, int page, boolean addRelated) throws RedbackException 
	{
		List<RedbackObject> objects = null;
		try {
			if(filter == null)
				filter = new DataMap();
			
			objectManager.initiateCurrentTransaction();
			objects = objectManager.listObjects(session, objectName, filter, search, addRelated, page);
			objectManager.commitCurrentTransaction();
		} catch(ScriptException e) {
			error("Error listing objects", e);
		}
		return objects;	
	}

	protected List<RedbackObject> listRelated(Session session, String objectName, String uid, String attribute, DataMap filter, String search, int page, boolean addRelated) throws RedbackException 
	{
		List<RedbackObject> objects = null;
		try {
			if(filter == null)
				filter = new DataMap();

			objectManager.initiateCurrentTransaction();
			objects = objectManager.listRelatedObjects(session, objectName, uid, attribute, filter, search, addRelated, page);
			objectManager.commitCurrentTransaction();
		} catch(ScriptException e) {
			error("Error listing objects", e);
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
				error("No such object to update");
			}
		} catch(ScriptException e) {
			error("Error listing objects", e);
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
			error("Error creating object", e);
		}
		return object;
	}

	protected RedbackObject execute(Session session, String objectName, String uid, String function, DataMap data) throws RedbackException
	{
		RedbackObject object = null;
		try {
			objectManager.initiateCurrentTransaction();
			object = objectManager.executeFunction(session, objectName, uid, function, data);
			objectManager.commitCurrentTransaction();
		} catch(ScriptException e) {
			error("Error executing function on object", e);
		}
		return object;
	}
	
	public void clearCaches()
	{
		objectManager.refreshAllConfigs();
	}

	protected List<RedbackAggregate> aggregate(Session session, String objectName, DataMap filter, DataList tuple, DataList metrics, boolean addRelated) throws RedbackException {
		List<RedbackAggregate> aggregates = null;
		try {
			objectManager.initiateCurrentTransaction();
			aggregates = objectManager.aggregateObjects(session, objectName, filter, tuple, metrics, addRelated);
			objectManager.commitCurrentTransaction();
		} catch(ScriptException e) {
			error("Error listing objects", e);
		}
		return aggregates;	
	}

}
