package io.redback.managers.clientmanager;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.firebus.Payload;
import io.firebus.StreamEndpoint;
import io.firebus.data.DataException;
import io.firebus.data.DataList;
import io.firebus.data.DataMap;
import io.firebus.exceptions.FunctionErrorException;
import io.firebus.interfaces.ServiceRequestor;
import io.firebus.interfaces.StreamHandler;
import io.firebus.logging.Logger;
import io.redback.exceptions.RedbackException;
import io.redback.exceptions.RedbackInvalidRequestException;
import io.redback.managers.clientmanager.SubscriptionManager.FilterSubscription;
import io.redback.managers.clientmanager.SubscriptionManager.ObjectDomainPointer;
import io.redback.managers.clientmanager.SubscriptionManager.ObjectUIDPointer;
import io.redback.security.Session;
import io.redback.security.UserProfile;
import io.redback.services.ClientStreamHandler;

public class ClientHandler extends ClientStreamHandler {
	
	protected ClientManager clientManager;
	protected Map<String, StreamEndpoint> uploads;
	protected Map<String, StreamEndpoint> streams;
	protected String deviceId;
	protected String gatewayConnectionId;
	
	public ClientHandler(ClientManager cm, Session s, Payload payload, StreamEndpoint streamEndpoint) {
		super(s, streamEndpoint);
		clientManager = cm;
		session = s;
		uploads = new HashMap<String, StreamEndpoint>();
		streams = new HashMap<String, StreamEndpoint>();
		String gatewayNode = payload.metadata.get("streamgwnode");
		gatewayConnectionId = payload.metadata.get("streamgwid");
		Logger.info("rb.client.connect", new DataMap("firebusnode", clientManager.firebus.getNodeId(), "gatewaynode", gatewayNode, "gatewayconnid", gatewayConnectionId));
	}
	
	public synchronized void clientStreamClosed() throws RedbackException {
		try {
			for(String uid: streams.keySet()) 
				streams.get(uid).close();
			for(String uid: uploads.keySet()) 
				uploads.get(uid).close();
			clientManager.onClientLeave(this);
			Logger.info("rb.client.disconnect", new DataMap("gatewayconnid", gatewayConnectionId, "stats", getStats()));			
		} catch(Exception e) {
			Logger.severe("rb.client.disconnect", "Error closing client handler", e);
		}
	}
	
	public void registerDevice(String deviceId, String deviceModel, String os, String appVersion, String locationPermissions, boolean notifAuthorized, String fcmToken, boolean nfcAvailable, String screenSize) throws RedbackException {
		this.deviceId = deviceId;
		clientManager.registerDevice(deviceId, deviceModel, os, appVersion, locationPermissions, notifAuthorized, fcmToken, nfcAvailable, screenSize, session.getUserProfile().getUsername());
		DataMap flags = clientManager.getFlagsForDevice(deviceId);
		if(flags != null ) {
			if(flags.getBoolean("sendlog") == true) sendLogRequest();
			clientManager.clearFlagsForDevice(deviceId);
		}
	}
	
	public void subscribeFilter(String objectname, DataMap filter, String id) throws RedbackException {
		clientManager.subsManager.subscribe(this, objectname, filter, id);
	}

	public void subscribeObject(String objectname, String uid) throws RedbackException {
		clientManager.subsManager.subscribe(this, objectname, uid);
	}

	public void requestService(String reqUid, String serviceName, DataMap request, int timeout) throws RedbackException {
		try {
			String mappedServiceName = clientManager.getMappedServiceName(serviceName);
			Payload payload = new Payload();
			payload.metadata.put("token", session.getToken());
			if(session != null && session.getTimezone() != null)
				payload.metadata.put("timezone", session.getTimezone());
			payload.metadata.put("mime", "application/json");
			payload.setData(request);
			clientManager.firebus.requestService(mappedServiceName, payload, new ServiceRequestor() {
				public void response(Payload payload) {
					try {
						String mime = payload.metadata.get("mime");
						if(mime == null || (mime != null && mime.equals("application/json")))
							sendRequestResultData(reqUid, payload.getDataMap());
						else if(mime.startsWith("text/"))
							sendRequestResultText(reqUid, payload.getString());
					} catch(DataException e2) {
						Logger.severe("rb.client.request", "Client service request error while parsing response json", e2);
					}
				}

				public void error(FunctionErrorException e) { 
					sendRequestError(reqUid, e);
				}

				public void timeout() {
					sendRequestTimeout(reqUid);
				}	
			}, clientManager.name, timeout > -1 ? timeout : 10000);
		} catch(Exception e) {
			throw new RedbackException("Error requesting service for client", e);
		}
	}


