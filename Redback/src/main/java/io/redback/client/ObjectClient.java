package io.redback.client;

import java.util.ArrayList;
import java.util.List;

import io.firebus.Firebus;
import io.firebus.Payload;
import io.firebus.StreamEndpoint;
import io.firebus.data.DataEntity;
import io.firebus.data.DataException;
import io.firebus.data.DataList;
import io.firebus.data.DataMap;
import io.redback.exceptions.RedbackException;
import io.redback.managers.objectmanager.requests.AggregateRequest;
import io.redback.managers.objectmanager.requests.MultiRequest;
import io.redback.managers.objectmanager.requests.MultiResponse;
import io.redback.managers.objectmanager.requests.UpdateRequest;
import io.redback.security.Session;
import io.redback.utils.stream.AccumulatingDataStream;
import io.redback.utils.stream.DataStream;
import io.redback.utils.stream.ReceivingConverter;
import io.redback.utils.stream.ReceivingStreamPipeline;

public class ObjectClient extends Client
{
	public ObjectClient(Firebus fb, String sn) 
	{
		super(fb, sn);
	}
	
	public RedbackObjectRemote getObject(Session session, String objectname, String uid) throws RedbackException  {
		return getObject(session, objectname, uid, true, false);
	}
	
	public RedbackObjectRemote getObject(Session session, String objectname, String uid, boolean addRelated, boolean addValidation) throws RedbackException  {
		DataMap req = new DataMap();
		req.put("action", "get");
		req.put("object", objectname);
		req.put("uid", uid);
		if(addRelated || addValidation) {
			DataMap options = new DataMap();
			if(addRelated) options.put("addrelated", true);
			if(addValidation) options.put("addvalidation", true);
			req.put("options", options);
		}
		DataMap resp = requestDataMap(session, req);
		return new RedbackObjectRemote(session, this, resp);
	}
	
	public RedbackObjectRemote getRelatedObject(Session session, String objectname, String uid, String attribute) throws RedbackException  {
		DataMap req = new DataMap();
		req.put("action", "get");
		req.put("object", objectname);
		req.put("uid", uid);
		req.put("attribute", attribute);
		req.put("options", new DataMap("addrelated", true));
		DataMap resp = requestDataMap(session, req);
		if(resp != null)
			return new RedbackObjectRemote(session, this, resp);
		else 
			return null;
	}

	public List<RedbackObjectRemote> listObjects(Session session, String objectname, DataMap filter) throws RedbackException  {
		return listObjects(session, objectname, filter, null, null, false, false, 0, 50);
	}

	public List<RedbackObjectRemote> listObjects(Session session, String objectname, DataMap filter, DataMap sort) throws RedbackException  {
		return listObjects(session, objectname, filter, null, sort, false, false, 0, 50);
	}
	
	public List<RedbackObjectRemote> listObjects(Session session, String objectname, DataMap filter, DataMap sort, boolean addRelated) throws RedbackException  {
		return listObjects(session, objectname, filter, null, sort, addRelated, false, 0, 50);
	}
	
	public List<RedbackObjectRemote> listObjects(Session session, String objectname, DataMap filter, DataMap sort, boolean addRelated, boolean addValidation) throws RedbackException  {
		return listObjects(session, objectname, filter, null, sort, addRelated, addValidation, 0, 50);
	}

	public List<RedbackObjectRemote> listObjects(Session session, String objectname, DataMap filter, DataMap sort, boolean addRelated, boolean addValidation, int page, int pageSize) throws RedbackException  {
		return listObjects(session, objectname, filter, null, sort, addRelated, addValidation, 0, 50);
	}
	
	public List<RedbackObjectRemote> listObjects(Session session, String objectname, DataMap filter, String search, DataMap sort, boolean addRelated, boolean addValidation, int page, int pageSize) throws RedbackException  {
		DataMap req = new DataMap();
		req.put("action", "list");
		req.put("object", objectname);
		req.put("filter", filter != null ? filter : new DataMap());
		if(search != null) 
			req.put("search", search);
		if(sort != null)
			req.put("sort", sort);
		req.put("page", page);
		req.put("pagesize", pageSize);
		if(addRelated || addValidation) {
			DataMap options = new DataMap();
			//if(addRelated) options.put("addrelated", true);
			if(addValidation) options.put("addvalidation", true);
			req.put("options", options);
		}
		DataMap resp = requestDataMap(session, req);
		List<RedbackObjectRemote> list = new ArrayList<RedbackObjectRemote>();
		for(int i = 0; i < resp.getList("list").size(); i++) {
			DataMap item = resp.getList("list").getObject(i);
			list.add(new RedbackObjectRemote(session, this, item));
		}
		if(addRelated) {
			RemoteObjectRelater relater = new RemoteObjectRelater(session, this);
			relater.resolve(list);
		}
		return list;

	}

	public List<RedbackObjectRemote>  listAllObjects(Session session, String objectName, DataMap filter, DataMap sort, boolean addRelated) throws RedbackException  {
		return listAllObjects(session, objectName, filter, sort, addRelated, false);
	}
	
