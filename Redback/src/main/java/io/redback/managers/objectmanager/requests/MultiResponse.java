package io.redback.managers.objectmanager.requests;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import io.firebus.data.DataMap;

public class MultiResponse {
	public Map<String, Object> responses;
	
	public MultiResponse() {
		responses = new HashMap<String, Object>();
	}
	
	public MultiResponse(DataMap data) {
		responses = new HashMap<String, Object>();
		for(String key: data.keySet()) {
			responses.put(key, data.get(key));
		}
	}
	
	public void addResponse(String key, Object resp) {
		responses.put(key, resp);
	}
	
	public Set<String> getKeys() {
		return responses.keySet();
	}
	
	public Object getResponse(String key) {
		return responses.get(key);
	}
	

}
