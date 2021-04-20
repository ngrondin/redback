package io.redback.services.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import io.firebus.Firebus;
import io.firebus.Payload;
import io.firebus.utils.DataFilter;
import io.firebus.utils.DataList;
import io.firebus.utils.DataMap;
import io.redback.RedbackException;
import io.redback.security.Session;
import io.redback.services.SignalServer;

public class RedbackSignalServer extends SignalServer {
/*
	protected abstract class Subscription {
		public abstract boolean matches(DataMap signal);
	};
*/
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
	
	public RedbackSignalServer(String n, DataMap c, Firebus f) {
		super(n, c, f);
		uniqueObjectSubsriptions = new HashMap<String, Map<String, List<Session>>>();
		objectFilterSubscriptions = new HashMap<String, Map<String, List<FilterSubscription>>>();
		sessionObjectUIDPointers = new HashMap<Session, List<ObjectUIDPointer>>();
		sessionObjectDomainPointers = new HashMap<Session, List<ObjectDomainPointer>>();
	}

	protected void onSignal(DataMap signal) throws RedbackException {
		String type = signal.getString("type");
		//System.out.println("RBSI Received signal " + signal.toString().hashCode() + " : " + signal.toString(0, true)); //Temp Logging
		if(type.equals("objectupdate")) {
			String objectName = signal.getString("object.objectname");
			String uid = signal.getString("object.uid");
			Map<String, List<Session>> map = uniqueObjectSubsriptions.get(objectName);
			if(map != null) {
				List<Session> list = map.get(uid);
				if(list != null)
					for(Session session : list) 
						sendStreamData(session, new Payload(signal.toString()));
			}
		} else if(type.equals("objectcreate")) {
			String objectName = signal.getString("object.objectname");
			String domain = signal.getString("object.domain");
			Map<String, List<FilterSubscription>> map = objectFilterSubscriptions.get(objectName);
			if(map != null) {
				List<FilterSubscription> list = map.get(domain);
				if(list != null)
					for(FilterSubscription subs : list) 
						if(subs.matches(signal.getObject("object.data")))
							sendStreamData(subs.session, new Payload(signal.toString()));
			}
		} else if(type.equals("processnotification") || type.equals("processinteractioncompletion")) {
			DataList to = type.equals("processnotification") ? signal.getObject("notification").getList("to") : signal.getObject("interaction").getList("to"); 
			if(to != null && to.size() > 0) {
				Iterator<Session> it = sessionToEndpoint.keySet().iterator();
				while(it.hasNext()) {
					Session session = it.next();
					for(int i = 0; i < to.size(); i++) {
						String username = to.getString(i);
						if(session.getUserProfile().getUsername().equals(username)) {
							sendStreamData(session, new Payload(signal.toString()));
						}
					}
				}
			}
		}
	}

	protected void onNewStream(Session session) throws RedbackException {
		sendStreamData(session, new Payload((new DataMap("action", "heartbeat")).toString()));
	}

	protected void onStreamData(Session session, Payload payload) throws RedbackException {
		try {
			synchronized(this) {
				DataMap req = new DataMap(payload.getString());
				if(req.getString("action").equals("subscribe")) {
					String type = req.getString("type");
					String object = req.getString("objectname");
					if(type.equals("objectupdate")) {
						String uid = req.getString("uid");
						Map<String, List<Session>> objMap = uniqueObjectSubsriptions.get(object);
						if(objMap == null) {
							objMap = new HashMap<String, List<Session>>();
							uniqueObjectSubsriptions.put(object, objMap);
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
						pointers.add(new ObjectUIDPointer(object, uid));
					} else if(type.equals("objectcreate")) {
						Map<String, List<FilterSubscription>> objMap = objectFilterSubscriptions.get(object);
						if(objMap == null) {
							objMap = new HashMap<String, List<FilterSubscription>>();
							objectFilterSubscriptions.put(object, objMap);
						}
						for(String domain: session.getUserProfile().getDomains()) {
							List<FilterSubscription> list = objMap.get(domain);
							if(list == null) {
								list = new ArrayList<FilterSubscription>();
								objMap.put(domain, list);
							}
							FilterSubscription filterSubscription = null;
							for(FilterSubscription fs: list) 
								if(fs.id.equals(req.getString("id"))) 
									filterSubscription = fs;
							if(filterSubscription != null) {
								filterSubscription.filter = new DataFilter(req.getObject("filter"));
							} else {
								list.add(new FilterSubscription(req.getString("id"), session, req.getObject("filter")));
							}
							List<ObjectDomainPointer> pointers = sessionObjectDomainPointers.get(session);
							if(pointers == null) {
								pointers = new ArrayList<ObjectDomainPointer>();
								sessionObjectDomainPointers.put(session, pointers);
							}
							pointers.add(new ObjectDomainPointer(object, domain));						
						}
					}
				} else if(req.getString("action").equals("unsubscribe")) {
					unsubscribe(session);
				} else if(req.getString("action").equals("heartbeat")) {
					sendStreamData(session, new Payload((new DataMap("action", "heartbeat")).toString()));
				}
			}
		} catch(Exception e) {
			throw new RedbackException("Error processing signal subscriptions for payload = " + payload.getString() + "", e);
		}
		
	}

	protected void onStreamClose(Session session) throws RedbackException {
		unsubscribe(session);
	}

	protected void unsubscribe(Session session) {
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
			
		}
	}
	
	
	public void clearCaches() {
		
	}


}
