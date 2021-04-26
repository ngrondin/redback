package io.redback.services;

import java.util.logging.Logger;

import io.firebus.Firebus;
import io.firebus.Payload;
import io.firebus.information.StreamInformation;
import io.firebus.interfaces.Consumer;
import io.firebus.utils.DataMap;
import io.redback.RedbackException;
import io.redback.security.Session;
import io.redback.utils.StringUtils;

public abstract class ClientServer extends AuthenticatedStreamProvider {
	private Logger logger = Logger.getLogger("io.redback");
	
	public ClientServer(String n, DataMap c, Firebus f) {
		super(n, c, f);
		if(c.containsKey("objectupdatechannel")) {
			f.registerConsumer(c.getString("objectupdatechannel"), new Consumer() {
				public void consume(Payload payload) {
					_onOjectUpdate(payload);
				}
			}, 10);			
		}
		if(c.containsKey("processnotificationchannel")) {
			f.registerConsumer(c.getString("processnotificationchannel"), new Consumer() {
				public void consume(Payload payload) {
					_onNotification(payload);
				}
			}, 10);			
		}
		if(c.containsKey("chatchannel")) {
			f.registerConsumer(c.getString("chatchannel"), new Consumer() {
				public void consume(Payload payload) {
					_onChatMessage(payload);
				}
			}, 10);			
		}		
	}

	public StreamInformation getStreamInformation() {
		return null;
	}

	protected void onNewStream(Session session) throws RedbackException {
		onClientConnect(session);
	}

	protected void onStreamData(Session session, Payload payload) throws RedbackException {
		try {
			DataMap msg = new DataMap(payload.getString());
			String type = msg.getString("type");
			if(type != null) {
				if(type.equals("subscribe")) {
					if(msg.containsKey("uid")) {
						this.subscribeObject(session, msg.getString("objectname"), msg.getString("uid"));
					} else if(msg.containsKey("filter")) {
						this.subscribeFilter(session, msg.getString("objectname"), msg.getObject("filter"), msg.getString("id"));
					}
				} else if(type.equals("servicerequest")) {
					String reqUid = msg.getString("requid");
					String serviceName = msg.getString("servicename");
					DataMap request = msg.getObject("request");
					DataMap respWrapper = new DataMap();
					try {
						DataMap resp = requestService(session, serviceName, request);
						respWrapper.put("type", "serviceresponse");
						respWrapper.put("requid", reqUid);
						respWrapper.put("response", resp);
					} catch(Exception e) {
						respWrapper.put("type", "serviceerror");
						respWrapper.put("requid", reqUid);
						respWrapper.put("error", StringUtils.rollUpExceptions(e));
					}
					sendStreamData(session, new Payload(respWrapper.toString()));
				} else if(type.equals("heartbeat")) {
					sendStreamData(session, new Payload(new DataMap("type", "heartbeat").toString()));
				}
			}
		} catch(Exception e) {
			throw new RedbackException("Error processing client message", e);
		}
	}

	protected void onStreamClose(Session session) throws RedbackException {
		onClientLeave(session);
	}
	
	private void _onOjectUpdate(Payload payload) {
		try {
			onObjectUpdate(new DataMap(payload.getString()));
		} catch(Exception e) {
			logger.severe(StringUtils.getStackTrace(e));
		}
	}
	
	private void _onNotification(Payload payload) {
		try {
			onNotification(new DataMap(payload.getString()));
		} catch(Exception e) {
			logger.severe(StringUtils.getStackTrace(e));
		}
	}
	
	private void _onChatMessage(Payload payload) {
		try {
			onChatMessage(new DataMap(payload.getString()));
		} catch(Exception e) {
			logger.severe(StringUtils.getStackTrace(e));
		}
	}

	public void clearCaches() {
		
	}
	
	protected abstract void onClientConnect(Session session) throws RedbackException;
	
	protected abstract void onClientLeave(Session session) throws RedbackException;
	
	protected abstract void subscribeFilter(Session session, String objectname, DataMap filter, String id) throws RedbackException;

	protected abstract void subscribeObject(Session session, String objectname, String uid) throws RedbackException;
	
	protected abstract DataMap requestService(Session session, String serviceName, DataMap request) throws RedbackException;

	protected abstract void onObjectUpdate(DataMap data) throws RedbackException;
	
	protected abstract void onNotification(DataMap data) throws RedbackException;
	
	protected abstract void onChatMessage(DataMap data) throws RedbackException;

}
