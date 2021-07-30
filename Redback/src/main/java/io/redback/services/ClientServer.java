package io.redback.services;

import java.util.logging.Logger;

import io.firebus.Firebus;
import io.firebus.Payload;
import io.firebus.information.StreamInformation;
import io.firebus.interfaces.Consumer;
import io.firebus.utils.DataMap;
import io.redback.exceptions.RedbackException;
import io.redback.security.Session;
import io.redback.services.common.AuthenticatedStreamProvider;
import io.redback.services.common.StreamHandler;
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

	public StreamHandler redbackAuthenticatedStream(Session session, Payload payload) throws RedbackException {
		return clientStream(session);
	}

	public StreamHandler redbackUnauthenticatedStream(Session session, Payload payload) throws RedbackException {
		throw new RedbackException("All client streams need to be authenticated");
	}
	
	public StreamInformation getStreamInformation() {
		return null;
	}
	
	public int getStreamIdleTimeout() {
		return 300000;
	}
	
	public abstract StreamHandler clientStream(Session session) throws RedbackException;


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

	protected abstract void onObjectUpdate(DataMap data) throws RedbackException;
	
	protected abstract void onNotification(DataMap data) throws RedbackException;
	
	protected abstract void onChatMessage(DataMap data) throws RedbackException;
}
