package io.redback.services.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import io.firebus.Firebus;
import io.firebus.Payload;
import io.firebus.data.DataList;
import io.firebus.data.DataMap;
import io.redback.client.DataClient;
import io.redback.exceptions.RedbackException;
import io.redback.exceptions.RedbackInvalidRequestException;
import io.redback.security.Session;
import io.redback.services.ChatServer;
import io.redback.utils.CollectionConfig;
import io.redback.utils.Convert;

public class RedbackChatServer extends ChatServer {
	protected DataClient dataClient;
	protected CollectionConfig userCollection;
	protected CollectionConfig conversationCollection;
	protected CollectionConfig messageCollection;
	protected String clientPublishChannel;

	public RedbackChatServer(String n, DataMap c, Firebus f) {
		super(n, c, f);
		String dataService = config.getString("dataservice");
		if(dataService != null) {
			dataClient = new DataClient(firebus, dataService);
			userCollection = new CollectionConfig(config.getObject("usercollection"), "rbch_user");
			conversationCollection = new CollectionConfig(config.getObject("conversationcollection"), "rbch_conversation");
			messageCollection = new CollectionConfig(config.getObject("messagecollection"), "rbch_message");
		}
		clientPublishChannel = config.getString("clientpublishchannel");
	}
	
	private List<DataMap> listCanonicalData(Session session, CollectionConfig collection, DataMap filter) throws RedbackException {
		DataMap fullFilter = filter != null ? collection.convertObjectToSpecific(filter) : new DataMap();
		DataMap domainFilter = session.getDomainFilterClause();
		if(domainFilter != null)
			fullFilter.put(collection.getField("domain"), domainFilter);
		DataMap result = dataClient.getData(collection.getName(), collection.convertObjectToSpecific(fullFilter));
		DataList resultList = result.getList("result");
		List<DataMap> list = new ArrayList<DataMap>();
		for(int i = 0; i < resultList.size(); i++) 
			list.add(collection.convertObjectToCanonical(resultList.getObject(i)));
		return list;
	}
	
	private ChatUserInfo toChatUserInfo(DataMap data) {
		ChatUserInfo info = new ChatServer.ChatUserInfo();
		info.id = data.getString("_id");
		info.username = data.getString("username");
		info.fullname = data.getString("fullname");
		info.available = true;
		return info;
	}
	
	private ChatConversation toChatConversation(DataMap data) {
		ChatServer.ChatConversation convo = new ChatServer.ChatConversation();
		convo.id = data.getString("_id");
		convo.name = data.getString("name");
		convo.owner = data.getString("owner");
		convo.users = new ArrayList<String>();
		DataList list = data.getList("users");
		for(int i = 0; i < list.size(); i++) 
			convo.users.add(list.getString(i));
		return convo;
	}
	
	private ChatMessage toChatMessage(DataMap data) {
		ChatServer.ChatMessage msg = new ChatServer.ChatMessage();
		msg.id = data.getString("_id");
		msg.conversationId = data.getString("conversation");
		msg.date = data.getDate("date");
		msg.from = data.getString("from");
		msg.body = data.getString("body");
		msg.readby = new ArrayList<String>();
		DataList list = data.getList("readby");
		for(int i = 0; i < list.size(); i++) 
			msg.readby.add(list.getString(i));
		return msg;
	}
	
	protected List<ChatUserInfo> listUsers(Session session) throws RedbackException {
		List<DataMap> resultList = listCanonicalData(session, userCollection, new DataMap("username", new DataMap("$ne", null)));
		List<ChatServer.ChatUserInfo> list = new ArrayList<ChatServer.ChatUserInfo>();
		for(DataMap data : resultList) 
			list.add(toChatUserInfo(data));
		return list;
	}

	protected List<ChatConversation> listConversations(Session session) throws RedbackException {
		List<DataMap> resultList = listCanonicalData(session, conversationCollection, new DataMap("users", session.getUserProfile().getUsername()));
		List<ChatServer.ChatConversation> list = new ArrayList<ChatServer.ChatConversation>();
		for(DataMap data : resultList) 
			list.add(toChatConversation(data));
		return list;	
	}
	
	protected ChatConversation getConversation(Session session, String conversationId) throws RedbackException {
		List<DataMap> convoResultList = listCanonicalData(session, conversationCollection, new DataMap("_id", conversationId, "users", session.getUserProfile().getUsername()));
		if(convoResultList.size() > 0) {
			return toChatConversation(convoResultList.get(0));
		} else {
			return null;
		}
	}

	protected List<ChatMessage> listMessages(Session session, String conversationId) throws RedbackException {
		List<ChatMessage> list = new ArrayList<ChatMessage>();
		ChatConversation convo = getConversation(session, conversationId);
		if(convo != null) {
			List<DataMap> resultList = listCanonicalData(session, messageCollection, new DataMap("conversation", conversationId));
			for(DataMap data : resultList)
				list.add(toChatMessage(data));
		}
		return list;
	}
	
