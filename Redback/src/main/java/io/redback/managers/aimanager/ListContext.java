package io.redback.managers.aimanager;

import java.util.List;

import io.firebus.data.DataMap;
import io.redback.client.RedbackObjectRemote;

public class ListContext extends SEContextLevel {
	public String objectname;
	public DataMap filter;
	public String search;
	public DataMap sort;
	public List<RedbackObjectRemote> list;
	
	public ListContext(List<RedbackObjectRemote> l,  DataMap f, String s, DataMap so) {
		objectname = l.size() > 0 ? l.get(0).getObjectName() : null;
		list = l;
		filter = f;
		search = s;
		sort = so;
	}
	
	public ListContext(String on, DataMap f, String s, DataMap so) {
		objectname = on;
		filter = f;
		search = s;
		sort = so;
	}

}
