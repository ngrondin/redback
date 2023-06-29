package io.redback.client;

import java.util.ArrayList;
import java.util.List;

import io.firebus.Firebus;
import io.firebus.Payload;
import io.firebus.StreamEndpoint;
import io.firebus.data.DataEntity;
import io.firebus.data.DataList;
import io.firebus.data.DataMap;
import io.firebus.interfaces.StreamHandler;
import io.firebus.logging.Logger;
import io.redback.exceptions.RedbackException;
import io.redback.managers.objectmanager.requests.AggregateRequest;
import io.redback.managers.objectmanager.requests.MultiRequest;
import io.redback.managers.objectmanager.requests.MultiResponse;
import io.redback.managers.objectmanager.requests.UpdateRequest;
import io.redback.security.Session;
import io.redback.utils.DataStream;
import io.redback.utils.DataStreamReceiver;

public class ObjectClient extends Client
{
	public ObjectClient(Firebus fb, String sn) 
	{
		super(fb, sn);
	}

	public RedbackObjectRemote getObject(Session session, String objectname, String uid) throws RedbackException  {
		DataMap req = new DataMap();
		req.put("action", "get");
		req.put("object", objectname);
		req.put("uid", uid);
		req.put("options", new DataMap("addrelated", true));
		DataMap resp = requestDataMap(session, req);
		return new RedbackObjectRemote(firebus, serviceName, session.getToken(), resp);
	}

	public List<RedbackObjectRemote> listObjects(Session session, String objectname, DataMap filter) throws RedbackException  {
		return listObjects(session, objectname, filter, null, true, 0, 50);
	}

	public List<RedbackObjectRemote> listObjects(Session session, String objectname, DataMap filter, DataMap sort) throws RedbackException  {
		return listObjects(session, objectname, filter, sort, true, 0, 50);
	}

	public List<RedbackObjectRemote> listObjects(Session session, String objectname, DataMap filter, DataMap sort, boolean addRelated, int page, int pageSize) throws RedbackException  {
		DataMap req = new DataMap();
		req.put("action", "list");
		req.put("object", objectname);
		req.put("filter", filter != null ? filter : new DataMap());
		if(sort != null)
			req.put("sort", sort);
		req.put("page", page);
		req.put("pagesize", pageSize);
		if(addRelated)
			req.put("options", new DataMap("addrelated", true));
		DataMap resp = requestDataMap(session, req);
		List<RedbackObjectRemote> list = new ArrayList<RedbackObjectRemote>();
		for(int i = 0; i < resp.getList("list").size(); i++) {
			DataMap item = resp.getList("list").getObject(i);
			list.add(new RedbackObjectRemote(firebus, serviceName, session.getToken(), item));
		}
		return list;

	}

	public List<RedbackObjectRemote>  listAllObjects(Session session, String objectname, DataMap filter, DataMap sort, boolean addRelated) throws RedbackException  {
		List<RedbackObjectRemote> list = new ArrayList<RedbackObjectRemote>();
		DataStream<List<RedbackObjectRemote>, Boolean> stream = new DataStream<List<RedbackObjectRemote>, Boolean>() {
			public void received(List<RedbackObjectRemote> sublist) {
				list.addAll(sublist);
				sendOut(true);
			}

			public void completed() {
				try {synchronized(list) {list.notify();}} catch(Exception e) {}
			}
		};
		StreamObjects(session, objectname, filter, sort, addRelated, stream);
		try {synchronized(list) {list.wait(60000);}} catch(Exception e) {}
		return list;
	}
	
	public void StreamObjects(Session session, String objectname, DataMap filter, DataMap sort, boolean addRelated, DataStream<List<RedbackObjectRemote>, Boolean> stream) throws RedbackException  {
		DataMap req = new DataMap();
		req.put("action", "list");
		req.put("object", objectname);
		req.put("filter", filter != null ? filter : new DataMap());
		if(sort != null) req.put("sort", sort);
		StreamEndpoint sep = this.requestStream(session, req);
		sep.setHandler(new StreamHandler() {
			public void receiveStreamData(Payload payload, StreamEndpoint streamEndpoint) {
				try {
					DataList list = payload.getDataMap().getList("result");
					List<RedbackObjectRemote> rorList = new ArrayList<RedbackObjectRemote>();
					for(int i = 0; i < list.size(); i++) {
						rorList.add(new RedbackObjectRemote(firebus, serviceName, session.getToken(), list.getObject(i)));
					}
					stream.sendIn(rorList);
				} catch(Exception e) {
					Logger.severe("rb.objectclient.stream", e);
				}
			}

			public void streamClosed(StreamEndpoint streamEndpoint) {
				sep.close();
				stream.complete();
			}
		});
		
		stream.setReceiver(new DataStreamReceiver<Boolean>() {
			public void receive(Boolean sendMore) {
				if(sendMore == true) 
					sep.send(new Payload("next"));
			}
		});
	}
		
	public RedbackObjectRemote createObject(Session session, String objectname, DataMap data, boolean addRelated) throws RedbackException  {
		return createObject(session, objectname, null, data, addRelated);
	}
	
	public RedbackObjectRemote createObject(Session session, String objectname, String domain, DataMap data, boolean addRelated) throws RedbackException  {
		DataMap req = new DataMap();
		req.put("action", "create");
		req.put("object", objectname);
		req.put("data", data);
		if(domain != null) 
			req.put("domain", domain);
		if(addRelated)
			req.put("options", new DataMap("addrelated", true));
		DataMap resp = requestDataMap(session, req);
		RedbackObjectRemote ror = new RedbackObjectRemote(firebus, serviceName, session.getToken(), resp);
		return ror;
	}
	
	public RedbackObjectRemote updateObject(Session session, String objectname, String uid, DataMap data, boolean addRelated) throws RedbackException  {
		UpdateRequest req = new UpdateRequest(objectname, uid, data, addRelated, false);
		DataMap resp = requestDataMap(session, req.getDataMap());
		RedbackObjectRemote ror = new RedbackObjectRemote(firebus, serviceName, session.getToken(), resp);
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
		return new RedbackObjectRemote(firebus, serviceName, session.getToken(), resp);
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
			list.add(new RedbackAggregateRemote(firebus, serviceName, session.getToken(), item));
		}		
		return list;
	}
}
