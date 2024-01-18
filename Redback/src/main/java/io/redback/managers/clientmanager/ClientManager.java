package io.redback.managers.clientmanager;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.firebus.Firebus;
import io.firebus.Payload;
import io.firebus.StreamEndpoint;
import io.firebus.data.DataList;
import io.firebus.data.DataMap;
import io.firebus.logging.Logger;
import io.redback.client.DataClient;
import io.redback.client.FileClient;
import io.redback.exceptions.RedbackException;
import io.redback.security.Session;
import io.redback.utils.CollectionConfig;

public class ClientManager extends Thread {
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
	
	public Payload acceptClientConnection(Session session, Payload payload, StreamEndpoint streamEndpoint) throws RedbackException {
		ClientHandler ch = new ClientHandler(this, session, payload, streamEndpoint);
		synchronized(clientHandlers) {
			clientHandlers.add(ch);
		}
		if(userCollection != null && dataClient != null) {
			DataMap key = new DataMap("_id", session.getUserProfile().getUsername());
			DataMap data = new DataMap();
			data.put("lastlogin", new Date());
			dataClient.putData(userCollection.getName(), userCollection.convertObjectToSpecific(key), userCollection.convertObjectToSpecific(data));
		}
		return new Payload();
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

	
	public void registerDevice(String deviceId, String deviceModel, String os, String appVersion, String locationPermissions, String fcmToken, boolean nfcAvailable, String screenSize, String username) throws RedbackException {
		if(deviceCollection != null && dataClient != null) {
			DataMap key = new DataMap("_id", deviceId);
			DataMap resp = dataClient.getData(deviceCollection.getName(), deviceCollection.convertObjectToSpecific(key));
			DataMap existingData = resp.containsKey("result") && resp.getList("result").size() > 0 ? resp.getList("result").getObject(0) : null;
			DataList newHistory = new DataList();
			DataMap newData = new DataMap();
			registerDeviceUpdateData("model", existingData, deviceModel, newData, newHistory);
			registerDeviceUpdateData("os", existingData, os, newData, newHistory);
			registerDeviceUpdateData("app", existingData, appVersion, newData, newHistory);
			registerDeviceUpdateData("locationpermissions", existingData, locationPermissions, newData, newHistory);
			registerDeviceUpdateData("fcmtoken", existingData, fcmToken, newData, newHistory);
			registerDeviceUpdateData("nfcavailable", existingData, nfcAvailable, newData, newHistory);
			registerDeviceUpdateData("screen", existingData, screenSize, newData, newHistory);
			registerDeviceUpdateData("username", existingData, username, newData, newHistory);
			newData.put("lastlogin", new Date());
			if(newHistory.size() > 0) {
				DataList history = existingData != null && existingData.containsKey("history") ? existingData.getList("history") : new DataList();
				for(int i = 0; i < newHistory.size(); i++)
					history.add(newHistory.getObject(i));
				newData.put("history", history);
			}
			dataClient.putData(deviceCollection.getName(), deviceCollection.convertObjectToSpecific(key), deviceCollection.convertObjectToSpecific(newData));
		}
	}
	
	private void registerDeviceUpdateData(String key, DataMap existingData, Object newVal, DataMap newData, DataList history) {
		if(newVal != null && (existingData == null || (existingData != null && !newVal.equals(existingData.get(key))))) {
			newData.put(key, newVal);
			history.add(new DataMap("date", new Date(), "key", key, "value", newVal));
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
		Map<ClientHandler, DataList> map = new HashMap<ClientHandler, DataList>();
		DataList list = data.getList("list");
		if(list != null) {
			for(int i = 0; i < list.size(); i++) {
				DataMap object = list.getObject(i);
				List<ClientHandler> subscribers = subsManager.getSubscribersFor(object);
				for(ClientHandler ch: subscribers) {
					if(!map.containsKey(ch))
						map.put(ch, new DataList());
					map.get(ch).add(object);
				}
			}
			for(ClientHandler ch : map.keySet())
				ch.receiveObjectData(map.get(ch));					
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
	
	public void onChatUpdate(DataMap data) throws RedbackException {
		DataList usernames = data.getList("users");
		List<ClientHandler> handlers = new ArrayList<ClientHandler>();
		synchronized(clientHandlers) {
			for(int i = 0; i < usernames.size(); i++) {
				String username = usernames.getString(i);
				for(ClientHandler ch: clientHandlers)
					if(ch.getSession().getUserProfile().getUsername().equals(username))
						handlers.add(ch);
			}
		}
		for(ClientHandler ch : handlers) 
			ch.receiveChatUpdate(data);
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
				Thread.sleep(10000);
				synchronized(clientHandlers) {
					for(ClientHandler ch: clientHandlers)
						ch.sendClientData(new DataMap("type", "serverkeepalive"));
				}
			} catch(Exception e) {
				Logger.severe("rb.client.run", "Error during client service heartbeat", e);
			}
		}
	}
}
