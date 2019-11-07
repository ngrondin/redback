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
		try {
			RedbackObject object = objectManager.getObject(session, objectName, uid);
			objectManager.commitCurrentTransaction();
			return object;
		} catch (ScriptException e) {
			throw new RedbackException("Error getting object", e);
		}
	}

	protected List<RedbackObject> list(Session session, String objectName, DataMap filter, String search, String uid, String attribute, boolean addRelated) throws RedbackException
	{
		try {
			List<RedbackObject> objects = new ArrayList<RedbackObject>();
			if(filter == null)
				filter = new DataMap();
			
			if(uid != null  &&  attribute != null)
				objects = objectManager.getObjectList(session, objectName, uid, attribute, filter, search);
			else
				objects = objectManager.getObjectList(session, objectName, filter, search);
			objectManager.commitCurrentTransaction();
	
			if(addRelated)
				objectManager.addRelatedBulk(session, objects);
			
			return objects;
		} catch(ScriptException e) {
			throw new RedbackException("Error listing objects", e);
		}
	}

	protected RedbackObject update(Session session, String objectName, String uid, DataMap data) throws RedbackException
	{
		try {
			RedbackObject object = objectManager.updateObject(session, objectName, uid, data);
			if(object != null)
				objectManager.commitCurrentTransaction();
			else
				throw new RedbackException("No such object to update");
			return object;
		} catch(ScriptException e) {
			throw new RedbackException("Error listing objects", e);
		}
		
	}

	protected RedbackObject create(Session session, String objectName, DataMap data) throws RedbackException
	{
		try {
			RedbackObject object = objectManager.createObject(session, objectName, data);
			objectManager.commitCurrentTransaction();
			return object;
		} catch(ScriptException e) {
			throw new RedbackException("Error creating object", e);
		}
	}

	protected RedbackObject execute(Session session, String objectName, String uid, String function, DataMap data) throws RedbackException
	{
		try {
			RedbackObject object = objectManager.executeFunction(session, objectName, uid, function, data);
			objectManager.commitCurrentTransaction();
			return object;
		} catch(ScriptException e) {
			throw new RedbackException("Error executing function on object", e);
			
		}
	}

	protected void refreshConfigs() 
	{
		objectManager.refreshAllConfigs();
	}
}
