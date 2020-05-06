package io.redback.services;

import java.util.ArrayList;
import java.util.List;

import io.firebus.Firebus;
import io.firebus.Payload;
import io.firebus.utils.DataMap;
import io.redback.RedbackException;
import io.redback.client.RedbackObjectRemote;
import io.redback.security.Session;

public abstract class ObjectClientService extends AuthenticatedService 
{
	protected String objectService;
	
	public ObjectClientService(String n, DataMap c, Firebus f) 
	{
		super(n, c, f);
		objectService = config.getString("objectservice");
	}

	protected RedbackObjectRemote getObject(Session session, String objectname, String uid) throws RedbackException  {
		try {
			DataMap req = new DataMap();
			req.put("action", "get");
			req.put("object", objectname);
			req.put("uid", uid);
			req.put("options", new DataMap("addrelated", true));
			Payload reqP = new Payload(req.toString());
			reqP.metadata.put("token", session.getToken());
			Payload respP = firebus.requestService(objectService, reqP);
			DataMap resp = new DataMap(respP.getString());
			return new RedbackObjectRemote(firebus, objectService, session.getToken(), resp);
		} catch(Exception e) {
			throw new RedbackException("Error getting object", e);
		}
	}

	protected List<RedbackObjectRemote> listObjects(Session session, String objectname, DataMap filter) throws RedbackException  {
		return listObjects(session, objectname, filter, true, 0);
	}
	
	protected List<RedbackObjectRemote> listObjects(Session session, String objectname, DataMap filter, boolean addRelated, int page) throws RedbackException  {
		try {
			DataMap req = new DataMap();
			req.put("action", "list");
			req.put("object", objectname);
			req.put("filter", filter);
			req.put("page", page);
			if(addRelated)
				req.put("options", new DataMap("addrelated", true));
			Payload reqP = new Payload(req.toString());
			reqP.metadata.put("token", session.getToken());
			Payload respP = firebus.requestService(objectService, reqP);
			DataMap resp = new DataMap(respP.getString());
			List<RedbackObjectRemote> list = new ArrayList<RedbackObjectRemote>();
			for(int i = 0; i < resp.getList("list").size(); i++) {
				DataMap item = resp.getList("list").getObject(i);
				list.add(new RedbackObjectRemote(firebus, objectService, session.getToken(), item));
			}
			return list;
		} catch(Exception e) {
			throw new RedbackException("Error listins objects", e);
		}
	}
}
