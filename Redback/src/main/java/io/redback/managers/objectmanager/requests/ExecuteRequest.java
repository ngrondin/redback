package io.redback.managers.objectmanager.requests;

import io.firebus.data.DataMap;
import io.redback.exceptions.RedbackException;

public class ExecuteRequest extends ObjectRequest {
	public String function;
	public String domain;
	public DataMap param;
	
	public ExecuteRequest(DataMap data) {
		super(data);
		function = data.getString("function");
		domain = data.getString("domain");
		param = data.containsKey("param") ? data.getObject("param") : data.containsKey("data") ? data.getObject("data") : null;

	}
	
	public DataMap getDataMap() {
		DataMap req = new DataMap();
		req.put("action", "execute");
		req.put("function", function);
		req.put("data", param);
		return req;
	}
	
	public DataMap produceResponse(Object resp) throws RedbackException {
		return new DataMap("result", "ok");
	}
}
