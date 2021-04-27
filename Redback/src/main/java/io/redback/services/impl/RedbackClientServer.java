package io.redback.services.impl;

import java.util.Iterator;
import java.util.List;

import io.firebus.Firebus;
import io.firebus.Payload;
import io.firebus.exceptions.FunctionErrorException;
import io.firebus.interfaces.ServiceRequestor;
import io.firebus.utils.DataList;
import io.firebus.utils.DataMap;
import io.redback.RedbackException;
import io.redback.managers.clientmanager.SubscriptionManager;
import io.redback.security.Session;
import io.redback.services.ClientServer;
import io.redback.utils.StringUtils;

public class RedbackClientServer extends ClientServer {
	protected SubscriptionManager subsManager;

	public RedbackClientServer(String n, DataMap c, Firebus f) {
		super(n, c, f);
		subsManager = new SubscriptionManager();
	}

	protected void onClientConnect(Session session) throws RedbackException {
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

	protected void requestService(Session session, String reqUid, String serviceName, DataMap request) throws RedbackException {
		try {
			Payload payload = new Payload();
			payload.metadata.put("token", session.getToken());
			payload.metadata.put("timezone", session.getTimezone());
			payload.setData(request.toString());
			firebus.requestService(serviceName, payload, new ServiceRequestor() {
				public void response(Payload payload) {
					try {
						DataMap resp = new DataMap(payload.getString());
						DataMap respWrapper = new DataMap();
						respWrapper.put("type", "serviceresponse");
						respWrapper.put("requid", reqUid);
						respWrapper.put("response", resp);
						sendStreamData(session, new Payload(respWrapper.toString()));
					} catch(Exception e2) {
					}
				}

				public void error(FunctionErrorException e) { 
					DataMap respWrapper = new DataMap();
					respWrapper.put("type", "serviceerror");
					respWrapper.put("requid", reqUid);
					respWrapper.put("error", StringUtils.rollUpExceptions(e));
					sendStreamData(session, new Payload(respWrapper.toString()));					
				}

				public void timeout() {
					DataMap respWrapper = new DataMap();
					respWrapper.put("type", "serviceerror");
					respWrapper.put("requid", reqUid);
					respWrapper.put("error", "service request timed out");
					sendStreamData(session, new Payload(respWrapper.toString()));						
				}	
			}, 10000);
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
