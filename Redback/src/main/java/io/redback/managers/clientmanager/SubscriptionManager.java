package io.redback.managers.clientmanager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.firebus.utils.DataFilter;
import io.firebus.utils.DataMap;
import io.redback.RedbackException;
import io.redback.security.Session;

public class SubscriptionManager {

	protected class FilterSubscription /*extends Subscription */{
		public String id;
		public DataFilter filter;
		public Session session;
		
		public FilterSubscription(String i, Session s, DataMap f) {
			id = i;
			session = s;
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
	
	protected Map<String, Map<String, List<Session>>> uniqueObjectSubsriptions;
	protected Map<String, Map<String, List<FilterSubscription>>> objectFilterSubscriptions;
	protected Map<Session, List<ObjectUIDPointer>> sessionObjectUIDPointers;
	protected Map<Session, List<ObjectDomainPointer>> sessionObjectDomainPointers;
	
	public SubscriptionManager() {
		uniqueObjectSubsriptions = new HashMap<String, Map<String, List<Session>>>();
		objectFilterSubscriptions = new HashMap<String, Map<String, List<FilterSubscription>>>();
		sessionObjectUIDPointers = new HashMap<Session, List<ObjectUIDPointer>>();
		sessionObjectDomainPointers = new HashMap<Session, List<ObjectDomainPointer>>();	
	}
	
	public void subscribe(Session session, String objectname, String uid) throws RedbackException {
		try {
			synchronized(this) {
				Map<String, List<Session>> objMap = uniqueObjectSubsriptions.get(objectname);
				if(objMap == null) {
					objMap = new HashMap<String, List<Session>>();
					uniqueObjectSubsriptions.put(objectname, objMap);
				}
				List<Session> list = objMap.get(uid);
				if(list == null) {
					list = new ArrayList<Session>();
					objMap.put(uid, list);
				}
				list.add(session);
				List<ObjectUIDPointer> pointers = sessionObjectUIDPointers.get(session);
				if(pointers == null) {
					pointers = new ArrayList<ObjectUIDPointer>();
					sessionObjectUIDPointers.put(session, pointers);
				}
				pointers.add(new ObjectUIDPointer(objectname, uid));
			}
		} catch(Exception e) {
			throw new RedbackException("Error subscribing session " + session.getId() + " to " + objectname + "." + uid , e);
		}
	}
	
	public void subscribe(Session session, String objectname, DataMap filter, String id) throws RedbackException {
		try {
			synchronized(this) {
				Map<String, List<FilterSubscription>> objMap = objectFilterSubscriptions.get(objectname);
				if(objMap == null) {
					objMap = new HashMap<String, List<FilterSubscription>>();
					objectFilterSubscriptions.put(objectname, objMap);
				}
				for(String domain: session.getUserProfile().getDomains()) {
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
						list.add(new FilterSubscription(id, session, filter));
					}
					List<ObjectDomainPointer> pointers = sessionObjectDomainPointers.get(session);
					if(pointers == null) {
						pointers = new ArrayList<ObjectDomainPointer>();
						sessionObjectDomainPointers.put(session, pointers);
					}
					pointers.add(new ObjectDomainPointer(objectname, domain));						
				}
			}
		} catch(Exception e) {
			throw new RedbackException("Error subscribing session " + session.getId() + " to filter " + id + " for " + objectname, e);
		}
	}
	
	public void unsubscribe(Session session) throws RedbackException {
		try {
			synchronized(this) {
				List<ObjectUIDPointer> p1 = sessionObjectUIDPointers.get(session);
				if(p1 != null) {
					for(ObjectUIDPointer pointer: p1) {
						Map<String, List<Session>> map = uniqueObjectSubsriptions.get(pointer.objectname);
						if(map != null) {
							List<Session> list = map.get(pointer.uid);
							if(list != null) {
								list.remove(session);
								if(list.size() == 0) {
									map.remove(pointer.uid);
									if(map.size() == 0)
										uniqueObjectSubsriptions.remove(pointer.objectname);
								}
							}
						}
					}
					sessionObjectUIDPointers.remove(session);
				}
				
				List<ObjectDomainPointer> p2 = sessionObjectDomainPointers.get(session);
				if(p2 != null) {
					for(ObjectDomainPointer pointer: p2) {
						Map<String, List<FilterSubscription>> map = objectFilterSubscriptions.get(pointer.objectname);
						if(map != null) 
						{
							List<FilterSubscription> list = map.get(pointer.domain);
							if(list != null) {
								for(int i = 0; i < list.size(); i++)
									if(list.get(i).session == session)
										list.remove(i--);
								if(list.size() == 0) {
									map.remove(pointer.domain);
									if(map.size() == 0)
										objectFilterSubscriptions.remove(pointer.objectname);
								}
							}
						}
					}
					sessionObjectDomainPointers.remove(session);
				}
			}
		} catch(Exception e) {
			throw new RedbackException("Error unsubscribing session " + session.getId(), e);
		}		
	}
	
	public List<Session> getSubscribersFor(DataMap data) throws RedbackException {
		List<Session> subscribers = new ArrayList<Session>();
		String objectName = data.getString("objectname");
		String uid = data.getString("uid");
		Map<String, List<Session>> map1 = uniqueObjectSubsriptions.get(objectName);
		if(map1 != null) {
			List<Session> list = map1.get(uid);
			if(list != null)
				for(Session session : list) 
					subscribers.add(session);
		}

		String domain = data.getString("domain");
		Map<String, List<FilterSubscription>> map2 = objectFilterSubscriptions.get(objectName);
		if(map2 != null) {
			List<FilterSubscription> list = map2.get(domain);
			if(list != null)
				for(FilterSubscription subs : list) 
					if(!subscribers.contains(subs.session) && subs.matches(data.getObject("data")))
						subscribers.add(subs.session);
		}
		return subscribers;
	}
}