	protected List<ChatMessage> listAllUnreadMessages(Session session) throws RedbackException {
		List<ChatConversation> convos = listConversations(session);
		DataList convoIds = new DataList();
		for(ChatConversation convo: convos)
			convoIds.add(convo.id);
		List<ChatMessage> list = new ArrayList<ChatMessage>();
		DataMap filter = new DataMap("conversation", new DataMap("$in", convoIds), "readby", new DataMap("$ne", session.getUserProfile().getUsername()));
		List<DataMap> resultList = listCanonicalData(session, messageCollection, filter);
		for(DataMap data : resultList)
			list.add(toChatMessage(data));
		return list;
	}

	protected ChatConversation createConversation(Session session, String name) throws RedbackException {
		String uuid = UUID.randomUUID().toString();
		DataList users = new DataList();
		users.add(session.getUserProfile().getUsername());
		DataMap key = new DataMap("_id", uuid);
		DataMap data = new DataMap("domain", session.getUserProfile().getDefaultDomain(), "name", name, "owner", session.getUserProfile().getUsername(), "latest", new Date(), "users", users);
		dataClient.putData(conversationCollection.getName(), conversationCollection.convertObjectToSpecific(key), conversationCollection.convertObjectToSpecific(data));
		data.merge(key);
		ChatConversation convo = toChatConversation(data);
		publishConversation(convo);
		return convo;
	}

	protected ChatConversation addUserToConversation(Session session, String conversationId, String username) throws RedbackException {
		ChatConversation convo = getConversation(session, conversationId);
		if(convo != null ) {
			if(convo.users.indexOf(username) == -1) {
				convo.users.add(username);
				DataMap key = new DataMap("_id", convo.id);
				DataMap data = new DataMap("users", Convert.listToDataList(convo.users));
				dataClient.putData(conversationCollection.getName(), conversationCollection.convertObjectToSpecific(key), conversationCollection.convertObjectToSpecific(data));
			}
			publishConversation(convo);
			return convo;
		} else {
			throw new RedbackInvalidRequestException("Conversation does not exist");
		}
	}
	
	protected ChatConversation removeUserFromConversation(Session session, String conversationId, String username) throws RedbackException {
		ChatConversation convo = getConversation(session, conversationId);
		if(convo != null ) {
			convo.users.remove(username);
			DataMap key = new DataMap("_id", convo.id);
			DataMap data = new DataMap("users", Convert.listToDataList(convo.users));
			dataClient.putData(conversationCollection.getName(), conversationCollection.convertObjectToSpecific(key), conversationCollection.convertObjectToSpecific(data));
			publishConversation(convo);
			return convo;
		} else {
			throw new RedbackInvalidRequestException("Conversation does not exist");
		}	}


	protected ChatMessage sendMessage(Session session, String conversationId, String body) throws RedbackException {
		ChatConversation convo = getConversation(session, conversationId);
		if(convo != null) {
			String uuid = UUID.randomUUID().toString();
			String username = session.getUserProfile().getUsername();
			DataList readby = new DataList();
			readby.add(username);
			Date date = new Date();
			DataMap key = new DataMap("_id", uuid);
			DataMap data = new DataMap("domain", session.getUserProfile().getDefaultDomain(), "conversation", conversationId, "date", date, "from", username, "body", body, "readby", readby);
			dataClient.putData(messageCollection.getName(), messageCollection.convertObjectToSpecific(key), messageCollection.convertObjectToSpecific(data));
			DataMap convoKey = new DataMap("_id", convo.id);
			DataMap convoData = new DataMap("latest", date);
			dataClient.putData(conversationCollection.getName(), conversationCollection.convertObjectToSpecific(convoKey), conversationCollection.convertObjectToSpecific(convoData));
			data.merge(key);
			ChatMessage msg = toChatMessage(data);
			publishMessage(convo, msg);
			return msg;
		} else {
			throw new RedbackInvalidRequestException("Conversation does not exist");
		}
	}

	protected void markMessageRead(Session session, String msgId) throws RedbackException {
		DataMap key = new DataMap("_id", msgId);		
		String username = session.getUserProfile().getUsername();
		List<DataMap> resultList = listCanonicalData(session, messageCollection, key);
		if(resultList.size() > 0) {
			ChatMessage msg = toChatMessage(resultList.get(0));
			if(!msg.readby.contains(username))
				msg.readby.add(username);
			DataMap data = new DataMap("readby", Convert.listToDataList(msg.readby));
			dataClient.putData(messageCollection.getName(), messageCollection.convertObjectToSpecific(key), messageCollection.convertObjectToSpecific(data));
		}
	}
	
	
	protected void publishConversation(ChatConversation convo) throws RedbackException {
		if(clientPublishChannel != null) {
			DataMap out = new DataMap("type", "conversation", "id", convo.id, "name", convo.name, "owner", convo.owner, "latest", convo.latest, "users", Convert.listToDataList(convo.users));
			this.firebus.publish(clientPublishChannel, new Payload(out));
		}
	}

	protected void publishMessage(ChatConversation convo, ChatMessage msg) throws RedbackException {
		if(clientPublishChannel != null) {
			DataMap out = new DataMap("type", "message", "conversation", convo.id, "id", msg.id, "date", msg.date, "from", msg.from, "body", msg.body, "readby", Convert.listToDataList(msg.readby), "users", Convert.listToDataList(convo.users));
			this.firebus.publish(clientPublishChannel, new Payload(out));
		}	
	}


}
