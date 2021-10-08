package io.redback.managers.objectmanager.requests;

import io.firebus.data.DataMap;
import io.redback.exceptions.RedbackException;

public class ExecuteGlobalRequest extends ObjectRequest {
	public String function;
	public DataMap param;
	
	public ExecuteGlobalRequest(DataMap data) {
		super(data);
		function = data.getString("function");
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
