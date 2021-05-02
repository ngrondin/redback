package io.redback.managers.clientmanager;

import java.util.ArrayList;
import java.util.List;

import io.firebus.Firebus;
import io.firebus.utils.DataList;
import io.firebus.utils.DataMap;
import io.redback.RedbackException;
import io.redback.client.FileClient;
import io.redback.security.Session;

public class ClientManager {

	protected String name;
	protected DataMap config;
	protected Firebus firebus;
	protected SubscriptionManager subsManager;
	protected List<ClientHandler> clientHandlers;
	protected FileClient fileClient;

	
	public ClientManager(String n, DataMap c, Firebus f) {
		name = n;
		config = c;
		firebus = f;
		subsManager = new SubscriptionManager();
		clientHandlers = new ArrayList<ClientHandler>();
		if(config.containsKey("fileservice")) fileClient = new FileClient(firebus, config.getString("fileservice")); 
		
	}

	public FileClient getFileClient() throws RedbackException {
		if(fileClient != null)
			return fileClient;
		else
			throw new RedbackException("File service has not been defined in client service");
	}
	
	public ClientHandler onClientConnect(Session session) throws RedbackException {
		ClientHandler ch = new ClientHandler(this, session);
		clientHandlers.add(ch);
		return ch;
	}
	
	protected void onClientLeave(ClientHandler clientHandler) throws RedbackException {
		subsManager.unsubscribe(clientHandler);
		clientHandlers.remove(clientHandler);
	}
	
	public void onObjectUpdate(DataMap data) throws RedbackException {
		List<ClientHandler> subscribers = subsManager.getSubscribersFor(data);
		for(ClientHandler ch : subscribers) {
			ch.receiveObjectData(data);
		}		
	}
	
	public void onNotification(DataMap data) throws RedbackException {
		DataList to = data.getList("to"); 
		for(ClientHandler ch: clientHandlers)
			for(int i = 0; i < to.size(); i++) 
				if(ch.session.getUserProfile().getUsername().equals(to.getString(i)))
					ch.receiveNotification(data);
	}
	
	public void onChatMessage(DataMap data) throws RedbackException {
		
	}
}
