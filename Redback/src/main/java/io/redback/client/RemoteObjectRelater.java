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
	protected List<RedbackObjectRemote> objects = new ArrayList<RedbackObjectRemote>();
	protected Map<String, List<Integer>> queried = new HashMap<String, List<Integer>>();;
	protected Session session;
	
	public RemoteObjectRelater(Session s, ObjectClient oc) {
		objectClient = oc;
		session = s;
	}
	
	protected RedbackObjectRemote get(String objectname, String uid, DataMap filter) {
		for(RedbackObjectRemote ror: objects) {
			if(ror.getObjectName().equals(objectname))
				if(uid != null && ror.getUid().equals(uid))
					return ror;
				else if(filter != null && FilterProcessor.apply(ror.getData(), filter))
					return ror;
		}
		return null;
	}
	
	protected void put(RedbackObjectRemote ror) {
		RedbackObjectRemote existing = get(ror.getObjectName(), ror.getUid(), null);
		if(existing == null) 
			objects.add(ror);
	}
	
	public void resolve(List<RedbackObjectRemote> list) throws RedbackException {
		for(RedbackObjectRemote ror: list) 
			put(ror);
		Map<String, List<DataMap>> queryMap = null;
		int n = 0;
		while(!(queryMap = scan()).isEmpty() && n++ < 10) {
			request(queryMap);			
		}
	}
	
	protected Map<String, List<DataMap>> scan() throws RedbackException {
		Map<String, List<DataMap>> queryMap = new HashMap<String, List<DataMap>>();
		for(int i = 0; i < objects.size(); i++) {
			RedbackObjectRemote ror = objects.get(i);
			for(String attribute: ror.getAttributeNames()) {
				String attributeValue = ror.getString(attribute);
				if(attributeValue != null) {
					DataMap validation = ror.getValidation(attribute);
					if(validation != null) {
						DataMap relValidation = validation.getObject("related");
						if(relValidation != null) {
							RedbackObjectRemote relRor = ror.getRelated(attribute, false);
							if(relRor != null) {
								put(relRor);
							} else {
								String relObjectName = relValidation.getString("object");
								String link = relValidation.getString("link");
								DataMap filter = new DataMap(link, attributeValue);
								if(link.equals("uid")) {
									relRor = get(relObjectName, attributeValue, null);
								} else {
									filter.merge(relValidation.getObject("listfilter"));
									relRor = get(relObjectName, null, filter);							
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
		return queryMap;
	}
	
	protected void request(Map<String, List<DataMap>> queryMap) throws RedbackException {
		for(String objectName: queryMap.keySet()) {
			List<DataMap> filterList = queryMap.get(objectName);
			DataList orList = new DataList();
			for(DataMap filter: filterList) 
				orList.add(filter);
			List<RedbackObjectRemote> result = objectClient.listAllObjects(session, objectName,  new DataMap("$or", orList), null, false, true);
			for(RedbackObjectRemote ror: result)
				put(ror);
		}
	}
}