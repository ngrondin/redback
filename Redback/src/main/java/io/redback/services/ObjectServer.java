package io.redback.services;

import java.util.List;

import io.firebus.Firebus;
import io.firebus.Payload;
import io.firebus.StreamEndpoint;
import io.firebus.data.DataException;
import io.firebus.data.DataList;
import io.firebus.data.DataMap;
import io.firebus.information.ServiceInformation;
import io.firebus.information.StreamInformation;
import io.redback.exceptions.RedbackException;
import io.redback.managers.objectmanager.RedbackAggregate;
import io.redback.managers.objectmanager.RedbackObject;
import io.redback.security.Session;
import io.redback.services.common.AuthenticatedDualProvider;
import io.redback.utils.FunctionInfo;
import io.redback.utils.stream.DataStream;
import io.redback.utils.stream.SendingConverter;
import io.redback.utils.stream.SendingStreamPipeline;

public abstract class ObjectServer extends AuthenticatedDualProvider 
{
	
	public ObjectServer(String n, DataMap c, Firebus f) {
		super(n, c, f);
	}

	public Payload redbackAuthenticatedService(Session session, Payload payload) throws RedbackException {
		try {
			DataMap requestData = payload.getDataMap();
			startTransaction(session);
			DataMap responseData = processServiceRequest(session, requestData);
			commitTransaction(session);
			Payload response = new Payload(responseData);
			response.metadata.put("mime", "application/json");
			return response;
		} catch(DataException e) {
			throw new RedbackException("Error in object server", e);
		}
	}

	public Payload redbackUnauthenticatedService(Session session, Payload payload) throws RedbackException {
		throw new RedbackException("All requests need to be authenticated");
	}
	
	public Payload redbackAcceptAuthenticatedStream(Session session, Payload payload, StreamEndpoint streamEndpoint) throws RedbackException {
		try {
			DataMap requestData = payload.getDataMap();
			startTransaction(session);
			processStreamRequest(session, requestData, streamEndpoint);
			commitTransaction(session);
			return null;
		} catch(DataException e) {
			throw new RedbackException("Error in object server", e);
		}
	}

	public Payload redbackAcceptUnauthenticatedStream(Session session, Payload payload, StreamEndpoint streamEndpoint) throws RedbackException {
		throw new RedbackException("All requests need to be authenticated");
	}

	public ServiceInformation getServiceInformation()
	{
		return null;
	}

	public int getStreamIdleTimeout() {
		return 10000;
	}

	public StreamInformation getStreamInformation() {
		return null;
	}
	
