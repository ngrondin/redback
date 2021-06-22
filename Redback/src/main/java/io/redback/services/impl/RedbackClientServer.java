package io.redback.services.impl;


import io.firebus.Firebus;
import io.firebus.utils.DataMap;
import io.redback.RedbackException;
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
	
	public StreamHandler clientStream(Session session) throws RedbackException {
		return clientManager.onClientConnect(session);
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

	public void configure() {

	}

	public void start() {
		
	}	
}
