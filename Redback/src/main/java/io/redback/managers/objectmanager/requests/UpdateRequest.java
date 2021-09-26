package io.redback.managers.objectmanager.requests;

import io.firebus.data.DataMap;
import io.redback.exceptions.RedbackException;

public class UpdateRequest extends ObjectRequest {
	public String objectName;
	public String uid;
	public DataMap updateData;
	
	public UpdateRequest(DataMap data) throws RedbackException {
		super(data);
		objectName = data.getString("object");
		uid = data.getString("uid");
		updateData = data.getObject("data");
	}
	
	
	public UpdateRequest(String on, String u, DataMap d, boolean ar, boolean av) {
		super(ar, av);
		objectName = on;
		uid = u;
		updateData = d;
	}

	public DataMap getDataMap() {
		DataMap req = new DataMap();
		req.put("action", "update");
		req.put("object", objectName);
		req.put("uid", uid);
		req.put("data", updateData);
		DataMap options = new DataMap();
		options.put("addrelated", addRelated);
		options.put("addvalidation", addValidation);
		return req;
	}
}