	protected DataMap processServiceRequest(Session session, DataMap requestData) throws RedbackException {
		String action = requestData.getString("action");
		DataMap options = requestData.getObject("options");
		boolean addValidation = options != null && options.containsKey("addvalidation") ? options.getBoolean("addvalidation") : false;
		boolean addRelated = options != null && options.containsKey("addrelated") ? options.getBoolean("addrelated") : false;
		if(action != null)
		{
			if(action.equals("get") && !requestData.containsKey("attribute"))
			{
				String objectName = requestData.getString("object");
				String uid = requestData.getString("uid");
				if(objectName != null && uid != null) {
					RedbackObject resp = get(session, objectName, uid);
					return resp.getDataMap(addValidation, addRelated, true);
				} else {
					throw new RedbackException("A 'get' action requires a 'uid' attribute");
				}
			}
			else if(action.equals("getrelated") || (action.equals("get") && requestData.containsKey("attribute"))) 
			{
				String objectName = requestData.getString("object");
				String uid = requestData.getString("uid");
				String attribute = requestData.getString("attribute");
				if(objectName != null && uid != null) {
					RedbackObject resp = getRelated(session, objectName, uid, attribute);
					return resp != null ? resp.getDataMap(addValidation, addRelated, true) : null;
				} else {
					throw new RedbackException("A 'get' action requires a 'uid' attribute");
				}				
			}
			else if(action.equals("list") && !requestData.containsKey("uid"))
			{
				String objectName = requestData.getString("object");
				DataMap filter = requestData.getObject("filter");
				String searchText = requestData.getString("search");
				DataMap sort = requestData.getObject("sort");
				int page = requestData.containsKey("page") ? requestData.getNumber("page").intValue() : 0;
				int pageSize = requestData.containsKey("pagesize") ? requestData.getNumber("pagesize").intValue() : 50;
				List<RedbackObject> objects = list(session, objectName, filter, searchText, sort, addRelated, page, pageSize);
				DataMap responseData = new DataMap();
				DataList respList = new DataList();
				for(RedbackObject object: (List<RedbackObject>)objects)
					respList.add(object.getDataMap(addValidation, addRelated, true));
				responseData.put("list", respList);
				return responseData;
			}
			else if(action.equals("listrelated") || (action.equals("list") && requestData.containsKey("uid")))
			{
				String objectName = requestData.getString("object");
				String uid = requestData.getString("uid");
				String attribute = requestData.getString("attribute");
				DataMap filter = requestData.getObject("filter");
				String searchText = requestData.getString("search");
				DataMap sort = requestData.getObject("sort");
				int page = requestData.containsKey("page") ? requestData.getNumber("page").intValue() : 0;
				int pageSize = requestData.containsKey("pagesize") ? requestData.getNumber("pagesize").intValue() : 50;
				if(uid == null || attribute == null)
					throw new RedbackException("A 'list' action requires either a filter, a search or a uid-attribute pair");
				List<RedbackObject> objects = listRelated(session, objectName, uid, attribute, filter, searchText, sort, addRelated, page, pageSize);
				DataMap responseData = new DataMap();
				DataList respList = new DataList();
				for(RedbackObject object: (List<RedbackObject>)objects)
					respList.add(object.getDataMap(addValidation, addRelated, true));
				responseData.put("list", respList);
				return responseData;
			}					
			else if(action.equals("update"))
			{
				String objectName = requestData.getString("object");
				String uid = requestData.getString("uid");
				DataMap updateData = requestData.getObject("data");
				RedbackObject object = update(session, objectName, uid, updateData);
				return object.getDataMap(addValidation, addRelated, true);
			}
			else if(action.equals("create"))
			{
				String objectName = requestData.getString("object");
				String uid = requestData.getString("uid");
				String domain = requestData.getString("domain");
				DataMap initialData = requestData.getObject("data");
				RedbackObject object = create(session, objectName, uid, domain, initialData);
				return object.getDataMap(addValidation, addRelated, true);
			}
			else if(action.equals("delete"))
			{
				String objectName = requestData.getString("object");
				String uid = requestData.getString("uid");
				delete(session, objectName, uid);
				return new DataMap("result", "ok");
			}					
			else if(action.equals("execute"))
			{
				if(requestData.containsKey("object")) {
					String objectName = requestData.getString("object");
					String uid = requestData.getString("uid");
					String function = requestData.getString("function");
					DataMap param = requestData.containsKey("param") ? requestData.getObject("param") : requestData.containsKey("data") ? requestData.getObject("data") : null;
					RedbackObject object = execute(session, objectName, uid, function, param);
					return object.getDataMap(addValidation, addRelated, true);
				} else {
					String function = requestData.getString("function");
					DataMap param = requestData.containsKey("param") ? requestData.getObject("param") : requestData.containsKey("data") ? requestData.getObject("data") : null;
					Object ret = execute(session, function, param);
					DataMap resp = new DataMap("result", "ok");
					if(ret != null) 
						resp.put("data", ret);
					return resp;
				}
			}
			else if(action.equals("getpack"))
			{
				String name = requestData.getString("name");
				List<RedbackObject> objects = getPack(session, name);
				DataList respList = new DataList();
				for(RedbackObject object: objects)
					respList.add(object.getDataMap(addValidation, addRelated, true));
				return new DataMap("list", respList);
			}
			else if(action.equals("listfunctions") || action.equals("listscripts"))
			{
				String category = requestData.getString("category");
				List<FunctionInfo> list = listFunctions(session, category);
				DataList respList = new DataList();
				for(FunctionInfo fi: list)
					respList.add(new DataMap("name", fi.name, "description", fi.description, "timeout", fi.timeout));
				return new DataMap("list", respList);
			}
			else if(action.equals("aggregate"))
			{
				String objectName = requestData.getString("object");
				DataMap filter = requestData.getObject("filter");
				String searchText = requestData.getString("search");
				DataList tuple = requestData.getList("tuple");
				DataList metrics = requestData.getList("metrics");
				DataMap sort = requestData.getObject("sort");
				DataList base = requestData.getList("base");
				int page = requestData.containsKey("page") ? requestData.getNumber("page").intValue() : 0;
				int pageSize = requestData.containsKey("pagesize") ? requestData.getNumber("pagesize").intValue() : 50;
				List<RedbackAggregate> aggregates = aggregate(session, objectName, filter, searchText, tuple, metrics, sort, base, addRelated, page, pageSize);
				DataMap responseData = new DataMap();
				DataList respList = new DataList();
				for(RedbackAggregate agg: aggregates)
					respList.add(agg.getDataMap(addRelated));
				responseData.put("list", respList);
				return responseData;			}
			else if(action.equals("multi")) 
			{
				DataList list = requestData.getList("multi");
				DataMap respMap = new DataMap();
				for(int i = 0; i < list.size(); i++) {
					DataMap sub = list.getObject(i);
					String key = sub.getString("key");
					DataMap subResp = processServiceRequest(session, sub);
					respMap.put(key, subResp);
				}
				return respMap;
			}
			else if(action.equals("noop")) 
			{
				return new DataMap("result", "ok");
			}
			else
			{
				throw new RedbackException("The '" + action + "' action is not valid as an object request");
			}
		}
		else
		{
			throw new RedbackException("Requests must have at least an 'action' attribute");
		}	
	}
	
