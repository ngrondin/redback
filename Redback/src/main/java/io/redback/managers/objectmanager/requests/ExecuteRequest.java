package io.redback.managers.objectmanager.requests;

import io.firebus.data.DataMap;
import io.redback.exceptions.RedbackException;

public class ExecuteRequest extends ObjectRequest {
	public String objectName;
	public String uid;
	public String function;
	public DataMap param;
	
	
	public ExecuteRequest(DataMap data) throws RedbackException {
		super(data);
		objectName = data.getString("object");
		uid = data.getString("uid");
		function = data.getString("function");
		param = data.containsKey("param") ? data.getObject("param") : data.containsKey("data") ? data.getObject("data") : null;
	}
	
	
	public ExecuteRequest(String on, String u, String f, DataMap p, boolean ar, boolean av) {
		super(ar, av);
		objectName = on;
		uid = u;
		function = f;
		param = p;
		addRelated = ar;
		addValidation = av;
	}

	public DataMap getDataMap() {
		DataMap req = new DataMap();
		req.put("action", "execute");
		req.put("object", objectName);
		req.put("uid", uid);
		req.put("function", function);
		req.put("data", param);
		return req;
	}
}
