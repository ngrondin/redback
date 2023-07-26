package io.redback.client;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.firebus.data.DataList;
import io.firebus.data.DataMap;
import io.redback.exceptions.RedbackException;
import io.redback.security.Session;
import io.redback.utils.FilterProcessor;
import io.redback.utils.Timer;

public class RemoteObjectRelater {
	protected ObjectClient objectClient;
	protected Map<String, Map<String, RedbackObjectRemote>> objects = new HashMap<String, Map<String, RedbackObjectRemote>>();
	protected Map<String, List<Integer>> queried = new HashMap<String, List<Integer>>();;
	protected Session session;
	
	public RemoteObjectRelater(Session s, ObjectClient oc) {
		objectClient = oc;
		session = s;
	}
	
	protected RedbackObjectRemote get(String objectname, String uid) {
		Map<String, RedbackObjectRemote> sub = objects.get(objectname);
		return sub != null ? sub.get(uid) : null;
	}
	
	protected RedbackObjectRemote find(String objectname, DataMap filter) {
		Map<String, RedbackObjectRemote> sub = objects.get(objectname);
		if(sub != null)
			for(RedbackObjectRemote ror: sub.values()) 
				if(FilterProcessor.apply(ror.getData(), filter))
					return ror;
		return null;
	}
	
	protected void put(RedbackObjectRemote ror) {
		Map<String, RedbackObjectRemote> sub = objects.get(ror.getObjectName());
		if(sub == null) {
			sub = new HashMap<String, RedbackObjectRemote>();
			objects.put(ror.getObjectName(), sub);
		}
		sub.put(ror.getUid(), ror);
	}
	
	public void resolve(List<RedbackObjectRemote> list) throws RedbackException {
		System.out.println("Relater starting");
		Timer t = new Timer();
		for(RedbackObjectRemote ror: list) 
			put(ror);
		Map<String, List<DataMap>> queryMap = null;
		int n = 0;
		while(!(queryMap = scan()).isEmpty() && n++ < 10) {
			request(queryMap);			
		}
		System.out.println("Relater took " + t.mark() + "ms");
	}
	
	protected Map<String, List<DataMap>> scan() throws RedbackException {
		Map<String, List<DataMap>> queryMap = new HashMap<String, List<DataMap>>();
		for(Map<String, RedbackObjectRemote> sub: objects.values()) {
			for(RedbackObjectRemote ror: sub.values()) {
				for(String attribute: ror.getAttributeNames()) {
					String attributeValue = ror.getString(attribute);
					if(attributeValue != null) {
						DataMap validation = ror.getValidation(attribute);
						if(validation != null) {
							DataMap relValidation = validation.getObject("related");
							if(relValidation != null) {
								RedbackObjectRemote relRor = ror.getRelated(attribute, false);
								if(relRor == null) {
									String relObjectName = relValidation.getString("object");
									String link = relValidation.getString("link");
									DataMap filter = new DataMap(link, attributeValue);
									if(link.equals("uid")) {
										relRor = get(relObjectName, attributeValue);
									} else {
										filter.merge(relValidation.getObject("listfilter"));
										relRor = find(relObjectName, filter);							
									}
									if(relRor != null) {
										ror.setRelated(attribute, relRor);
									} else {
										int hash = filter.toString().hashCode();
										if(!queried.containsKey(relObjectName)) queried.put(relObjectName, new ArrayList<Integer>());
										if(!queried.get(relObjectName).contains(hash)) {
											queried.get(relObjectName).add(hash);
											if(!queryMap.containsKey(relObjectName)) queryMap.put(relObjectName, new ArrayList<DataMap>());										
											queryMap.get(relObjectName).add(filter);										
										}
									}
								}
							}				
						}
					}
				}
			}
		}
		return queryMap;
	}
	
	protected void request(Map<String, List<DataMap>> queryMap) throws RedbackException {
		for(String objectName: queryMap.keySet()) {
			List<DataMap> filterList = queryMap.get(objectName);
			DataList orList = new DataList();
			DataList uidList = new DataList();
			for(DataMap filter: filterList)
				if(filter.containsKey("uid")) uidList.add(filter.getString("uid"));
				else orList.add(filter);
			DataMap filter = new DataMap();
			if(orList.size() == 0) filter.put("uid", new DataMap("$in", uidList));
			else if(uidList.size() == 0) filter.put("$or", orList);
			else {
				orList.add(new DataMap("uid", new DataMap("$in", uidList)));
				filter.put("$or", orList);
			}
			List<RedbackObjectRemote> result = objectClient.listAllObjects(session, objectName, filter, null, false, true);
			for(RedbackObjectRemote ror: result)
				put(ror);
		}
	}
}