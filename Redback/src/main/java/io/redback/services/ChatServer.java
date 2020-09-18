package io.redback.services;

import java.util.ArrayList;
import java.util.List;
import io.firebus.Firebus;
import io.firebus.Payload;
import io.firebus.information.StreamInformation;
import io.firebus.utils.DataList;
import io.firebus.utils.DataMap;
import io.redback.RedbackException;
import io.redback.security.Session;


public abstract class ChatServer extends AuthenticatedStreamProvider {
	//private Logger logger = Logger.getLogger("io.redback");
	
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
				String action = msg.getString("action"); 
				if(action.equals("sendtext")) {
					List<String> to = new ArrayList<String>();
					for(int i = 0; i < msg.getList("to").size(); i++)
						to.add(msg.getList("to").getString(i));
					String chatId = msg.getString("chatid");
					String objectname = msg.getString("object");
					String uid = msg.getString("uid");
					String body = msg.getString("body");
					receiveTextMessage(session, to, chatId, objectname, uid, body);			
				} else if(action.equals("listusers")) {
					List<String> list = getConnectedUsers(session);
					DataMap resp = new DataMap();
					resp.put("type", "users");
					resp.put("users", new DataList());
					for(int i = 0; i < list.size(); i++)
						resp.getList("users").add(list.get(i));
					sendStreamData(session, new Payload(resp.toString()));
				}
			}
		} catch(Exception e) {
			throw new RedbackException("Error receiving chat message", e);
		}
	}

	protected void onStreamClose(Session session) throws RedbackException {
		userDisconnected(session);
	}
	
	protected void sendTextMessage(Session session, String from, String chatId, String object, String uid, String body) {
		DataMap msg = new DataMap();
		msg.put("type", "text");
		msg.put("from", from);
		msg.put("chatid", chatId);
		msg.put("object", object);
		msg.put("uid", uid);
		msg.put("body", body);
		sendStreamData(session, new Payload(msg.toString()));
	}

	protected abstract void userConnected(Session session) throws RedbackException;

	protected abstract void receiveTextMessage(Session session, List<String> to, String chatId, String object, String uid, String message) throws RedbackException;

	protected abstract void receiveSoundPacket(Session session, List<String> to, String chatId, String object, String uid, byte[] bytes) throws RedbackException;

	protected abstract void userDisconnected(Session session) throws RedbackException;
	
	protected abstract List<String> getConnectedUsers(Session session) throws RedbackException;

}
