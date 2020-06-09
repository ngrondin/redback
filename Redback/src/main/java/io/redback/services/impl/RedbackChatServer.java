package io.redback.services.impl;

import java.util.ArrayList;
import java.util.List;

import io.firebus.Firebus;
import io.firebus.utils.DataMap;
import io.redback.RedbackException;
import io.redback.security.Session;
import io.redback.services.ChatServer;

public class RedbackChatServer extends ChatServer {

	protected List<Session> sessions;
	
	public RedbackChatServer(String n, DataMap c, Firebus f) {
		super(n, c, f);
		sessions = new ArrayList<Session>();
	}

	protected void userConnected(Session session) throws RedbackException {
		sessions.add(session);
	}

	protected void receiveTextMessage(Session session, List<String> to, String message) throws RedbackException {
		if(to != null) {
			for(int i = 0; i < to.size(); i++) {
				Session dest = getSessionByUsername(to.get(i));
				sendTextMessage(dest, session.getUserProfile().getUsername(), message);
			}
		}
	}

	protected void receiveSoundPacket(Session session, List<String> to, byte[] bytes) throws RedbackException {
		
	}

	protected void userDisconnected(Session session) throws RedbackException {
		sessions.remove(session);
	}

	
	protected Session getSessionByUsername(String username) {
		for(int i = 0; i < sessions.size(); i++) {
			if(sessions.get(i).getUserProfile().getUsername().equals(username))
				return sessions.get(i);
		}
		return null;
	}

	protected List<String> getConnectedUsers() throws RedbackException {
		List<String> resp = new ArrayList<String>();
		for(int i = 0; i < sessions.size(); i++) {
			resp.add(sessions.get(i).getUserProfile().getUsername());
		}
		return resp;
	}


	public void clearCaches() {
		
	}

}
