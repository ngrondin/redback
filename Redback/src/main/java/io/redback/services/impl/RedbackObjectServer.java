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

public class RedbackObjectServer extends ObjectServer
{
	protected ObjectManager objectManager;
	protected HashMap<String, ObjectConfig> objectConfigs;

	public RedbackObjectServer(String n, DataMap c, Firebus f) throws RedbackException {
		super(n, c, f);
		objectManager = new ObjectManager(n, config, firebus);
	}
	
	public void configure() {
		objectManager.refreshAllConfigs();
	}	
	

	protected void startTransaction(Session session) throws RedbackException {
		objectManager.initiateCurrentTransaction(session);
	}

	protected void commitTransaction(Session session) throws RedbackException {
		objectManager.commitCurrentTransaction(session);
	}
	

	protected RedbackObject get(Session session, String objectName, String uid) throws RedbackException
	{
		RedbackObject object = null;
		object = objectManager.getObject(session, objectName, uid);
		return object;
	}

	
	protected List<RedbackObject> list(Session session, String objectName, DataMap filter, String search, DataMap sort, boolean addRelated, int page, int pageSize) throws RedbackException 
	{
		List<RedbackObject> objects = null;
		if(filter == null)
			filter = new DataMap();
		
		objects = objectManager.listObjects(session, objectName, filter, search, sort, addRelated, page, pageSize);
		return objects;	
	}

	protected List<RedbackObject> listRelated(Session session, String objectName, String uid, String attribute, DataMap filter, String search, DataMap sort, boolean addRelated, int page, int pageSize) throws RedbackException 
	{
		List<RedbackObject> objects = null;
		if(filter == null)
			filter = new DataMap();

		objects = objectManager.listRelatedObjects(session, objectName, uid, attribute, filter, search, sort, addRelated, page, pageSize);
		return objects;	
	}

	protected RedbackObject update(Session session, String objectName, String uid, DataMap data) throws RedbackException
	{
		RedbackObject object = null;
		object = objectManager.updateObject(session, objectName, uid, data);
		return object;
	}

	protected RedbackObject create(Session session, String objectName, String uid, String domain, DataMap data) throws RedbackException
	{
		RedbackObject object = null;
		object = objectManager.createObject(session, objectName, uid, domain, data);
		return object;
	}

	protected void delete(Session session, String objectName, String uid) throws RedbackException {
		objectManager.deleteObject(session, objectName, uid);
	}

	protected RedbackObject execute(Session session, String objectName, String uid, String function, DataMap param) throws RedbackException
	{
		RedbackObject object = null;
		object = objectManager.executeObjectFunction(session, objectName, uid, function, param);
		return object;
	}
	
	protected Object execute(Session session, String function, DataMap param) throws RedbackException {
		return objectManager.executeFunction(session, function, param);
	}
	
	protected List<FunctionInfo> listFunctions(Session session, String category) throws RedbackException {
		return objectManager.listFunctions(session, category);
	}

	protected List<RedbackAggregate> aggregate(Session session, String objectName, DataMap filter, String searchText, DataList tuple, DataList metrics, DataMap sort, DataList base, boolean addRelated, int page, int pageSize) throws RedbackException {
		List<RedbackAggregate> aggregates = null;
		aggregates = objectManager.aggregateObjects(session, objectName, filter, searchText, tuple, metrics, sort, base, addRelated, page, pageSize);
		return aggregates;	
	}

	/*protected MultiResponse multi(Session session, MultiRequest multiRequest) throws RedbackException {
		MultiResponse response = new MultiResponse();
		objectManager.initiateCurrentTransaction(session);
		for(String key: multiRequest.getKeys()) {
			ObjectRequest request = multiRequest.getRequest(key);
			if(request instanceof GetRequest) {
				GetRequest req = (GetRequest)request;
				response.addResponse(key, objectManager.getObject(session, req.objectName, req.uid));
			} else if(request instanceof ListRequest) {
				ListRequest req = (ListRequest)request;
				response.addResponse(key, objectManager.listObjects(session, req.objectName, req.filter, req.searchText, req.sort, req.addRelated, req.page, req.pageSize));
			} else if(request instanceof ListRelatedRequest) {
				ListRelatedRequest req = (ListRelatedRequest)request;
				response.addResponse(key, objectManager.listRelatedObjects(session, req.objectName, req.uid, req.attribute, req.filter, req.searchText, req.sort, req.addRelated, req.page, req.pageSize));
			} else if(request instanceof CreateRequest) {
				CreateRequest req = (CreateRequest)request;
				response.addResponse(key, objectManager.createObject(session, req.objectName, req.uid, req.domain, req.initialData));
			} else if(request instanceof UpdateRequest) {
				UpdateRequest req = (UpdateRequest)request;
				response.addResponse(key, objectManager.updateObject(session, req.objectName, req.uid, req.updateData));
			} else if(request instanceof DeleteRequest) {
				DeleteRequest req = (DeleteRequest)request;
				objectManager.deleteObject(session, req.objectName, req.uid);
			} else if(request instanceof ExecuteObjectRequest) {
				ExecuteObjectRequest req = (ExecuteObjectRequest)request;
				response.addResponse(key, objectManager.executeObjectFunction(session, req.objectName, req.uid, req.function, req.param));
			} else if(request instanceof ExecuteRequest) {
				ExecuteRequest req = (ExecuteRequest)request;
				objectManager.executeFunction(session, req.function, req.domain, req.param);
			} else if(request instanceof AggregateRequest) {
				AggregateRequest req = (AggregateRequest)request;
				response.addResponse(key, objectManager.aggregateObjects(session, req.objectName, req.filter, req.searchText, req.tuple, req.metrics, req.sort, req.base, req.addRelated, req.page, req.pageSize));
			}  
		}
		objectManager.commitCurrentTransaction(session);
		return response;
	}*/


}
