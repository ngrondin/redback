package io.redback.managers.clientmanager;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import io.firebus.Firebus;
import io.firebus.utils.DataList;
import io.firebus.utils.DataMap;
import io.redback.RedbackException;
import io.redback.client.DataClient;
import io.redback.client.FileClient;
import io.redback.security.Session;
import io.redback.utils.CollectionConfig;

public class ClientManager {

	protected String name;
	protected DataMap config;
	protected Firebus firebus;
	protected SubscriptionManager subsManager;
	protected List<ClientHandler> clientHandlers;
	protected FileClient fileClient;
	protected DataClient dataClient;
	protected CollectionConfig deviceCollection;
	protected CollectionConfig userCollection;

	
	public ClientManager(String n, DataMap c, Firebus f) {
		name = n;
		config = c;
		firebus = f;
		subsManager = new SubscriptionManager();
		clientHandlers = new ArrayList<ClientHandler>();
		if(config.containsKey("fileservice")) fileClient = new FileClient(firebus, config.getString("fileservice"));
		if(config.containsKey("dataservice")) dataClient = new DataClient(firebus, config.getString("dataservice")); 
		deviceCollection = new CollectionConfig(config.getObject("devicecollection"), "rbcs_device");
		userCollection = new CollectionConfig(config.getObject("usercollection"), "rbcs_user");
	}

	public FileClient getFileClient() throws RedbackException {
		if(fileClient != null)
			return fileClient;
		else
			throw new RedbackException("File service has not been defined in client service");
	}
	
	public DataClient getDataClient() throws RedbackException {
		if(dataClient != null)
			return dataClient;
		else
			throw new RedbackException("Data service has not been defined in client service");
	}
	
	public CollectionConfig getDeviceCollectionConfig() {
		return deviceCollection;
	}
	
	public CollectionConfig getUserCollectionConfig() {
		return userCollection;
	}
	
	public ClientHandler onClientConnect(Session session) throws RedbackException {
		ClientHandler ch = new ClientHandler(this, session);
		clientHandlers.add(ch);
		if(userCollection != null && dataClient != null) {
			DataMap key = new DataMap("_id", session.getUserProfile().getUsername());
			DataMap data = new DataMap();
			data.put("lastlogin", new Date());
			dataClient.putData(userCollection.getName(), userCollection.convertObjectToSpecific(key), userCollection.convertObjectToSpecific(data));
		}		
		return ch;
	}
	
	protected void onClientLeave(ClientHandler clientHandler) throws RedbackException {
		subsManager.unsubscribe(clientHandler);
		clientHandlers.remove(clientHandler);
		if(deviceCollection != null && dataClient != null && clientHandler.deviceId != null) {
			DataMap key = new DataMap("_id", clientHandler.deviceId != null);
			DataMap data = new DataMap();
			data.put("lastlogout", new Date());
			dataClient.putData(deviceCollection.getName(), deviceCollection.convertObjectToSpecific(key), deviceCollection.convertObjectToSpecific(data));
		}	
		if(userCollection != null && dataClient != null) {
			DataMap key = new DataMap("_id", clientHandler.getSession().getUserProfile().getUsername());
			DataMap data = new DataMap();
			data.put("lastlogout", new Date());
			dataClient.putData(userCollection.getName(), userCollection.convertObjectToSpecific(key), userCollection.convertObjectToSpecific(data));
		}			
	}
	
	public void registerDevice(String deviceId, String deviceName, String deviceModel, String deviceVersion, String appVersion, String username) throws RedbackException {
		if(deviceCollection != null && dataClient != null) {
			DataMap key = new DataMap("_id", deviceId);
			DataMap data = new DataMap();
			data.put("name", deviceName);
			data.put("model", deviceModel);
			data.put("version", deviceVersion);
			data.put("app", appVersion);
			data.put("username", username);
			data.put("lastlogin", new Date());
			dataClient.putData(deviceCollection.getName(), deviceCollection.convertObjectToSpecific(key), deviceCollection.convertObjectToSpecific(data));
		}
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
				if(ch.getSession().getUserProfile().getUsername().equals(to.getString(i)))
					ch.receiveNotification(data);
	}
	
	public void onChatMessage(DataMap data) throws RedbackException {
		
	}
}
