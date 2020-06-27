package io.redback.services.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import io.firebus.Firebus;
import io.firebus.Payload;
import io.firebus.StreamEndpoint;
import io.firebus.utils.DataException;
import io.firebus.utils.DataMap;
import io.redback.RedbackException;
import io.redback.security.Session;
import io.redback.services.SignalServer;
import io.redback.utils.FilterProcessor;

public class RedbackSignalServer extends SignalServer {

	protected abstract class Subscription {
		public abstract boolean matches(DataMap signal);
	};
	
	protected class ObjectSubscription extends Subscription {
		public String object;
		public String uid;
		
		public ObjectSubscription(String o, String u) {
			object = o;
			uid = u;
		}
		
		public boolean matches(DataMap signal) {
			if(signal.getString("type").equals("objectchange") && signal.getString("object.objectname").equals(object) && signal.getString("object.uid").equals(uid))
				return true;
			else
				return false;
		}
	}
	
	protected class FilterSubscription extends Subscription {
		public String id;
		public DataMap filter;
		public String object;
		
		public FilterSubscription(String i, String o, DataMap f) {
			id = i;
			object = o;
			filter = f;
		}

		public boolean matches(DataMap signal) {
			if(signal.getString("type").equals("objectchange") && signal.getString("object.objectname").equals(object))
				return FilterProcessor.apply(signal.getObject("object.data"), filter);
			else
				return false;
		}
	}
	
	protected Map<Session, List<Subscription>> subscriptions;
	
	public RedbackSignalServer(String n, DataMap c, Firebus f) {
		super(n, c, f);
		subscriptions = new HashMap<Session, List<Subscription>>();
	}

	protected void onNewStream(Session session, StreamEndpoint streamEndpoint) throws RedbackException {
	}

	protected void onSignal(DataMap signal) throws RedbackException {
		Iterator<Session> it = subscriptions.keySet().iterator();
		while(it.hasNext()) {
			Session session = it.next();
			List<Subscription> list = subscriptions.get(session);
			for(Subscription subscription : list) {
				if(subscription.matches(signal)) {
					sendStreamData(session, new Payload(signal.toString()));
					break;
				}
			}
		}
	}

	protected void onNewStream(Session session) throws RedbackException {
		List<Subscription> list = new ArrayList<Subscription>();
		subscriptions.put(session, list);
	}

	protected void onStreamData(Session session, Payload payload) throws RedbackException {
		try {
			DataMap req = new DataMap(payload.getString());
			if(req.getString("action").equals("subscribe")) {
				Subscription subscription = null;
				if(req.getString("type").equals("objectchange")) {
					subscription = new ObjectSubscription(req.getString("objectname"), req.getString("uid"));
				} if(req.getString("type").equals("objectfilter")) {
					List<Subscription> list = subscriptions.get(session);
					for(Subscription subs: list) 
						if(subs instanceof FilterSubscription && ((FilterSubscription)subs).id.equals(req.getString("id")))
							subscription = subs;
					if(subscription != null) {
						((FilterSubscription)subscription).filter = req.getObject("filter");
					} else {
						subscription = new FilterSubscription(req.getString("id"), req.getString("objectname"), req.getObject("filter"));
						subscriptions.get(session).add(subscription);
					}
				}
			} else if(req.getString("action").equals("unsubscribe")) {
				subscriptions.get(session).clear();
			}
		} catch(DataException e) {
			//TODO: Handle this
		}
		
	}

	protected void onStreamClose(Session session) throws RedbackException {
		subscriptions.remove(session);		
	}

	public void clearCaches() {
		
	}


}
