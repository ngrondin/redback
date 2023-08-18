package io.redback.services;

import java.util.Date;
import java.util.List;

import io.firebus.Firebus;
import io.firebus.Payload;
import io.firebus.data.DataException;
import io.firebus.data.DataList;
import io.firebus.data.DataMap;
import io.firebus.information.ServiceInformation;
import io.redback.exceptions.RedbackException;
import io.redback.security.Session;
import io.redback.services.common.AuthenticatedServiceProvider;
import io.redback.utils.Convert;

public abstract class ChatServer extends AuthenticatedServiceProvider {
	
	public class ChatUserInfo {
		public String id;
		public String username;
		public String fullname;
		public boolean available;
	}
	
	public class ChatConversation {
		public String id;
		public String name;
		public String owner;
		public Date latest;
		public List<String> users;
	}
	
	public class ChatMessage {
		public String id;
		public String conversationId;
		public Date date;
		public String from;
		public String body;
		public List<String> readby;

	}

	public ChatServer(String n, DataMap c, Firebus f) {
		super(n, c, f);
	}

	
	public ServiceInformation getServiceInformation() {
		return null;
	}
	
	public Payload redbackUnauthenticatedService(Session session, Payload payload) throws RedbackException {
		throw new RedbackException("All requests need to be authenticated");
	}
	
	public Payload redbackAuthenticatedService(Session session, Payload payload) throws RedbackException {
		Payload response = new Payload();
		try {
			DataMap request = payload.getDataMap();
			String action = request.getString("action");
			DataMap responseData = null;
			if(action != null)
			{
				if(action.equals("listusers"))
				{
					List<ChatUserInfo> list = listUsers(session);
					DataList responseList = new DataList();
					for(ChatUserInfo userInfo: list)
						responseList.add(new DataMap("id", userInfo.id, "username", userInfo.username, "fullname", userInfo.fullname, "available", userInfo.available));
					responseData = new DataMap("list", responseList);
				}
				else if(action.equals("listconversations"))
				{
					List<ChatConversation> list = listConversations(session);
					DataList responseList = new DataList();
					for(ChatConversation convo: list)
						responseList.add(new DataMap("id", convo.id, "name", convo.name, "owner", convo.owner, "latest", convo.latest, "users", Convert.listToDataList(convo.users)));
					responseData = new DataMap("list", responseList);
				}	
				else if(action.equals("listmessages"))
				{
					List<ChatMessage> list = listMessages(session, request.getString("conversation"));
					DataList responseList = new DataList();
					for(ChatMessage msg: list)
						responseList.add(new DataMap("id", msg.id, "date", msg.date, "from", msg.from, "body", msg.body, "readby", Convert.listToDataList(msg.readby)));
					responseData = new DataMap("list", responseList);
				}
				else if(action.equals("listunreadmessages"))
				{
					List<ChatMessage> list = listAllUnreadMessages(session);
					DataList responseList = new DataList();
					for(ChatMessage msg: list)
						responseList.add(new DataMap("id", msg.id, "conversation", msg.conversationId, "date", msg.date, "from", msg.from, "body", msg.body, "readby", Convert.listToDataList(msg.readby)));
					responseData = new DataMap("list", responseList);
				}				
				else if(action.equals("createconversation"))
				{
					String name = request.getString("name");
					ChatConversation convo = createConversation(session, name);
					responseData = new DataMap("id", convo.id, "name", convo.name, "owner", convo.owner, "latest", convo.latest, "users", Convert.listToDataList(convo.users));
				}	
				else if(action.equals("adduser"))
				{
					String conversationId = request.getString("conversation");
					String username = request.getString("username");
					ChatConversation convo = addUserToConversation(session, conversationId, username);
					responseData = new DataMap("id", convo.id, "users", Convert.listToDataList(convo.users));
				}	
				else if(action.equals("removeuser"))
				{
					String conversationId = request.getString("conversation");
					String username = request.getString("username");
					ChatConversation convo = removeUserFromConversation(session, conversationId, username);
					responseData = new DataMap("id", convo.id, "users", Convert.listToDataList(convo.users));
				}					
				else if(action.equals("sendmessage"))
				{
					String conversationId = request.getString("conversation");
					String body = request.getString("body");
					ChatMessage msg = sendMessage(session, conversationId, body);
					responseData = new DataMap("id", msg.id, "date", msg.date, "from", msg.from, "body", msg.body, "readby", Convert.listToDataList(msg.readby));
				}		
				else if(action.equals("markread"))
				{
					String msgId = request.getString("message");
					markMessageRead(session, msgId);
					responseData = new DataMap("result", "ok");
				}				
				else
				{
					throw new RedbackException("Valid actions are 'listusers', 'listconversations', 'listmessages' and 'sendmessage'");
				}
			}
			response.setData(responseData);
			response.metadata.put("mime", "application/json");
			return response;	
		} catch(DataException e) {
			throw new RedbackException("Error in object server", e);
		}
	}

	
	protected abstract List<ChatUserInfo> listUsers(Session session) throws RedbackException;
	
	protected abstract List<ChatConversation> listConversations(Session session) throws RedbackException;

	protected abstract List<ChatMessage> listMessages(Session session, String conversationId) throws RedbackException;

	protected abstract List<ChatMessage> listAllUnreadMessages(Session session) throws RedbackException;

	protected abstract ChatConversation createConversation(Session session, String name) throws RedbackException;
	
	protected abstract ChatConversation addUserToConversation(Session session, String conversationId, String username) throws RedbackException;
	
	protected abstract ChatConversation removeUserFromConversation(Session session, String conversationId, String username) throws RedbackException;

	protected abstract ChatMessage sendMessage(Session session, String conversationId, String body) throws RedbackException;
	
	protected abstract void markMessageRead(Session session, String msgId) throws RedbackException;

}
