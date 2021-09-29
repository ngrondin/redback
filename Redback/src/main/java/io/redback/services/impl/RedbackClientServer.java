package io.redback.services.impl;


import io.firebus.Firebus;
import io.firebus.Payload;
import io.firebus.data.DataMap;
import io.redback.exceptions.RedbackException;
import io.redback.managers.clientmanager.ClientManager;
import io.redback.security.Session;
import io.redback.services.ClientServer;
import io.redback.services.common.StreamHandler;

public class RedbackClientServer extends ClientServer {
	protected ClientManager clientManager;

	public RedbackClientServer(String n, DataMap c, Firebus f) {
		super(n, c, f);
		clientManager = new ClientManager(n, c, f);
	}
	
	public StreamHandler acceptClientStream(Session session, Payload payload) throws RedbackException {
		return clientManager.acceptClientConnection(session, payload);
	}
	
	public int getStreamIdleTimeout() {
		return 15000;
	}

	protected void onObjectUpdate(DataMap data) throws RedbackException {
		clientManager.onObjectUpdate(data);
	}

	protected void onNotification(DataMap data) throws RedbackException {
		clientManager.onNotification(data);
	}

	protected void onChatMessage(DataMap data) throws RedbackException {
		clientManager.onChatMessage(data);
	}
	
	public DataMap getStatus() {
		return clientManager.getStatus();
	}
}
