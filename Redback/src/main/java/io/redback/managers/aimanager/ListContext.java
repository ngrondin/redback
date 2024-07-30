package io.redback.managers.aimanager;

import java.util.List;

import io.firebus.data.DataEntity;
import io.firebus.data.DataList;
import io.firebus.data.DataLiteral;
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
	
	public DataMap getUnresolvedUIFilter() {
		if(filter == null) return null;
		return (DataMap)_getUnresolvedUIFilter(filter);
	}
	
	private DataEntity _getUnresolvedUIFilter(DataEntity val) {
		if(val instanceof DataList) {
			DataList outList = new DataList();
			DataList inList = (DataList)val;
			for(int i = 0; i < inList.size(); i++)
				outList.add(_getUnresolvedUIFilter(inList.get(i)));
			return outList;
		} else if(val instanceof DataMap) {
			DataMap out = new DataMap();
			DataMap in = (DataMap)val;
			for(String key : in.keySet())
				out.put(key, _getUnresolvedUIFilter(in.get(key)));
			return out;
		} else if(val instanceof DataLiteral) {
			DataLiteral in = (DataLiteral)val;
			if(in.getType() == DataLiteral.TYPE_STRING || in.getType() == DataLiteral.TYPE_DATE || in.getType() == DataLiteral.TYPE_TIME)
				return new DataLiteral("'" + in.getString() + "'");
			else
				return new DataLiteral(in.getObject());
		} 
		return null;
	}

}
