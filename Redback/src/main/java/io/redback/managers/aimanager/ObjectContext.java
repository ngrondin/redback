package io.redback.managers.aimanager;

import java.util.List;

import io.firebus.data.DataMap;
import io.redback.client.RedbackObjectRemote;

public class ObjectContext {
	public String objectname;
	public String uid;
	public DataMap filter;
	public String search;
	public RedbackObjectRemote object;
	public List<RedbackObjectRemote> list;
	
	public ObjectContext(RedbackObjectRemote o) {
		object = o;
		objectname = o.getObjectName();
		uid = o.getUid();
	}
	
	public ObjectContext(RedbackObjectRemote o,  DataMap f, String s) {
		object = o;
		objectname = o.getObjectName();
		uid = o.getUid();
		filter = f;
		search = s;
	}
	
	public ObjectContext(String on, String u, DataMap f, String s) {
		objectname = on;
		uid = u;
		filter = f;
		search = s;
	}
	
	public ObjectContext(List<RedbackObjectRemote> l,  DataMap f, String s) {
		objectname = l.size() > 0 ? l.get(0).getObjectName() : null;
		list = l;
		filter = f;
		search = s;
	}

}
