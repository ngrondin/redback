package io.redback.managers.clientmanager;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.firebus.Payload;
import io.firebus.StreamEndpoint;
import io.firebus.data.DataException;
import io.firebus.data.DataMap;
import io.firebus.exceptions.FunctionErrorException;
import io.firebus.interfaces.ServiceRequestor;
import io.firebus.interfaces.StreamHandler;
import io.firebus.logging.Logger;
import io.redback.exceptions.RedbackException;
import io.redback.managers.clientmanager.SubscriptionManager.FilterSubscription;
import io.redback.managers.clientmanager.SubscriptionManager.ObjectDomainPointer;
import io.redback.managers.clientmanager.SubscriptionManager.ObjectUIDPointer;
import io.redback.security.Session;
import io.redback.services.ClientStreamHandler;

public class ClientHandler extends ClientStreamHandler {
	
	protected ClientManager clientManager;
	protected Map<String, StreamEndpoint> uploads;
	protected String deviceId;
	protected String gatewayConnectionId;
	
	public ClientHandler(ClientManager cm, Session s, Payload payload) {
		super(s);
		clientManager = cm;
		session = s;
		uploads = new HashMap<String, StreamEndpoint>();
		String gatewayNode = payload.metadata.get("streamgwnode");
		gatewayConnectionId = payload.metadata.get("streamgwid");
		Logger.info("rb.client.connect", new DataMap("firebusnode", clientManager.firebus.getNodeId(), "gatewaynode", gatewayNode, "gatewayconnid", gatewayConnectionId));
	}
	
	public void clientStreamClosed() throws RedbackException {
		try {
			clientManager.onClientLeave(this);
			Logger.info("rb.client.disconnect", new DataMap("gatewayconnid", gatewayConnectionId, "stats", getStatString()));			
		} catch(Exception e) {
			Logger.severe("rb.client.disconnect", "Error closing client handler", e);
			//logger.severe("Error closing client handler : " + e.getMessage());
		}
	}
	
	public void registerDevice(String deviceId, String deviceModel, String os, String appVersion, String locationPermissions, boolean notifAuthorized, boolean nfcAvailable, String screenSize) throws RedbackException {
		this.deviceId = deviceId;
		clientManager.registerDevice(deviceId, deviceModel, os, appVersion, locationPermissions, notifAuthorized, nfcAvailable, screenSize, session.getUserProfile().getUsername());
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
			Payload payload = new Payload();
			payload.metadata.put("token", session.getToken());
			if(session != null && session.getTimezone() != null)
				payload.metadata.put("timezone", session.getTimezone());
			payload.metadata.put("mime", "application/json");
			payload.setData(request);
			clientManager.firebus.requestService(serviceName, payload, new ServiceRequestor() {
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
						} else if(ctl.equals("complete")) {
							// Don't close yet as we're waiting for a response with the file meta
						} else if(ctl.equals("chunk")) { //This is the meta response after upload
							DataMap result = payload.getDataMap();
							if(object != null && uid != null)
								clientManager.getFileClient().linkFileTo(session, result.getString("fileuid"), object, uid);
							sendUploadResult(uploaduid, result);
							sep.close();
							uploads.remove(uploaduid);
						} else {
							throw new RedbackException("Error in client upload, unexpected response");
						}
					} catch(Exception e) {
						sendUploadError(uploaduid, e.getMessage());
					}
				}

				public void streamClosed(StreamEndpoint streamEndpoint) {
					uploads.remove(uploaduid);
				}
			});
			sendUploadNext(uploaduid);
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
