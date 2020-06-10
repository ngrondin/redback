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
		System.out.println("User connected " + session.getUserProfile().getUsername());
	}

	protected void receiveTextMessage(Session session, List<String> to, String message) throws RedbackException {
		System.out.println("Chat: received text from " + session.getUserProfile().getUsername() + " for " + to + " : " + message.trim());
		if(to != null) {
			for(int i = 0; i < to.size(); i++) {
				for(int j = 0; j < sessions.size(); j++) {
					if(sessions.get(j).getUserProfile().getUsername().equals(to.get(i))) {
						System.out.println("Chat: sending text to " + to.get(i));
						sendTextMessage(sessions.get(j), session.getUserProfile().getUsername(), message);
					}
				}
			}
		}
	}

	protected void receiveSoundPacket(Session session, List<String> to, byte[] bytes) throws RedbackException {
		
	}

	protected void userDisconnected(Session session) throws RedbackException {
		sessions.remove(session);
		System.out.println("User disconnected " + session.getUserProfile().getUsername());
	}

	
	protected Session getSessionByUsername(String username) {
		for(int i = 0; i < sessions.size(); i++) {
			if(sessions.get(i).getUserProfile().getUsername().equals(username))
				return sessions.get(i);
		}
		return null;
	}

	protected List<String> getConnectedUsers(Session session) throws RedbackException {
		System.out.println("Chat: listing users");
		List<String> resp = new ArrayList<String>();
		for(int i = 0; i < sessions.size(); i++) {
			String username = sessions.get(i).getUserProfile().getUsername(); 
			if(!resp.contains(username) && !session.getUserProfile().getUsername().equals(username)) {
				System.out.println("Chat: " + sessions.get(i).getUserProfile().getUsername());
				resp.add(username);
			}
		}
		return resp;
	}


	public void clearCaches() {
		
	}

}
