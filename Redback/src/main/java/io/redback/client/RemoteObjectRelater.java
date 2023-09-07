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

public class RemoteObjectRelater {
	protected ObjectClient objectClient;
	protected Map<String, Map<String, RedbackObjectRemote>> objectMap = new HashMap<String, Map<String, RedbackObjectRemote>>();
	protected List<RedbackObjectRemote> objectList = new ArrayList<RedbackObjectRemote>();
	protected Map<String, Map<Integer, Boolean>> queried = new HashMap<String, Map<Integer, Boolean>>();;
	protected Map<String, List<DataMap>> queryMap = new HashMap<String, List<DataMap>>();
	protected Session session;
	
	public RemoteObjectRelater(Session s, ObjectClient oc) {
		objectClient = oc;
		session = s;
	}
	
	protected RedbackObjectRemote get(String objectname, String uid) {
		Map<String, RedbackObjectRemote> sub = objectMap.get(objectname);
		return sub != null ? sub.get(uid) : null;
	}
	
	protected RedbackObjectRemote find(String objectname, DataMap filter) {
		Map<String, RedbackObjectRemote> sub = objectMap.get(objectname);
		if(sub != null)
			for(RedbackObjectRemote ror: sub.values()) 
				if(FilterProcessor.apply(ror.getData(), filter))
					return ror;
		return null;
	}
	
	protected void put(RedbackObjectRemote ror) {
		Map<String, RedbackObjectRemote> sub = objectMap.get(ror.getObjectName());
		if(sub == null) {
			sub = new HashMap<String, RedbackObjectRemote>();
			objectMap.put(ror.getObjectName(), sub);
		}
		if(!sub.containsKey(ror.getUid())) {
			sub.put(ror.getUid(), ror);
			objectList.add(ror);
		}
	}
	
	public void resolve(List<RedbackObjectRemote> list) throws RedbackException {
		for(RedbackObjectRemote ror: list) 
			put(ror);
		long start = System.currentTimeMillis();
		int n = 0;
		while(scan() && n++ < 10) {
			retrieve();
		}
		System.out.println("Resolve - in " + (System.currentTimeMillis() - start));
	}
	
	protected boolean scan() throws RedbackException {
		int totalScanned = 0;
		int totalToQuery = 0;
		long start = System.currentTimeMillis();
		/*for(String objectName: objectMap.keySet()) {
			long ostart = System.currentTimeMillis();
			Map<String, RedbackObjectRemote> sub = objectMap.get(objectName);
			for(String uid: sub.keySet()) {
				RedbackObjectRemote ror = sub.get(uid);*/
			for(RedbackObjectRemote ror: objectList) {
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
										if(!queried.containsKey(relObjectName)) queried.put(relObjectName, new HashMap<Integer, Boolean>());
										if(!queried.get(relObjectName).containsKey(hash)) {
											queried.get(relObjectName).put(hash, true);
											if(!queryMap.containsKey(relObjectName)) queryMap.put(relObjectName, new ArrayList<DataMap>());										
											queryMap.get(relObjectName).add(filter);	
											totalToQuery++;
										}
									}
								}
							}				
						}
					}
				}
				totalScanned++;
			}
			//System.out.println("Scan - " + objectName + " " + sub.keySet().size() + " objects in " + (System.currentTimeMillis() - ostart) + "ms");
		//}
		System.out.println("Scan - total scanned " + totalScanned + " total to query " + totalToQuery + " in " + (System.currentTimeMillis() - start) + "ms");
		return !queryMap.isEmpty();
	}
	
	protected void retrieve() throws RedbackException {
		int totalRetrieved = 0;
		long start = System.currentTimeMillis();
		for(String objectName: queryMap.keySet()) {
			long ostart = System.currentTimeMillis();
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
			for(RedbackObjectRemote ror: result) {
				put(ror);
				totalRetrieved++;
			}
			System.out.println("Retrieve - " + objectName + " in " + (System.currentTimeMillis() - ostart) + "ms");
		}
		queryMap.clear();
		System.out.println("Retrieve - total retrieved " + totalRetrieved + " in " + (System.currentTimeMillis() - start) + "ms");
		
	}
}