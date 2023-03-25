package io.redback.managers.objectmanager.requests;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import io.firebus.data.DataList;
import io.firebus.data.DataMap;
import io.redback.exceptions.RedbackException;

public class MultiRequest extends ObjectRequest {
	public Map<String, ObjectRequest> requests;
	
	public MultiRequest() {
		super(false, false);
		requests = new HashMap<String, ObjectRequest>();
	}
	
	public MultiRequest(DataMap data) throws RedbackException {
		super(data);
		requests = new HashMap<String, ObjectRequest>();
		DataList list = data.getList("multi");
		processList(list);
	}
	
	public MultiRequest(DataList list) throws RedbackException {
		super(false, false);
		requests = new HashMap<String, ObjectRequest>();
		processList(list);
	}
	
	private void processList(DataList list) throws RedbackException {
		for(int i = 0; i < list.size(); i++) {
			DataMap line = list.getObject(i);
			String action = line.getString("action");
			String key = line.getString("key");
			ObjectRequest request = null;
			if(action.equals("get"))
			{
				request = new GetRequest(line);
			}
			else if(action.equals("list") && !line.containsKey("uid"))
			{
				request = new ListRequest(line);
			}
			else if(action.equals("listrelated") || (action.equals("list") && line.containsKey("uid")))
			{
				request = new ListRelatedRequest(line);
			}					
			else if(action.equals("update"))
			{
				request = new UpdateRequest(line);
			}
			else if(action.equals("create"))
			{
				request = new CreateRequest(line);
			}
			else if(action.equals("delete"))
			{
				request = new DeleteRequest(line);
			}					
			else if(action.equals("execute"))
			{
				if(line.containsKey("object")) {
					request = new ExecuteObjectRequest(line);
				} else {
					request = new ExecuteRequest(line);
				}
			}
			else if(action.equals("aggregate"))
			{
				request = new AggregateRequest(line);
			}
			else
			{
				throw new RedbackException("The '" + action + "' action is not valid as an object request");
			}	
			requests.put(key, request);
		}	
	}
	
	public Set<String> getKeys() {
		return requests.keySet();
	}
	
	public ObjectRequest getRequest(String key) {
		return requests.get(key);
	}
	
	public void putRequest(String key,  ObjectRequest req) {
		requests.put(key, req);
	}
	
	
	public DataList getDataList() {
		DataList ret = new DataList();
		for(String key: requests.keySet()) {
			DataMap map = requests.get(key).getDataMap();
			map.put("key", key);
			ret.add(map);
		}
		return ret;
	}

	public DataMap getDataMap() {
		DataMap map = new DataMap();
		map.put("action", "multi");
		map.put("multi", getDataList());
		return map;
	}

	public DataMap produceResponse(Object resp) throws RedbackException {
		if(resp instanceof MultiResponse) {
			MultiResponse mr = (MultiResponse)resp;
			DataMap respMap = new DataMap();
			for(String key: mr.getKeys()) {
				ObjectRequest originalRequest = requests.get(key);
				respMap.put(key, originalRequest.produceResponse(mr.getResponse(key)));
			}
			return respMap;
		} else {
			throw new RedbackException("Unexpected object response format");
		}
	}
}
