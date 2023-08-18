package io.redback.services;

import io.firebus.Firebus;
import io.firebus.Payload;
import io.firebus.StreamEndpoint;
import io.firebus.data.DataMap;
import io.firebus.information.StreamInformation;
import io.firebus.interfaces.Consumer;
import io.firebus.logging.Logger;
import io.redback.exceptions.RedbackException;
import io.redback.security.Session;
import io.redback.services.common.AuthenticatedStreamProvider;

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
						onChatUpdate(payload.getDataMap());
					} catch(Exception e) {
						Logger.severe("rb.client.onchat", "Error receiving chat", e);
					}
				}
			}, 10);			
		}		
	}

	public Payload redbackAcceptAuthenticatedStream(Session session, Payload payload, StreamEndpoint streamEndpoint) throws RedbackException {
		return acceptClientStream(session, payload, streamEndpoint);
	}

	public Payload redbackAcceptUnauthenticatedStream(Session session, Payload payload, StreamEndpoint streamEndpoint) throws RedbackException {
		throw new RedbackException("All client streams need to be authenticated");
	}
	
	public StreamInformation getStreamInformation() {
		return null;
	}
	
	public abstract Payload acceptClientStream(Session session, Payload payload, StreamEndpoint streamEndpoint) throws RedbackException;

	protected abstract void onObjectUpdate(DataMap data) throws RedbackException;
	
	protected abstract void onNotification(DataMap data) throws RedbackException;
	
	protected abstract void onChatUpdate(DataMap data) throws RedbackException;
}
