package io.redback.managers.objectmanager.requests;

import io.firebus.data.DataMap;
import io.redback.exceptions.RedbackException;
import io.redback.managers.objectmanager.RedbackObject;

public class GetRequest extends ObjectRequest {
	public String objectName;
	public String uid;
	
	public GetRequest(DataMap data) throws RedbackException {
		super(data);
		objectName = data.getString("object");
		uid = data.getString("uid");
		if(uid == null) {
			throw new RedbackException("A 'get' action requires a 'uid' attribute");
		}
	}
	
	public GetRequest(String on, String u, boolean ar, boolean av) {
		super(ar, av);
		objectName = on;
		uid = u;
	}


	public DataMap getDataMap() {
		DataMap req = new DataMap();
		req.put("action", "get");
		req.put("object", objectName);
		req.put("uid", uid);
		DataMap options = new DataMap();
		options.put("addrelated", addRelated);
		options.put("addvalidation", addValidation);
		req.put("options", options);
		return req;
	}
	
	public DataMap produceResponse(Object resp) throws RedbackException {
		if(resp instanceof RedbackObject) {
			return ((RedbackObject)resp).getDataMap(addValidation, addRelated, true);
		} else {
			throw new RedbackException("Unexpected object response format");
		}
	}
	
}
