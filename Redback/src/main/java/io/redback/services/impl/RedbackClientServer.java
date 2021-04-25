package io.redback.services.impl;

import java.util.Iterator;
import java.util.List;

import io.firebus.Firebus;
import io.firebus.Payload;
import io.firebus.utils.DataList;
import io.firebus.utils.DataMap;
import io.redback.RedbackException;
import io.redback.managers.clientmanager.SubscriptionManager;
import io.redback.security.Session;
import io.redback.services.ClientServer;

public class RedbackClientServer extends ClientServer {
	protected SubscriptionManager subsManager;

	public RedbackClientServer(String n, DataMap c, Firebus f) {
		super(n, c, f);
		subsManager = new SubscriptionManager();
	}

	protected void onClientConnect(Session session) throws RedbackException {
		//DataMap hello = new DataMap("type", "hello");
		//System.out.println("RBCS sending hello message to stream " + session.getId() + " : " + hello.toString().hashCode()); //Temp Logging
		//this.sendStreamData(session, new Payload(hello.toString()));
		System.out.println("New client connected for " + session.getUserProfile().getUsername());
	}

	protected void onClientLeave(Session session) throws RedbackException {
		subsManager.unsubscribe(session);
		System.out.println("Client disconnected for " + session.getUserProfile().getUsername());
	}

	protected void subscribeFilter(Session session, String objectname, DataMap filter, String id) throws RedbackException {
		subsManager.subscribe(session, objectname, filter, id);
	}

	protected void subscribeObject(Session session, String objectname, String uid) throws RedbackException {
		subsManager.subscribe(session, objectname, uid);
	}

	protected DataMap requestService(Session session, String serviceName, DataMap request) throws RedbackException {
		try {
			Payload payload = new Payload();
			payload.metadata.put("token", session.getToken());
			payload.metadata.put("timezone", session.getTimezone());
			payload.setData(request.toString());;
			Payload resp = firebus.requestService(serviceName, payload);
			return new DataMap(resp.getString());
		} catch(Exception e) {
			throw new RedbackException("Error requesting service for client", e);
		}
	}

	protected void onObjectUpdate(DataMap data) throws RedbackException {
		List<Session> subscribers = subsManager.getSubscribersFor(data);
		DataMap wrapper = new DataMap();
		wrapper.put("type", "objectupdate");
		wrapper.put("object", data);
		Payload payload = new Payload(wrapper.toString());
		for(Session session : subscribers) {
			this.sendStreamData(session, payload);
		}
	}

	protected void onNotification(DataMap data) throws RedbackException {
		DataList to = data.getList("to"); 
		if(to != null && to.size() > 0) {
			DataMap wrapper = new DataMap();
			wrapper.put("type", "notification");
			wrapper.put("notification", data);
			Payload payload = new Payload(wrapper.toString());
			Iterator<Session> it = sessionToEndpoint.keySet().iterator();
			while(it.hasNext()) {
				Session session = it.next();
				for(int i = 0; i < to.size(); i++) {
					String username = to.getString(i);
					if(session.getUserProfile().getUsername().equals(username)) {
						sendStreamData(session, payload);
					}
				}
			}
		}		
	}

	protected void onChatMessage(DataMap data) throws RedbackException {
		
	}



}
