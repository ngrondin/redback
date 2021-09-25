package io.redback.managers.objectmanager.requests;

import io.firebus.data.DataMap;
import io.redback.exceptions.RedbackException;

public class ListRequest extends ObjectRequest {
	public String objectName;
	public DataMap filter;
	public String searchText;
	public DataMap sort;
	public int page;
	public int pageSize;
	
	public ListRequest(DataMap data) throws RedbackException {
		super(data);
		objectName = data.getString("object");
		filter = data.getObject("filter");
		searchText = data.getString("search");
		sort = data.getObject("sort");
		page = data.containsKey("page") ? data.getNumber("page").intValue() : 0;
		pageSize = data.containsKey("pagesize") ? data.getNumber("pagesize").intValue() : 50;
	}
	
	public ListRequest(String on, DataMap f, String st, DataMap s, boolean ar, boolean av, int p, int ps) {
		super(ar, av);
		objectName = on;
		filter = f;
		searchText = st;
		sort = s;
		addRelated = ar;
		page = p;
		pageSize = ps;
	}

	public DataMap getDataMap() {
		DataMap req = new DataMap();
		req.put("action", "list");
		req.put("object", objectName);
		req.put("filter", filter != null ? filter : new DataMap());
		if(sort != null)
			req.put("sort", sort);
		req.put("page", page);
		req.put("pagesize", pageSize);
		DataMap options = new DataMap();
		options.put("addrelated", addRelated);
		options.put("addvalidation", addValidation);
		return req;
	}
	
	
}
