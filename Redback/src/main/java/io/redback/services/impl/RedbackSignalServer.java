package io.redback.services.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.firebus.Firebus;
import io.firebus.Payload;
import io.firebus.StreamEndpoint;
import io.firebus.utils.DataMap;
import io.redback.RedbackException;
import io.redback.security.Session;
import io.redback.services.SignalServer;
import io.redback.utils.FilterProcessor;

public class RedbackSignalServer extends SignalServer {

	protected abstract class Subscription {
		public abstract boolean matches(DataMap signal);
	};
	

	protected class FilterSubscription extends Subscription {
		public String id;
		public DataMap filter;
		public Session session;
		
		public FilterSubscription(String i, Session s, DataMap f) {
			id = i;
			session = s;
			filter = f;
		}

		public boolean matches(DataMap data) {
			return FilterProcessor.apply(data, filter);
		}
	}
	
	protected Map<String, Map<String, List<Session>>> objectUpdate;
	protected Map<String, Map<String, List<FilterSubscription>>> objectCreate;
	protected Map<Session, List<String[]>> sessionToObjectUpdate;
	protected Map<Session, List<String[]>> sessionToObjectCreate;
	
	public RedbackSignalServer(String n, DataMap c, Firebus f) {
		super(n, c, f);
		objectUpdate = new HashMap<String, Map<String, List<Session>>>();
		objectCreate = new HashMap<String, Map<String, List<FilterSubscription>>>();
		sessionToObjectUpdate = new HashMap<Session, List<String[]>>();
		sessionToObjectCreate = new HashMap<Session, List<String[]>>();
	}

	protected void onNewStream(Session session, StreamEndpoint streamEndpoint) throws RedbackException {
	}

	protected void onSignal(DataMap signal) throws RedbackException {
		String type = signal.getString("type");
		if(type.equals("objectupdate")) {
			String objectName = signal.getString("object.objectname");
			String uid = signal.getString("object.uid");
			Map<String, List<Session>> map = objectUpdate.get(objectName);
			if(map != null) {
				List<Session> list = map.get(uid);
				if(list != null)
					for(Session session : list) 
						sendStreamData(session, new Payload(signal.toString()));
			}
		} else if(type.equals("objectcreate")) {
			String objectName = signal.getString("object.objectname");
			String uid = signal.getString("object.uid");
			Map<String, List<FilterSubscription>> map = objectCreate.get(objectName);
			if(map != null) {
				List<FilterSubscription> list = map.get(uid);
				if(list != null)
					for(FilterSubscription subs : list) 
						if(subs.matches(signal.getObject("object.data")))
							sendStreamData(subs.session, new Payload(signal.toString()));
			}
		}
	}

	protected void onNewStream(Session session) throws RedbackException {

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
						Map<String, List<Session>> objMap = objectUpdate.get(object);
						if(objMap == null) {
							objMap = new HashMap<String, List<Session>>();
							objectUpdate.put(object, objMap);
						}
						List<Session> list = objMap.get(uid);
						if(list == null) {
							list = new ArrayList<Session>();
							objMap.put(uid, list);
						}
						list.add(session);
						List<String[]> pointers = sessionToObjectUpdate.get(session);
						if(pointers == null) {
							pointers = new ArrayList<String[]>();
							sessionToObjectUpdate.put(session, pointers);
						}
						pointers.add(new String[] {object, uid});
					} else if(type.equals("objectcreate")) {
						Map<String, List<FilterSubscription>> objMap = objectCreate.get(object);
						if(objMap == null) {
							objMap = new HashMap<String, List<FilterSubscription>>();
							objectCreate.put(object, objMap);
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
								filterSubscription.filter = req.getObject("filter");
							} else {
								list.add(new FilterSubscription(req.getString("id"), session, req.getObject("filter")));
							}
							List<String[]> pointers = sessionToObjectCreate.get(session);
							if(pointers == null) {
								pointers = new ArrayList<String[]>();
								sessionToObjectCreate.put(session, pointers);
							}
							pointers.add(new String[] {object, domain});						
						}
					}
				} else if(req.getString("action").equals("unsubscribe")) {
					unsubscribe(session);
				}
			}
		} catch(Exception e) {
			throw new RedbackException("Error process signal subscriptions", e);
		}
		
	}

	protected void onStreamClose(Session session) throws RedbackException {
		unsubscribe(session);
	}

	protected synchronized void unsubscribe(Session session) {
		List<String[]> pointers = sessionToObjectUpdate.get(session);
		if(pointers != null) {
			for(String[] pointer: pointers) {
				objectUpdate.get(pointer[0]).get(pointer[1]).remove(session);
				if(objectUpdate.get(pointer[0]).get(pointer[1]).size() == 0) {
					objectUpdate.get(pointer[0]).remove(pointer[1]);
					if(objectUpdate.get(pointer[0]).size() == 0)
						objectUpdate.remove(pointer[0]);
				}
			}
			sessionToObjectUpdate.remove(session);
		}
		
		pointers = sessionToObjectCreate.get(session);
		if(pointers != null) {
			for(String[] pointer: pointers) {
				List<FilterSubscription> list = objectCreate.get(pointer[0]).get(pointer[1]);
				if(list != null) {
					for(FilterSubscription fs : list) 
						if(fs.session == session)
							list.remove(fs);
					if(objectCreate.get(pointer[0]).get(pointer[1]).size() == 0) {
						objectCreate.get(pointer[0]).remove(pointer[1]);
						if(objectCreate.get(pointer[0]).size() == 0)
							objectCreate.remove(pointer[0]);
					}
				}
			}
			sessionToObjectCreate.remove(session);
		}
	}
	
	
	public void clearCaches() {
		
	}


}
