package io.redback.client;

import java.util.ArrayList;
import java.util.List;

import io.firebus.Firebus;
import io.firebus.utils.DataMap;
import io.redback.RedbackException;
import io.redback.security.Session;

public class ObjectClient extends Client
{
	public ObjectClient(Firebus fb, String sn) 
	{
		super(fb, sn);
	}

	
	public RedbackObjectRemote getObject(Session session, String objectname, String uid) throws RedbackException  {
		try {
			DataMap req = new DataMap();
			req.put("action", "get");
			req.put("object", objectname);
			req.put("uid", uid);
			req.put("options", new DataMap("addrelated", true));
			DataMap resp = request(session, req);
			return new RedbackObjectRemote(firebus, serviceName, session.getToken(), resp);
		} catch(Exception e) {
			throw new RedbackException("Error getting object", e);
		}
	}

	public List<RedbackObjectRemote> listObjects(Session session, String objectname, DataMap filter) throws RedbackException  {
		return listObjects(session, objectname, filter, null, true, 0);
	}

	public List<RedbackObjectRemote> listObjects(Session session, String objectname, DataMap filter, DataMap sort) throws RedbackException  {
		return listObjects(session, objectname, filter, sort, true, 0);
	}

	public List<RedbackObjectRemote> listObjects(Session session, String objectname, DataMap filter, DataMap sort, boolean addRelated, int page) throws RedbackException  {
		try {
			DataMap req = new DataMap();
			req.put("action", "list");
			req.put("object", objectname);
			req.put("filter", filter);
			if(sort != null)
				req.put("sort", sort);
			req.put("page", page);
			if(addRelated)
				req.put("options", new DataMap("addrelated", true));
			DataMap resp = request(session, req);
			List<RedbackObjectRemote> list = new ArrayList<RedbackObjectRemote>();
			for(int i = 0; i < resp.getList("list").size(); i++) {
				DataMap item = resp.getList("list").getObject(i);
				list.add(new RedbackObjectRemote(firebus, serviceName, session.getToken(), item));
			}
			return list;
		} catch(Exception e) {
			throw new RedbackException("Error listing objects", e);
		}
	}
	
	public List<RedbackObjectRemote> listAllObjects(Session session, String objectname, DataMap filter, DataMap sort, boolean addRelated) throws RedbackException  {
		List<RedbackObjectRemote> list = new ArrayList<RedbackObjectRemote>();
		int page = 0;
		boolean more = true;
		while(more) 
		{
			List<RedbackObjectRemote> sublist = listObjects(session, objectname, filter, sort, addRelated, page++);
			list.addAll(sublist);
			if(sublist.size() < 50)
				more = false;
		}
		return list;
	}
		
	public RedbackObjectRemote createObject(Session session, String objectname, DataMap data, boolean addRelated) throws RedbackException  {
		return createObject(session, objectname, null, data, addRelated);
	}
	
	public RedbackObjectRemote createObject(Session session, String objectname, String domain, DataMap data, boolean addRelated) throws RedbackException  {
		try {
			DataMap req = new DataMap();
			req.put("action", "create");
			req.put("object", objectname);
			req.put("data", data);
			if(domain != null) 
				req.put("domain", domain);
			if(addRelated)
				req.put("options", new DataMap("addrelated", true));
			DataMap resp = request(session, req);
			RedbackObjectRemote ror = new RedbackObjectRemote(firebus, serviceName, session.getToken(), resp);
			return ror;
		} catch(Exception e) {
			throw new RedbackException("Error listing objects", e);
		}
	}
	
	public RedbackObjectRemote execute(Session session, String objectname, String uid, String function, DataMap data) throws RedbackException  {
		try {
			DataMap req = new DataMap();
			req.put("action", "execute");
			req.put("object", objectname);
			req.put("uid", uid);
			req.put("function", function);
			req.put("data", data);
			DataMap resp = request(session, req);
			return new RedbackObjectRemote(firebus, serviceName, session.getToken(), resp);
		} catch(Exception e) {
			throw new RedbackException("Error getting object", e);
		}
	}
}