	public void requestStream(String requid, String service, DataMap data, boolean autoNext) throws RedbackException {
		try {
			String mappedServiceName = clientManager.getMappedServiceName(service);
			Payload payload = new Payload();
			payload.metadata.put("token", session.getToken());
			if(session != null && session.getTimezone() != null)
				payload.metadata.put("timezone", session.getTimezone());
			payload.metadata.put("mime", "application/json");
			payload.setData(data);
			StreamEndpoint sep = clientManager.firebus.requestStream(mappedServiceName, payload, requid, 10000);
			registerStream(requid, sep);
			sep.setHandler(new StreamHandler() {
				public void receiveStreamData(Payload payload) {
					try {
						DataMap data = payload.getDataMap();
						sendStreamData(requid, data);
						if(autoNext)
							streamEndpoint.send(new Payload("next"));
					} catch(Exception e) {
						sep.close();
						sendStreamError(requid, new FunctionErrorException("Error receiving stream data", e));
					}
				}

				public void streamClosed() {
					deregisterStream(requid);
					sendStreamComplete(requid);
				}
				
				public void streamError(FunctionErrorException error) { 
					sendStreamError(requid, error);
				}
			});

		} catch(Exception e) {
			sendStreamError(requid, new FunctionErrorException("Error requesting stream", e));
			throw new RedbackException("Error requesting service for client", e);
		}
	}
	
	public void sendStreamNext(String requid) throws RedbackException {
		StreamEndpoint sep = streams.get(requid);
		if(sep != null) {
			sep.send(new Payload("next"));
		} else {
			throw new RedbackInvalidRequestException("Request UID not found");
		}
	}	

	public void startUpload(String uploaduid, String filename, int filesize, String mime, String object, String uid) throws RedbackException {
		try {
			StreamEndpoint sep = clientManager.getFileClient().putFileStream(session, filename, filesize, mime);
			registerUpload(uploaduid, sep);
			sep.setHandler(new StreamHandler() {
				public void receiveStreamData(Payload payload) {
					try {
						String ctl = payload.metadata.get("ctl");
						if(ctl.equals("next")) {
							sendUploadNext(uploaduid);
						} else if(ctl.equals("error")){
							sendUploadError(uploaduid, payload.metadata.get("error"));
							sep.close();
						} else if(ctl.equals("complete")) {
							DataMap result = payload.getDataMap();
							if(object != null && uid != null)
								clientManager.getFileClient().linkFileTo(session, result.getString("fileuid"), object, uid);
							sendUploadResult(uploaduid, result);
							sep.close();
						} else {
							throw new RedbackException("Error in client upload, unexpected response");
						}
					} catch(Exception e) {
						sep.close();
						sendUploadError(uploaduid, e.getMessage());
					} 
				}

				public void streamClosed() {
					deregisterUpload(uploaduid);
				}
				
				public void streamError(FunctionErrorException error) { 
					sendUploadError(uploaduid, error.getMessage());
				}
			});
			sendUploadNext(uploaduid);
		} catch(Exception e) {
			throw new RedbackException("Error starting client upload", e);
		}
	}

	public void uploadChunk(String uploaduid, int chunkSequence, byte[] bytes) throws RedbackException {
		StreamEndpoint sep = getUploadStreamEndpoint(uploaduid);
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
			throw new RedbackInvalidRequestException("Upload UID not found");
		}			
	}
	
	public void receiveObjectData(DataList list) throws RedbackException {
		DataMap wrapper = new DataMap();
		wrapper.put("type", "objectupdate");
		wrapper.put("objects", list);
		sendClientData(wrapper);
	}
	
	public void receiveNotification(DataMap data) throws RedbackException {
		DataMap wrapper = new DataMap();
		wrapper.put("type", "notification");
		wrapper.put("notification", data);
		sendClientData(wrapper);
	}
	
	public void receiveChatUpdate(DataMap data) throws RedbackException {
		sendChatUpdate(data);
	}

	public void updateToken(String newToken) throws RedbackException {
		if(newToken == null) return;
		UserProfile up = clientManager.getAccessManagementClient().validate(session, newToken);
		if(up != null) {		
			session.setToken(newToken);
			session.setUserProfile(up);
		}
	}
	
	protected synchronized void registerStream(String uid, StreamEndpoint ep) {
		streams.put(uid, ep);
	}

	protected synchronized void deregisterStream(String uid) {
		streams.remove(uid);
	}
	
	protected synchronized void registerUpload(String uid, StreamEndpoint ep) {
		uploads.put(uid, ep);
	}

	protected synchronized void deregisterUpload(String uid) {
		uploads.remove(uid);
	}
	
	public DataMap getStatus() {
		DataMap status = new DataMap();
		status.put("username", session.getUserProfile().getUsername());
		DataMap in = new DataMap();
		in.put("count", countIn);
		in.put("bytes", bytesIn);
		DataMap out = new DataMap();
		out.put("count", countOut);
		out.put("bytes", bytesOut);
		status.put("in", in);
		status.put("out", out);
		status.put("username", session.getUserProfile().getUsername());
		DataMap subs = new DataMap();
		List<ObjectUIDPointer> ouidplist = clientManager.subsManager.sessionObjectUIDPointers.get(this);
		subs.put("uniqueobjects", ouidplist != null ? ouidplist.size() : 0);
		int filterSubs = 0;
		int objectSubs = 0;
		List<ObjectDomainPointer> odplist = clientManager.subsManager.sessionObjectDomainPointers.get(this);
		if(odplist != null) {
			for(ObjectDomainPointer odp: odplist) {
				objectSubs++;
				List<FilterSubscription> fslist = clientManager.subsManager.objectFilterSubscriptions.get(odp.objectname).get(odp.domain);
				filterSubs += fslist.size();
			}
		}
		subs.put("objectfilters", filterSubs);
		subs.put("objects", objectSubs);
		status.put("subs", subs);
		return status;
	}




}
