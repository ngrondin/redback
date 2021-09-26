package io.redback.managers.objectmanager.requests;

import io.firebus.data.DataMap;
import io.redback.exceptions.RedbackException;

public class CreateRequest extends ObjectRequest {
	public String objectName;
	public String uid;
	public String domain;
	public DataMap initialData;
	
	public CreateRequest(DataMap data) throws RedbackException {
		super(data);
		objectName = data.getString("object");
		uid = data.getString("uid");
		domain = data.getString("domain");
		initialData = data.getObject("data");
	}
	
	public CreateRequest(String on, String u, String d, DataMap id, boolean ar, boolean av) {
		super(ar, av);
		objectName = on;
		uid = u;
		domain = d;
		initialData = id;
		addRelated = ar;
		addValidation = av;
	}

	public DataMap getDataMap() {
		DataMap req = new DataMap();
		req.put("action", "create");
		req.put("object", objectName);
		req.put("data", initialData);
		if(domain != null) 
			req.put("domain", domain);
		DataMap options = new DataMap();
		options.put("addrelated", addRelated);
		options.put("addvalidation", addValidation);
		return req;
	}
}
