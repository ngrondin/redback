package io.redback.managers.objectmanager.requests;

import io.firebus.data.DataMap;
import io.redback.exceptions.RedbackException;

public class DeleteRequest extends ObjectRequest {
	public String objectName;
	public String uid;
	
	
	public DeleteRequest(DataMap data) throws RedbackException {
		super(data);
		objectName = data.getString("object");
		uid = data.getString("uid");
	}
	
	
	public DeleteRequest(String on, String u) {
		super(false, false);
		objectName = on;
		uid = u;
	}
	
	public DataMap getDataMap() {
		DataMap req = new DataMap();
		req.put("action", "delete");
		req.put("object", objectName);
		req.put("uid", uid);
		return req;
	}
	
	public DataMap produceResponse(Object resp) throws RedbackException {
		return new DataMap("result", "ok");
	}
}
