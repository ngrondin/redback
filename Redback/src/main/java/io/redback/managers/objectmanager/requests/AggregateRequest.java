package io.redback.managers.objectmanager.requests;

import io.firebus.data.DataList;
import io.firebus.data.DataMap;
import io.redback.exceptions.RedbackException;

public class AggregateRequest extends ObjectRequest {
	public String objectName;
	public DataMap filter;
	public String searchText;
	public DataList tuple;
	public DataList metrics;
	public DataMap sort;
	public DataList base;
	public int page;
	public int pageSize;
	
	
	public AggregateRequest(DataMap data) throws RedbackException {
		super(data);
		objectName = data.getString("object");
		filter = data.getObject("filter");
		searchText = data.getString("search");
		tuple = data.getList("tuple");
		metrics = data.getList("metrics");
		sort = data.getObject("sort");
		base = data.getList("base");
		page = data.containsKey("page") ? data.getNumber("page").intValue() : 0;
		pageSize = data.containsKey("pagesize") ? data.getNumber("pagesize").intValue() : 50;
	}
	
	public AggregateRequest(String on, DataMap f, String st, DataList t, DataList m, DataMap s, DataList b, boolean ar, int p, int ps) {
		super(ar, false);
		objectName = on;
		filter = f;
		searchText = st;
		tuple = t;
		metrics = m;
		sort = s;
		base = b;
		page = p;
		pageSize = ps;
	}

	public DataMap getDataMap() {
		DataMap map = new DataMap();
		map.put("action", "aggregate");
		map.put("object", objectName);
		map.put("filter", filter != null ? filter : new DataMap());
		if(searchText != null)
			map.put("search", searchText);
		if(sort != null)
			map.put("sort", sort);
		if(tuple != null)
			map.put("tuple", tuple);
		map.put("page", page);
		if(metrics != null)
			map.put("metrics", metrics);
		if(base != null)
			map.put("base", base);		
		map.put("pagesize", pageSize);
		if(addRelated)
			map.put("options", new DataMap("addrelated", true));
		return map;
	}
	
	
	

}
