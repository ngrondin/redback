package io.redback.managers.aimanager;

import java.util.List;

import io.firebus.data.DataMap;
import io.redback.client.RedbackObjectRemote;

public class ListContext extends SEContextLevel {
	public String objectname;
	public DataMap filter;
	public String search;
	public List<RedbackObjectRemote> list;
	
	public ListContext(List<RedbackObjectRemote> l,  DataMap f, String s) {
		objectname = l.size() > 0 ? l.get(0).getObjectName() : null;
		list = l;
		filter = f;
		search = s;
	}
	
	public ListContext(String on, DataMap f, String s) {
		objectname = on;
		filter = f;
		search = s;
	}

}
