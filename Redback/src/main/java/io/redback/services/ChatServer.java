package io.redback.services;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import io.firebus.Firebus;
import io.firebus.Payload;
import io.firebus.information.StreamInformation;
import io.firebus.utils.DataMap;
import io.redback.RedbackException;
import io.redback.security.Session;


public abstract class ChatServer extends AuthenticatedStreamProvider {
	private Logger logger = Logger.getLogger("io.redback");
	
	public ChatServer(String n, DataMap c, Firebus f) {
		super(n, c, f);
	}

	public StreamInformation getStreamInformation() {
		return null;
	}

	protected void onNewStream(Session session) throws RedbackException {
		userConnected(session);
	}

	protected void onStreamData(Session session, Payload payload) throws RedbackException {
		try {
			String mime = payload.metadata.get("mime");
			if(mime == null || (mime != null && mime.equals("application/json"))) {
				DataMap msg = new DataMap(payload.getString());
				String type = msg.getString("type"); 
				if(type.equals("text")) {
					List<String> to = new ArrayList<String>();
					for(int i = 0; i < msg.getList("to").size(); i++)
						to.add(msg.getList("to").getString(i));
					receiveTextMessage(session, to, msg.getString("body"));			
				}
			}
		} catch(Exception e) {
			throw new RedbackException("Error receiving chat message", e);
		}
	}

	protected void onStreamClose(Session session) throws RedbackException {
		userDisconnected(session);
	}
	
	protected void sendTextMessage(Session session, String from, String message) {
		DataMap msg = new DataMap();
		msg.put("type", "text");
		msg.put("from", from);
		msg.put("body", message);
		sendStreamData(session, new Payload(msg.toString()));
	}

	protected abstract void userConnected(Session session) throws RedbackException;

	protected abstract void receiveTextMessage(Session session, List<String> to, String message) throws RedbackException;

	protected abstract void receiveSoundPacket(Session session, List<String> to, byte[] bytes) throws RedbackException;

	protected abstract void userDisconnected(Session session) throws RedbackException;
	
	protected abstract List<String> getConnectedUsers() throws RedbackException;

}
