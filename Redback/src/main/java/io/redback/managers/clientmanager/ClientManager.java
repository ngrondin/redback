package io.redback.managers.clientmanager;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import io.firebus.Firebus;
import io.firebus.Payload;
import io.firebus.data.DataList;
import io.firebus.data.DataMap;
import io.redback.client.DataClient;
import io.redback.client.FileClient;
import io.redback.exceptions.RedbackException;
import io.redback.security.Session;
import io.redback.utils.CollectionConfig;

public class ClientManager extends Thread {
	private Logger logger = Logger.getLogger("io.redback");
	protected String name;
	protected DataMap config;
	protected Firebus firebus;
	protected SubscriptionManager subsManager;
	protected FileClient fileClient;
	protected DataClient dataClient;
	protected CollectionConfig deviceCollection;
	protected CollectionConfig userCollection;
	protected List<ClientHandler> clientHandlers;
	protected boolean quit;

	
	public ClientManager(String n, DataMap c, Firebus f) {
		name = n;
		config = c;
		firebus = f;
		quit = false;
		subsManager = new SubscriptionManager();
		clientHandlers = new ArrayList<ClientHandler>();
		if(config.containsKey("fileservice")) fileClient = new FileClient(firebus, config.getString("fileservice"));
		if(config.containsKey("dataservice")) dataClient = new DataClient(firebus, config.getString("dataservice")); 
		deviceCollection = new CollectionConfig(config.getObject("devicecollection"), "rbcs_device");
		userCollection = new CollectionConfig(config.getObject("usercollection"), "rbcs_user");
		setName("rbClientHB");
		start();
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
	
	public ClientHandler acceptClientConnection(Session session, Payload payload) throws RedbackException {
		ClientHandler ch = new ClientHandler(this, session, payload);
		synchronized(clientHandlers) {
			clientHandlers.add(ch);
		}
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
		synchronized(clientHandlers) {
			clientHandlers.remove(clientHandler);
		}
		if(deviceCollection != null && dataClient != null && clientHandler.deviceId != null) {
			DataMap key = new DataMap("_id", clientHandler.deviceId);
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
	
	public void registerDevice(String deviceId, String deviceModel, String os, String appVersion, String locationPermissions, String username) throws RedbackException {
		if(deviceCollection != null && dataClient != null) {
			DataMap key = new DataMap("_id", deviceId);
			DataMap data = new DataMap();
			data.put("model", deviceModel);
			data.put("os", os);
			data.put("app", appVersion);
			data.put("locationpermissions", locationPermissions);
			data.put("username", username);
			data.put("lastlogin", new Date());
			dataClient.putData(deviceCollection.getName(), deviceCollection.convertObjectToSpecific(key), deviceCollection.convertObjectToSpecific(data));
		}
	}
	
	public DataMap getFlagsForDevice(String deviceId) throws RedbackException {
		DataMap key = new DataMap("_id", deviceId);
		DataMap resp = deviceCollection.convertObjectToCanonical(dataClient.getData(deviceCollection.getName(), deviceCollection.convertObjectToSpecific(key)));
		if(resp != null && resp.getList("result").size() > 0)
			return resp.getList("result").getObject(0).getObject("flags");
		else
			return null;
	}
	
	public void clearFlagsForDevice(String deviceId) throws RedbackException {
		DataMap key = new DataMap("_id", deviceId);
		DataMap data = new DataMap("flags", new DataMap());
		dataClient.putData(deviceCollection.getName(), deviceCollection.convertObjectToSpecific(key), deviceCollection.convertObjectToSpecific(data));
	}
	
	public void onObjectUpdate(DataMap data) throws RedbackException {
		List<ClientHandler> subscribers = subsManager.getSubscribersFor(data);
		for(ClientHandler ch : subscribers) {
			ch.receiveObjectData(data);
		}		
	}
	
	public void onNotification(DataMap data) throws RedbackException {
		Map<String, List<ClientHandler>> handlers = new HashMap<String, List<ClientHandler>>();
		synchronized(clientHandlers) {
			for(String username: data.keySet()) {
				List<ClientHandler> userHandlers = new ArrayList<ClientHandler>();
				for(ClientHandler ch: clientHandlers)
					if(ch.getSession().getUserProfile().getUsername().equals(username))
						userHandlers.add(ch);
				handlers.put(username, userHandlers);
			}
		}
		for(String username: data.keySet()) {
			List<ClientHandler> userHandlers = handlers.get(username);
			for(ClientHandler ch: userHandlers) 
				ch.receiveNotification(data.getObject(username));
		}	
	}
	
	public void onChatMessage(DataMap data) throws RedbackException {
		
	}
	
	public DataMap getStatus() {
		DataMap status = new DataMap();
		status.put("handlerCount", clientHandlers.size());
		DataList chList = new DataList();
		for(ClientHandler ch: clientHandlers)
			chList.add(ch.getStatus());
		status.put("handlers", chList);
		return status;
	}
	
	public void run() {
		while(!quit) {
			try {
				synchronized(clientHandlers) {
					for(ClientHandler ch: clientHandlers)
						ch.sendClientData(new DataMap("type", "serverkeepalive"));
				}
				Thread.sleep(10000);
			} catch(Exception e) {
				logger.severe("Error during client service heartbeat: " + e.getMessage());
			}
		}
	}
}