	protected void processStreamRequest(Session session, DataMap requestData, StreamEndpoint streamEndpoint) throws RedbackException {
		String action = requestData.getString("action");
		DataMap options = requestData.getObject("options");
		boolean addValidation = options != null && options.containsKey("addvalidation") ? options.getBoolean("addvalidation") : false;
		if(options != null && options.containsKey("addrelated") && options.getBoolean("addrelated") == true)
			throw new RedbackException("Cannot add related objects in a stream");
		boolean addRelated = options != null && options.containsKey("addrelated") ? options.getBoolean("addrelated") : false;
		if(action != null)
		{
			if(action.equals("list") && !requestData.containsKey("uid")) {
				String objectName = requestData.getString("object");
				DataMap filter = requestData.getObject("filter");
				String searchText = requestData.getString("search");
				DataMap sort = requestData.getObject("sort");
				int chunkSize = requestData.containsKey("chunksize") ? requestData.getNumber("chunksize").intValue() : -1;
				int advance = requestData.containsKey("advance") ? requestData.getNumber("advance").intValue() : -1;
				SendingStreamPipeline<RedbackObject> ssp = new SendingStreamPipeline<RedbackObject>(streamEndpoint, chunkSize, new SendingConverter<RedbackObject>() {
					public Payload convert(List<RedbackObject> list) throws DataException, RedbackException {
						DataList dataList = new DataList();							
						for(RedbackObject rbo: list)
							dataList.add(rbo.getDataMap(addValidation, addRelated, true));
						return new Payload(new DataMap("result", dataList));						
					}
				});
				streamList(session, objectName, filter, searchText, sort, chunkSize, advance, ssp.getDataStream());
			} else if(action.equals("listrelated") || (action.equals("list") && requestData.containsKey("uid"))) {
				throw new RedbackException("Not yet implemented");
			}
		}
		else
		{
			throw new RedbackException("Requests must havea valid 'action' attribute");
		}	
	}
	
	protected abstract void startTransaction(Session session) throws RedbackException;
	
	protected abstract void commitTransaction(Session session) throws RedbackException;
		
	protected abstract RedbackObject get(Session session, String objectName, String uid) throws RedbackException;

	protected abstract RedbackObject getRelated(Session session, String objectName, String uid, String attribute) throws RedbackException;

	protected abstract List<RedbackObject> list(Session session, String objectName, DataMap filter, String search, DataMap sort, boolean addRelated, int page, int pageSize) throws RedbackException;

	protected abstract void streamList(Session session, String objectName, DataMap filter, String search, DataMap sort, int chunkSize, int advance, DataStream<RedbackObject> stream) throws RedbackException;

	protected abstract List<RedbackObject> listRelated(Session session, String objectName, String uid, String attribute, DataMap filter, String search, DataMap sort, boolean addRelated, int page, int pageSize) throws RedbackException;

	protected abstract RedbackObject update(Session session, String objectName, String uid, DataMap data) throws RedbackException;

	protected abstract RedbackObject create(Session session, String objectName, String uid, String domain, DataMap data) throws RedbackException;

	protected abstract void delete(Session session, String objectName, String uid) throws RedbackException;

	protected abstract RedbackObject execute(Session session, String objectName, String uid, String function, DataMap param) throws RedbackException;

	protected abstract Object execute(Session session, String function, DataMap param) throws RedbackException;

	protected abstract List<RedbackObject> getPack(Session session, String name) throws RedbackException;

	protected abstract List<FunctionInfo> listFunctions(Session session, String category) throws RedbackException;
	
	protected abstract List<RedbackAggregate> aggregate(Session session, String objectName, DataMap filter, String searchText, DataList tuple, DataList metrics, DataMap sort, DataList base, boolean addRelated, int page, int pageSize) throws RedbackException;

}
