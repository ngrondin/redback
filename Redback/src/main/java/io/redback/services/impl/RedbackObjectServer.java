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
import io.redback.managers.objectmanager.requests.AggregateRequest;
import io.redback.managers.objectmanager.requests.CreateRequest;
import io.redback.managers.objectmanager.requests.DeleteRequest;
import io.redback.managers.objectmanager.requests.ExecuteGlobalRequest;
import io.redback.managers.objectmanager.requests.ExecuteRequest;
import io.redback.managers.objectmanager.requests.GetRequest;
import io.redback.managers.objectmanager.requests.ListRelatedRequest;
import io.redback.managers.objectmanager.requests.ListRequest;
import io.redback.managers.objectmanager.requests.MultiRequest;
import io.redback.managers.objectmanager.requests.MultiResponse;
import io.redback.managers.objectmanager.requests.ObjectRequest;
import io.redback.managers.objectmanager.requests.UpdateRequest;
import io.redback.security.Session;
import io.redback.services.ObjectServer;

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

	public void start() {
		
	}	

	protected RedbackObject get(Session session, String objectName, String uid) throws RedbackException
	{
		RedbackObject object = null;
		objectManager.initiateCurrentTransaction();
		object = objectManager.getObject(session, objectName, uid);
		objectManager.commitCurrentTransaction();
		return object;
	}

	
	protected List<RedbackObject> list(Session session, String objectName, DataMap filter, String search, DataMap sort, boolean addRelated, int page, int pageSize) throws RedbackException 
	{
		List<RedbackObject> objects = null;
		if(filter == null)
			filter = new DataMap();
		
		objectManager.initiateCurrentTransaction();
		objects = objectManager.listObjects(session, objectName, filter, search, sort, addRelated, page, pageSize);
		objectManager.commitCurrentTransaction();
		return objects;	
	}

	protected List<RedbackObject> listRelated(Session session, String objectName, String uid, String attribute, DataMap filter, String search, DataMap sort, boolean addRelated, int page, int pageSize) throws RedbackException 
	{
		List<RedbackObject> objects = null;
		if(filter == null)
			filter = new DataMap();

		objectManager.initiateCurrentTransaction();
		objects = objectManager.listRelatedObjects(session, objectName, uid, attribute, filter, search, sort, addRelated, page, pageSize);
		objectManager.commitCurrentTransaction();
		return objects;	
	}

	protected RedbackObject update(Session session, String objectName, String uid, DataMap data) throws RedbackException
	{
		RedbackObject object = null;
		objectManager.initiateCurrentTransaction();
		object = objectManager.updateObject(session, objectName, uid, data);
		if(object != null) {
			objectManager.commitCurrentTransaction();
		} else {
			throw new RedbackException("No such object to update");
		}
		return object;
	}

	protected RedbackObject create(Session session, String objectName, String uid, String domain, DataMap data) throws RedbackException
	{
		RedbackObject object = null;
		objectManager.initiateCurrentTransaction();
		object = objectManager.createObject(session, objectName, uid, domain, data);
		objectManager.commitCurrentTransaction();
		return object;
	}

	protected void delete(Session session, String objectName, String uid) throws RedbackException {
		objectManager.initiateCurrentTransaction();
		objectManager.deleteObject(session, objectName, uid);
		objectManager.commitCurrentTransaction();
	}

	protected RedbackObject execute(Session session, String objectName, String uid, String function, DataMap param) throws RedbackException
	{
		RedbackObject object = null;
		objectManager.initiateCurrentTransaction();
		object = objectManager.executeFunction(session, objectName, uid, function, param);
		objectManager.commitCurrentTransaction();
		return object;
	}
	
	protected RedbackObject execute(Session session, String function, DataMap param) throws RedbackException {
		objectManager.initiateCurrentTransaction();
		objectManager.executeFunction(session, function, param);
		objectManager.commitCurrentTransaction();
		return null;
	}

	protected List<RedbackAggregate> aggregate(Session session, String objectName, DataMap filter, String searchText, DataList tuple, DataList metrics, DataMap sort, DataList base, boolean addRelated, int page, int pageSize) throws RedbackException {
		List<RedbackAggregate> aggregates = null;
		objectManager.initiateCurrentTransaction();
		aggregates = objectManager.aggregateObjects(session, objectName, filter, searchText, tuple, metrics, sort, base, addRelated, page, pageSize);
		objectManager.commitCurrentTransaction();
		return aggregates;	
	}

	protected MultiResponse multi(Session session, MultiRequest multiRequest) throws RedbackException {
		MultiResponse response = new MultiResponse();
		objectManager.initiateCurrentTransaction();
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
			} else if(request instanceof ExecuteRequest) {
				ExecuteRequest req = (ExecuteRequest)request;
				response.addResponse(key, objectManager.executeFunction(session, req.objectName, req.uid, req.function, req.param));
			} else if(request instanceof ExecuteGlobalRequest) {
				ExecuteGlobalRequest req = (ExecuteGlobalRequest)request;
				objectManager.executeFunction(session, req.function, req.param);
			} else if(request instanceof AggregateRequest) {
				AggregateRequest req = (AggregateRequest)request;
				response.addResponse(key, objectManager.aggregateObjects(session, req.objectName, req.filter, req.searchText, req.tuple, req.metrics, req.sort, req.base, req.addRelated, req.page, req.pageSize));
			}  
		}
		objectManager.commitCurrentTransaction();
		return response;
	}

}
