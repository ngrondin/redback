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

public class RedbackSignalServer extends SignalServer {

	protected Map<Session, List<String>> subscriptions;
	
	public RedbackSignalServer(String n, DataMap c, Firebus f) {
		super(n, c, f);
		subscriptions = new HashMap<Session, List<String>>();
	}

	protected void onNewStream(Session session, StreamEndpoint streamEndpoint) throws RedbackException {
	}

	protected void onSignal(String signal) throws RedbackException {
		Iterator<Session> it = subscriptions.keySet().iterator();
		while(it.hasNext()) {
			Session session = it.next();
			List<String> list = subscriptions.get(session);
			for(int i = 0; i < list.size(); i++)
				if(list.get(i).equals(signal))
					sendStreamData(session, new Payload((new DataMap("signal", signal)).toString()));
		}
	}

	@Override
	protected void onNewStream(Session session) throws RedbackException {
		List<String> list = new ArrayList<String>();
		subscriptions.put(session, list);
	}

	@Override
	protected void onStreamData(Session session, Payload payload) throws RedbackException {
		try {
			DataMap req = new DataMap(payload.getString());
			if(req.containsKey("subscribe")) {
				subscriptions.get(session).add(req.getString("subscribe"));
			} else if(req.containsKey("unsubscribe")) {
				String selector = req.getString("unsubscribe");
				if(selector.equals("*"))
					subscriptions.get(session).clear();
				else
					subscriptions.get(session).remove(selector);
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
