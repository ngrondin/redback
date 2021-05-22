package io.redback.managers.clientmanager;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import io.firebus.Payload;
import io.firebus.StreamEndpoint;
import io.firebus.exceptions.FunctionErrorException;
import io.firebus.interfaces.ServiceRequestor;
import io.firebus.interfaces.StreamHandler;
import io.firebus.utils.DataMap;
import io.redback.RedbackException;
import io.redback.security.Session;
import io.redback.services.ClientStreamHandler;
import io.redback.utils.StringUtils;

public class ClientHandler extends ClientStreamHandler {
	private Logger logger = Logger.getLogger("io.redback");
	
	protected ClientManager clientManager;
	protected Map<String, StreamEndpoint> uploads;
	protected String deviceId;
	
	public ClientHandler(ClientManager cm, Session s) {
		super(s);
		clientManager = cm;
		session = s;
		uploads = new HashMap<String, StreamEndpoint>();
		System.out.println("Client connected for " + session.getUserProfile().getUsername());
	}
	
	public void clientStreamClosed() throws RedbackException {
		try {
			clientManager.onClientLeave(this);
			System.out.println("Client disconnected for " + session.getUserProfile().getUsername());
		} catch(Exception e) {
			logger.severe("Error closing stream : " + e.getMessage());
		}
	}
	
	public void registerDevice(String deviceId, String deviceModel, String os, String appVersion, String locationPermissions) throws RedbackException {
		this.deviceId = deviceId;
		clientManager.registerDevice(deviceId, deviceModel, os, appVersion, locationPermissions, session.getUserProfile().getUsername());
	}
	
	public void subscribeFilter(String objectname, DataMap filter, String id) throws RedbackException {
		clientManager.subsManager.subscribe(this, objectname, filter, id);
	}

	public void subscribeObject(String objectname, String uid) throws RedbackException {
		clientManager.subsManager.subscribe(this, objectname, uid);
	}

	public void requestService(String reqUid, String serviceName, DataMap request) throws RedbackException {
		try {
			Payload payload = new Payload();
			payload.metadata.put("token", session.getToken());
			payload.metadata.put("timezone", session.getTimezone());
			payload.metadata.put("mime", "application/json");
			payload.setData(request.toString());
			clientManager.firebus.requestService(serviceName, payload, new ServiceRequestor() {
				public void response(Payload payload) {
					try {
						DataMap resp = new DataMap(payload.getString());
						DataMap respWrapper = new DataMap();
						respWrapper.put("type", "serviceresponse");
						respWrapper.put("requid", reqUid);
						respWrapper.put("response", resp);
						sendClientData(respWrapper);
					} catch(Exception e2) {
					}
				}

				public void error(FunctionErrorException e) { 
					DataMap respWrapper = new DataMap();
					respWrapper.put("type", "serviceerror");
					respWrapper.put("requid", reqUid);
					respWrapper.put("error", StringUtils.rollUpExceptions(e));
					sendClientData(respWrapper);					
				}

				public void timeout() {
					DataMap respWrapper = new DataMap();
					respWrapper.put("type", "serviceerror");
					respWrapper.put("requid", reqUid);
					respWrapper.put("error", "service request timed out");
					sendClientData(respWrapper);						
				}	
			}, 10000);
		} catch(Exception e) {
			throw new RedbackException("Error requesting service for client", e);
		}
	}



	public void startUpload(String uploaduid, String filename, int filesize, String mime, String object, String uid) throws RedbackException {
		try {
			StreamEndpoint sep = clientManager.getFileClient().putFileStream(session, filename, filesize, mime);
			uploads.put(uploaduid, sep);
			sep.setHandler(new StreamHandler() {
				public void receiveStreamData(Payload payload, StreamEndpoint streamEndpoint) {
					try {
						String ctl = payload.metadata.get("ctl");
						if(ctl.equals("next")) {
							sendUploadNext(uploaduid);
						} else if(ctl.equals("error")){
							sendUploadError(uploaduid, payload.metadata.get("error"));
						} else {
							DataMap result = new DataMap(payload.getString());
							if(object != null && uid != null)
								clientManager.getFileClient().linkFileTo(session, result.getString("fileuid"), object, uid);
							sendUploadResult(uploaduid, result);
						}
					} catch(Exception e) {
						sendUploadError(uploaduid, e.getMessage());
					}
				}

				public void streamClosed(StreamEndpoint streamEndpoint) {
					uploads.remove(uploaduid);
				}
			});
		} catch(Exception e) {
			throw new RedbackException("Error starting client upload", e);
		}
	}

	public void uploadChunk(String uploaduid, int chunkSequence, byte[] bytes) throws RedbackException {
		StreamEndpoint sep = uploads.get(uploaduid);
		Payload payload = new Payload(bytes);
		payload.metadata.put("ctl", "chunk");
		payload.metadata.put("seq", String.valueOf(chunkSequence));
		sep.send(payload);
	}

	public void finishUpload(String uploaduid) throws RedbackException {
		StreamEndpoint sep = getUploadStreamEndpoint(uploaduid);
		Payload payload = new Payload();
		payload.metadata.put("ctl", "complete");
		sep.send(payload);		
	}
	
	protected StreamEndpoint getUploadStreamEndpoint(String uid) throws RedbackException {
		StreamEndpoint sep = uploads.get(uid);
		if(sep != null) {
			return sep;
		} else {
			throw new RedbackException("Upload UID not found");
		}			
	}


	
	public void receiveObjectData(DataMap data) throws RedbackException {
		DataMap wrapper = new DataMap();
		wrapper.put("type", "objectupdate");
		wrapper.put("object", data);
		sendClientData(wrapper);
	}
	
	public void receiveNotification(DataMap data) throws RedbackException {
		DataMap wrapper = new DataMap();
		wrapper.put("type", "notification");
		wrapper.put("notification", data);
		sendClientData(wrapper);
	}
	
	public void receiveChat(DataMap data) throws RedbackException {
		
	}


	

}
