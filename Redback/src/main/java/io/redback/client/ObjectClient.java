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
		return listObjects(session, objectname, filter, true, 0);
	}

	public List<RedbackObjectRemote> listAllObjects(Session session, String objectname, DataMap filter, boolean addRelated) throws RedbackException  {
		List<RedbackObjectRemote> list = new ArrayList<RedbackObjectRemote>();
		int page = 0;
		boolean more = true;
		while(more) 
		{
			List<RedbackObjectRemote> sublist = listObjects(session, objectname, filter, addRelated, page++);
			list.addAll(sublist);
			if(sublist.size() < 50)
				more = false;
		}
		return list;
	}

		
	public List<RedbackObjectRemote> listObjects(Session session, String objectname, DataMap filter, boolean addRelated, int page) throws RedbackException  {
		try {
			DataMap req = new DataMap();
			req.put("action", "list");
			req.put("object", objectname);
			req.put("filter", filter);
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
}
