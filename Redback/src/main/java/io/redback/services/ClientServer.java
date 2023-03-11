package io.redback.services;

import io.firebus.Firebus;
import io.firebus.Payload;
import io.firebus.data.DataMap;
import io.firebus.information.StreamInformation;
import io.firebus.interfaces.Consumer;
import io.firebus.logging.Logger;
import io.redback.exceptions.RedbackException;
import io.redback.security.Session;
import io.redback.services.common.AuthenticatedStreamProvider;
import io.redback.services.common.StreamHandler;

public abstract class ClientServer extends AuthenticatedStreamProvider {
	
	public ClientServer(String n, DataMap c, Firebus f) {
		super(n, c, f);
		if(c.containsKey("objectupdatechannel")) {
			f.registerConsumer(c.getString("objectupdatechannel"), new Consumer() {
				public void consume(Payload payload) {
					try {
						onObjectUpdate(payload.getDataMap());
					} catch(Exception e) {
						Logger.severe("rb.client.onupdate", "Error receiving object update", e);
					}					
				}
			}, 10);			
		}
		if(c.containsKey("processnotificationchannel")) {
			f.registerConsumer(c.getString("processnotificationchannel"), new Consumer() {
				public void consume(Payload payload) {
					try {
						onNotification(payload.getDataMap());
					} catch(Exception e) {
						Logger.severe("rb.client.onnotification", "Error receiving process notification", e);
					}
				}
			}, 10);			
		}
		if(c.containsKey("chatchannel")) {
			f.registerConsumer(c.getString("chatchannel"), new Consumer() {
				public void consume(Payload payload) {
					try {
						onChatMessage(payload.getDataMap());
					} catch(Exception e) {
						Logger.severe("rb.client.onchat", "Error receiving chat", e);
					}
				}
			}, 10);			
		}		
	}

	public StreamHandler redbackAcceptAuthenticatedStream(Session session, Payload payload) throws RedbackException {
		return acceptClientStream(session, payload);
	}

	public StreamHandler redbackAcceptUnauthenticatedStream(Session session, Payload payload) throws RedbackException {
		throw new RedbackException("All client streams need to be authenticated");
	}
	
	public StreamInformation getStreamInformation() {
		return null;
	}
	
	public abstract StreamHandler acceptClientStream(Session session, Payload payload) throws RedbackException;

	protected abstract void onObjectUpdate(DataMap data) throws RedbackException;
	
	protected abstract void onNotification(DataMap data) throws RedbackException;
	
	protected abstract void onChatMessage(DataMap data) throws RedbackException;
}