	public List<RedbackObjectRemote>  listAllObjects(Session session, String objectName, DataMap filter, DataMap sort, boolean addRelated, boolean addValidation) throws RedbackException  {
		AccumulatingDataStream<RedbackObjectRemote> stream = new AccumulatingDataStream<RedbackObjectRemote>();
		streamObjects(session, objectName, filter, sort, addValidation || addRelated, -1, stream);
		List<RedbackObjectRemote> list = stream.getList();
		if(addRelated) {
			RemoteObjectRelater relater = new RemoteObjectRelater(session, this);
			relater.resolve(list);
		}
		return list;
	}
	
	public void streamObjects(Session session, String objectname, DataMap filter, DataMap sort, boolean addValidation, int chunkSize, DataStream<RedbackObjectRemote> stream) throws RedbackException  {
		DataMap req = new DataMap();
		req.put("action", "list");
		req.put("object", objectname);
		req.put("filter", filter != null ? filter : new DataMap());
		if(sort != null) req.put("sort", sort);
		if(chunkSize != -1) req.put("chunksize", chunkSize);
		if(addValidation) {
			DataMap options = new DataMap();
			//if(addRelated) options.put("addrelated", true);
			if(addValidation) options.put("addvalidation", true);
			req.put("options", options);
		}
		final ObjectClient objectClient = this;
		StreamEndpoint sep = this.requestStream(session, req);
		new ReceivingStreamPipeline<RedbackObjectRemote>(sep, stream, new ReceivingConverter<RedbackObjectRemote>() {
			public List<RedbackObjectRemote> convert(Payload payload) throws DataException {
				DataList list = payload.getDataMap().getList("result");
				List<RedbackObjectRemote> rorList = new ArrayList<RedbackObjectRemote>();
				for(int i = 0; i < list.size(); i++) {
					rorList.add(new RedbackObjectRemote(session, objectClient, list.getObject(i)));
				}
				return rorList;
			}
		});
	}
		
	public RedbackObjectRemote createObject(Session session, String objectname, DataMap data, boolean addRelated) throws RedbackException  {
		return createObject(session, objectname, null, data, addRelated);
	}
	
	public RedbackObjectRemote createObject(Session session, String objectname, String domain, DataMap data, boolean addRelated) throws RedbackException  {
		return createObject(session, objectname, domain, data, addRelated, false);
	}
	
	public RedbackObjectRemote createObject(Session session, String objectname, String domain, DataMap data, boolean addRelated, boolean addValidation) throws RedbackException  {
		DataMap req = new DataMap();
		req.put("action", "create");
		req.put("object", objectname);
		req.put("data", data);
		if(domain != null) 
			req.put("domain", domain);
		if(addRelated || addValidation) {
			DataMap options = new DataMap();
			if(addRelated) options.put("addrelated", true);
			if(addValidation) options.put("addvalidation", true);
			req.put("options", options);
		}
		DataMap resp = requestDataMap(session, req);
		RedbackObjectRemote ror = new RedbackObjectRemote(session, this, resp);
		return ror;
	}
	
	public RedbackObjectRemote updateObject(Session session, String objectname, String uid, DataMap data, boolean addRelated) throws RedbackException  {
		UpdateRequest req = new UpdateRequest(objectname, uid, data, addRelated, false);
		DataMap resp = requestDataMap(session, req.getDataMap());
		RedbackObjectRemote ror = new RedbackObjectRemote(session, this, resp);
		return ror;
	}
	
	public RedbackObjectRemote execute(Session session, String objectname, String uid, String function, DataMap data) throws RedbackException  {
		DataMap req = new DataMap();
		req.put("action", "execute");
		req.put("object", objectname);
		req.put("uid", uid);
		req.put("function", function);
		req.put("data", data);
		DataMap resp = requestDataMap(session, req);
		return new RedbackObjectRemote(session, this, resp);
	}
	
	public DataEntity execute(Session session, String function, DataMap data) throws RedbackException  {
		DataMap req = new DataMap();
		req.put("action", "execute");
		req.put("function", function);
		req.put("data", data);
		DataMap resp = requestDataMap(session, req);
		return resp.get("data");
	}
	
	public MultiResponse multi(Session session, MultiRequest multiRequest) throws RedbackException {
		DataMap resp = requestDataMap(session, multiRequest.getDataMap());
		return new MultiResponse(resp);
	}
	
	public List<RedbackAggregateRemote> aggregate(Session session, String objectname, DataMap filter, String search, DataList tuple, DataList metrics, DataMap sort, DataList base, boolean addRelated, int page, int pageSize) throws RedbackException {
		AggregateRequest req = new AggregateRequest(objectname, filter, search, tuple, metrics, sort, base, addRelated, page, pageSize);
		DataMap resp = requestDataMap(session, req.getDataMap());
		List<RedbackAggregateRemote> list = new ArrayList<RedbackAggregateRemote>();
		for(int i = 0; i < resp.getList("list").size(); i++) {
			DataMap item = resp.getList("list").getObject(i);
			list.add(new RedbackAggregateRemote(session, this, item));
		}		
		return list;
	}
}
