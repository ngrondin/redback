package io.redback.managers.objectmanager.requests;

import io.firebus.data.DataMap;
import io.redback.exceptions.RedbackException;

public class ListRelatedRequest extends ObjectRequest {
	public String objectName;
	public String uid;
	public String attribute;
	public DataMap filter;
	public String searchText;
	public DataMap sort;
	public int page;
	public int pageSize;
	
	public ListRelatedRequest(DataMap data) throws RedbackException {
		super(data);
		objectName = data.getString("object");
		uid = data.getString("uid");
		attribute = data.getString("attribute");
		filter = data.getObject("filter");
		searchText = data.getString("search");
		sort = data.getObject("sort");
		page = data.containsKey("page") ? data.getNumber("page").intValue() : 0;
		pageSize = data.containsKey("pagesize") ? data.getNumber("pagesize").intValue() : 50;
		if(uid == null || attribute == null)
			throw new RedbackException("A 'list' action requires either a filter, a search or a uid-attribute pair");
	}
	
	
	public ListRelatedRequest(String on, String u, String a, DataMap f, String st, DataMap s, boolean ar, boolean av, int p, int ps) {
		super(ar, av);
		objectName = on;
		uid = u;
		attribute = a;
		filter = f;
		searchText = st;
		sort = s;
		addRelated = ar;
		page = p;
		pageSize = ps;
		addRelated = ar;
		addValidation = av;
	}


	public DataMap getDataMap() {
		DataMap req = new DataMap();
		req.put("action", "listrelated");
		req.put("object", objectName);
		req.put("uid", uid);
		req.put("attribute", attribute);
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
