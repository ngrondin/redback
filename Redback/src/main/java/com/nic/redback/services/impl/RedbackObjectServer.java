package com.nic.redback.services.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.script.ScriptException;

import com.nic.firebus.Firebus;
import com.nic.firebus.utils.DataMap;
import com.nic.redback.RedbackException;
import com.nic.redback.managers.objectmanager.ObjectConfig;
import com.nic.redback.managers.objectmanager.ObjectManager;
import com.nic.redback.managers.objectmanager.RedbackObject;
import com.nic.redback.security.Session;
import com.nic.redback.services.ObjectServer;

public class RedbackObjectServer extends ObjectServer
{
	protected ObjectManager objectManager;
	protected HashMap<String, ObjectConfig> objectConfigs;

	public RedbackObjectServer(DataMap c, Firebus f) {
		super(c, f);
		objectManager = new ObjectManager(firebus, config);
	}

	protected RedbackObject get(Session session, String objectName, String uid) throws RedbackException
	{
		RedbackObject object = null;
		try {
			object = objectManager.getObject(session, objectName, uid);
			objectManager.commitCurrentTransaction();
		} catch (ScriptException e) {
			error("Error getting object", e);
		}
		return object;
	}

	
	protected List<RedbackObject> list(Session session, String objectName, DataMap filter, int page, boolean addRelated) throws RedbackException 
	{
		List<RedbackObject> objects = null;
		try {
			objects = new ArrayList<RedbackObject>();
			if(filter == null)
				filter = new DataMap();
			
			objects = objectManager.listObjects(session, objectName, filter, null, page);
			objectManager.commitCurrentTransaction();
	
			if(addRelated)
				objectManager.addRelatedBulk(session, objects);
			
			return objects;
		} catch(ScriptException e) {
			error("Error listing objects", e);
		}
		return objects;	}

	protected List<RedbackObject> list(Session session, String objectName, String search, int page, boolean addRelated) throws RedbackException 
	{
		List<RedbackObject> objects = null;
		try {
			objects = new ArrayList<RedbackObject>();
			objects = objectManager.listObjects(session, objectName, null, search, page);
			objectManager.commitCurrentTransaction();
	
			if(addRelated)
				objectManager.addRelatedBulk(session, objects);
			
			return objects;
		} catch(ScriptException e) {
			error("Error listing objects", e);
		}
		return objects;	}

	protected List<RedbackObject> list(Session session, String objectName, String uid, String attribute, DataMap filter, int page, boolean addRelated) throws RedbackException 
	{
		List<RedbackObject> objects = null;
		try {
			objects = new ArrayList<RedbackObject>();
			if(filter == null)
				filter = new DataMap();
			
			objects = objectManager.listObjects(session, objectName, uid, attribute, filter, null, page);
			objectManager.commitCurrentTransaction();
	
			if(addRelated)
				objectManager.addRelatedBulk(session, objects);
			
			return objects;
		} catch(ScriptException e) {
			error("Error listing objects", e);
		}
		return objects;	}

	protected List<RedbackObject> list(Session session, String objectName, String uid, String attribute, String search, int page, boolean addRelated) throws RedbackException 
	{
		List<RedbackObject> objects = null;
		try {
			objects = new ArrayList<RedbackObject>();
			objects = objectManager.listObjects(session, objectName, uid, attribute, null, search);
			objectManager.commitCurrentTransaction();
	
			if(addRelated)
				objectManager.addRelatedBulk(session, objects);
			
			return objects;
		} catch(ScriptException e) {
			error("Error listing objects", e);
		}
		return objects;	}


	protected RedbackObject update(Session session, String objectName, String uid, DataMap data) throws RedbackException
	{
		RedbackObject object = null;
		try {
			object = objectManager.updateObject(session, objectName, uid, data);
			if(object != null)
				objectManager.commitCurrentTransaction();
			else
				error("No such object to update");
		} catch(ScriptException e) {
			error("Error listing objects", e);
		}
		return object;
	}

	protected RedbackObject create(Session session, String objectName, String domain, DataMap data) throws RedbackException
	{
		RedbackObject object = null;
		try {
			object = objectManager.createObject(session, objectName, domain, data);
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
			object = objectManager.executeFunction(session, objectName, uid, function, data);
			objectManager.commitCurrentTransaction();
		} catch(ScriptException e) {
			error("Error executing function on object", e);
		}
		return object;
	}

	protected void refreshConfigs() 
	{
		objectManager.refreshAllConfigs();
	}

}
