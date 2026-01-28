package io.redback.services.impl;

import java.util.HashMap;
import java.util.List;

import io.firebus.Firebus;
import io.firebus.data.DataList;
import io.firebus.data.DataMap;
import io.redback.exceptions.RedbackException;
import io.redback.managers.objectmanager.ObjectConfig;
import io.redback.managers.objectmanager.ObjectManager;
import io.redback.managers.objectmanager.RedbackAggregate;
import io.redback.managers.objectmanager.RedbackObject;
import io.redback.security.Session;
import io.redback.services.ObjectServer;
import io.redback.utils.FunctionInfo;
import io.redback.utils.stream.DataStream;

public class RedbackObjectServer extends ObjectServer
{
	protected ObjectManager objectManager;
	protected HashMap<String, ObjectConfig> objectConfigs;

	public RedbackObjectServer(String n, DataMap c, Firebus f) throws RedbackException {
		super(n, c, f);
		objectManager = new ObjectManager(n, config, firebus);
	}
	
	public void configure() {
		super.configure();
		objectManager.refreshAllConfigs();
	}	
	

	protected void startTransaction(Session session, boolean store) throws RedbackException {
		objectManager.initiateCurrentTransaction(session, store);
	}

	protected void commitTransaction(Session session) throws RedbackException {
		objectManager.commitCurrentTransaction(session);
	}
	
	protected RedbackObject get(Session session, String objectName, String uid) throws RedbackException {
		return objectManager.getObject(session, objectName, uid);
	}
	
	protected RedbackObject getRelated(Session session, String objectName, String uid, String attribute) throws RedbackException {
		return objectManager.getRelatedObject(session, objectName, uid, attribute);
	}

	protected List<RedbackObject> list(Session session, String objectName, DataMap filter, String search, DataMap sort, boolean addRelated, int page, int pageSize) throws RedbackException {
		return objectManager.listObjects(session, objectName, filter != null ? filter : new DataMap(), search, sort, addRelated, page, pageSize);
	}

	protected void streamList(Session session, String objectName, DataMap filter, String search, DataMap sort, int chunkSize, int advance, DataStream<RedbackObject> stream) throws RedbackException {
		objectManager.streamObjects(session, objectName, filter != null ? filter : new DataMap(), search, sort, chunkSize, advance, stream);		
	}

	protected List<RedbackObject> listRelated(Session session, String objectName, String uid, String attribute, DataMap filter, String search, DataMap sort, boolean addRelated, int page, int pageSize) throws RedbackException {
		return objectManager.listRelatedObjects(session, objectName, uid, attribute, filter != null ? filter : new DataMap(), search, sort, addRelated, page, pageSize);
	}

	protected RedbackObject update(Session session, String objectName, String uid, DataMap data) throws RedbackException {
		return objectManager.updateObject(session, objectName, uid, data);
	}

	protected RedbackObject create(Session session, String objectName, String uid, String domain, DataMap data) throws RedbackException {
		return objectManager.createObject(session, objectName, uid, domain, data);
	}

	protected void delete(Session session, String objectName, String uid) throws RedbackException {
		objectManager.deleteObject(session, objectName, uid);
	}
	
	protected long count(Session session, String objectName, DataMap filter, String search) throws RedbackException {
		return objectManager.countObjects(session, objectName, filter, search);
	}

	protected RedbackObject execute(Session session, String objectName, String uid, String function, DataMap param) throws RedbackException {
		return objectManager.executeObjectFunction(session, objectName, uid, function, param);
	}
	
	protected Object execute(Session session, String function, DataMap param) throws RedbackException {
		return objectManager.executeFunction(session, function, param);
	}

	protected void streamPack(Session session, String name, DataStream<RedbackObject> stream) throws RedbackException {
		objectManager.streamPack(session, name, stream);
	}

	protected List<FunctionInfo> listFunctions(Session session, String category) throws RedbackException {
		return objectManager.listFunctions(session, category);
	}

	protected List<RedbackAggregate> aggregate(Session session, String objectName, DataMap filter, String searchText, DataList tuple, DataList metrics, DataMap sort, DataList base, boolean addRelated, int page, int pageSize) throws RedbackException {
		return objectManager.aggregateObjects(session, objectName, filter, searchText, tuple, metrics, sort, base, addRelated, page, pageSize);
	}
}
