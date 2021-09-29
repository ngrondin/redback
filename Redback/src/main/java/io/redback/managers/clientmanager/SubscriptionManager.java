package io.redback.managers.clientmanager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.firebus.data.DataFilter;
import io.firebus.data.DataMap;
import io.redback.exceptions.RedbackException;

public class SubscriptionManager {

	protected class FilterSubscription /*extends Subscription */{
		public String id;
		public DataFilter filter;
		public ClientHandler clientHandler;
		
		public FilterSubscription(String i, ClientHandler ch, DataMap f) {
			id = i;
			clientHandler = ch;
			filter = new DataFilter(f);
		}

		public boolean matches(DataMap data) {
			return filter.apply(data);
		}
	}
	
	protected abstract class Pointer {
		
	}
	
	protected class ObjectDomainPointer extends Pointer {
		public String objectname;
		public String domain;
		public ObjectDomainPointer(String o, String d) {objectname = o; domain = d;}
	}

	protected class ObjectUIDPointer extends Pointer {
		public String objectname;
		public String uid;
		public ObjectUIDPointer(String o, String i) {objectname = o; uid = i;}
	}
	
	protected Map<String, Map<String, List<ClientHandler>>> uniqueObjectSubsriptions;
	protected Map<String, Map<String, List<FilterSubscription>>> objectFilterSubscriptions;
	protected Map<ClientHandler, List<ObjectUIDPointer>> sessionObjectUIDPointers;
	protected Map<ClientHandler, List<ObjectDomainPointer>> sessionObjectDomainPointers;
	
	public SubscriptionManager() {
		uniqueObjectSubsriptions = new HashMap<String, Map<String, List<ClientHandler>>>();
		objectFilterSubscriptions = new HashMap<String, Map<String, List<FilterSubscription>>>();
		sessionObjectUIDPointers = new HashMap<ClientHandler, List<ObjectUIDPointer>>();
		sessionObjectDomainPointers = new HashMap<ClientHandler, List<ObjectDomainPointer>>();	
	}
	
	public synchronized void subscribe(ClientHandler clientHandler, String objectname, String uid) throws RedbackException {
		try {
			Map<String, List<ClientHandler>> objMap = uniqueObjectSubsriptions.get(objectname);
			if(objMap == null) {
				objMap = new HashMap<String, List<ClientHandler>>();
				uniqueObjectSubsriptions.put(objectname, objMap);
			}
			List<ClientHandler> list = objMap.get(uid);
			if(list == null) {
				list = new ArrayList<ClientHandler>();
				objMap.put(uid, list);
			}
			list.add(clientHandler);
			List<ObjectUIDPointer> pointers = sessionObjectUIDPointers.get(clientHandler);
			if(pointers == null) {
				pointers = new ArrayList<ObjectUIDPointer>();
				sessionObjectUIDPointers.put(clientHandler, pointers);
			}
			pointers.add(new ObjectUIDPointer(objectname, uid));
		} catch(Exception e) {
			throw new RedbackException("Error subscribing session " + clientHandler.getSession().getId() + " to " + objectname + "." + uid , e);
		}
	}
	
	public synchronized void subscribe(ClientHandler clientHandler, String objectname, DataMap filter, String id) throws RedbackException {
		try {
			Map<String, List<FilterSubscription>> objMap = objectFilterSubscriptions.get(objectname);
			if(objMap == null) {
				objMap = new HashMap<String, List<FilterSubscription>>();
				objectFilterSubscriptions.put(objectname, objMap);
			}
			for(String domain: clientHandler.getSession().getUserProfile().getDomains()) {
				List<FilterSubscription> list = objMap.get(domain);
				if(list == null) {
					list = new ArrayList<FilterSubscription>();
					objMap.put(domain, list);
				}
				FilterSubscription existingFilterSubscription = null;
				if(id != null)
					for(FilterSubscription fs: list) 
						if(fs.id.equals(id)) 
							existingFilterSubscription = fs;
				if(existingFilterSubscription != null) {
					existingFilterSubscription.filter = new DataFilter(filter);
				} else {
					list.add(new FilterSubscription(id, clientHandler, filter));
				}
				List<ObjectDomainPointer> pointers = sessionObjectDomainPointers.get(clientHandler);
				if(pointers == null) {
					pointers = new ArrayList<ObjectDomainPointer>();
					sessionObjectDomainPointers.put(clientHandler, pointers);
				}
				pointers.add(new ObjectDomainPointer(objectname, domain));						
			}
		} catch(Exception e) {
			throw new RedbackException("Error subscribing session " + clientHandler.getSession().getId() + " to filter " + id + " for " + objectname, e);
		}
	}
	
	public synchronized void unsubscribe(ClientHandler clientHandler) throws RedbackException {
		try {
			List<ObjectUIDPointer> p1 = sessionObjectUIDPointers.get(clientHandler);
			if(p1 != null) {
				for(ObjectUIDPointer pointer: p1) {
					Map<String, List<ClientHandler>> map = uniqueObjectSubsriptions.get(pointer.objectname);
					if(map != null) {
						List<ClientHandler> list = map.get(pointer.uid);
						if(list != null) {
							list.remove(clientHandler);
							if(list.size() == 0) {
								map.remove(pointer.uid);
								if(map.size() == 0)
									uniqueObjectSubsriptions.remove(pointer.objectname);
							}
						}
					}
				}
				sessionObjectUIDPointers.remove(clientHandler);
			}
				
			List<ObjectDomainPointer> p2 = sessionObjectDomainPointers.get(clientHandler);
			if(p2 != null) {
				for(ObjectDomainPointer pointer: p2) {
					Map<String, List<FilterSubscription>> map = objectFilterSubscriptions.get(pointer.objectname);
					if(map != null) 
					{
						List<FilterSubscription> list = map.get(pointer.domain);
						if(list != null) {
							for(int i = 0; i < list.size(); i++)
								if(list.get(i).clientHandler == clientHandler)
									list.remove(i--);
							if(list.size() == 0) {
								map.remove(pointer.domain);
								if(map.size() == 0)
									objectFilterSubscriptions.remove(pointer.objectname);
							}
						}
					}
				}
				sessionObjectDomainPointers.remove(clientHandler);
			}
		} catch(Exception e) {
			throw new RedbackException("Error unsubscribing session " + clientHandler.getSession().getId(), e);
		}	
	}
	
	public synchronized List<ClientHandler> getSubscribersFor(DataMap data) throws RedbackException {
		List<ClientHandler> subscribers = new ArrayList<ClientHandler>();
		String objectName = data.getString("objectname");
		String uid = data.getString("uid");
		Map<String, List<ClientHandler>> map1 = uniqueObjectSubsriptions.get(objectName);
		if(map1 != null) {
			List<ClientHandler> list = map1.get(uid);
			if(list != null)
				for(ClientHandler clientHandler : list) 
					subscribers.add(clientHandler);
		}

		String domain = data.getString("domain");
		Map<String, List<FilterSubscription>> map2 = objectFilterSubscriptions.get(objectName);
		if(map2 != null) {
			List<FilterSubscription> list = map2.get(domain);
			if(list != null)
				for(FilterSubscription subs : list) 
					if(!subscribers.contains(subs.clientHandler) && subs.matches(data.getObject("data")))
						subscribers.add(subs.clientHandler);
		}
		return subscribers;
	}
}
