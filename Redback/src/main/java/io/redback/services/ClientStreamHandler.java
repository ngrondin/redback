package io.redback.services;

import java.util.Base64;

import io.firebus.Payload;
import io.firebus.StreamEndpoint;
import io.firebus.exceptions.FunctionErrorException;
import io.firebus.data.DataList;
import io.firebus.data.DataMap;
import io.redback.exceptions.RedbackException;
import io.redback.security.Session;
import io.redback.services.common.StreamHandler;
import io.redback.utils.StringUtils;

public abstract class ClientStreamHandler extends StreamHandler {
	protected long start = 0;
	protected int heartbeatCount = 0;
	protected long lastHeartbeat = 0;
	protected long lastIn = 0;
	protected long lastOut = 0;
	protected long bytesIn = 0;
	protected long bytesOut = 0;
	protected long countIn = 0;
	protected long countOut = 0;
	
	public ClientStreamHandler(Session s, StreamEndpoint se) {
		super(s, se);
		start = System.currentTimeMillis();
	}

	public void receiveData(Payload payload) throws RedbackException {
		try {
			String payloadStr = payload.getString();
			lastIn = System.currentTimeMillis();
			bytesIn += payloadStr.length();
			countIn++;
			DataMap data = new DataMap(payloadStr);
			String type = data.getString("type");
			if(type != null) {
				if(type.equals("clientinfo")) {
					DataMap clientData = data.getObject("data");
					registerDevice(clientData.getString("deviceid"), clientData.getString("devicemodel"), clientData.getString("os"), clientData.getString("appversion"), clientData.getString("locationpermission"), clientData.getBoolean("notificationauthorized"), clientData.getBoolean("nfcavailable"), clientData.getString("screensize"));
				} else if(type.equals("subscribe")) {
					if(data.containsKey("uid")) {
						this.subscribeObject(data.getString("objectname"), data.getString("uid"));
					} else if(data.containsKey("filter")) {
						this.subscribeFilter(data.getString("objectname"), data.getObject("filter"), data.getString("id"));
					} else if(data.containsKey("list")) {
						DataList list = data.getList("list");
						for(int i = 0; i < list.size(); i++) {
							DataMap item = list.getObject(i);
							if(item.containsKey("uid")) {
								this.subscribeObject(item.getString("objectname"), item.getString("uid"));
							} else if(item.containsKey("filter")) {
								this.subscribeFilter(item.getString("objectname"), item.getObject("filter"), item.getString("id"));
							}	
						}
					}
				} else if(type.equals("servicerequest")) {
					String reqUid = data.getString("requid");
					String serviceName = data.getString("servicename");
					DataMap request = data.getObject("request");
					int timeout = data.containsKey("timeout") ? data.getNumber("timeout").intValue() : -1;
					requestService(reqUid, serviceName, request, timeout);
				} else if(type.equals("streamrequest")) {
					String reqUid = data.getString("requid");
					String serviceName = data.getString("servicename");
					DataMap request = data.getObject("request");
					boolean autoNext = data.getBoolean("autonext");
					requestStream(reqUid, serviceName, request, autoNext);	
				} else if(type.equals("streamnext")) {
					String reqUid = data.getString("requid");
					sendStreamNext(reqUid);						
				} else if(type.equals("upload")) {
					String uploadUid = data.getString("uploaduid");
					try {
						if(data.containsKey("request")) {
							DataMap ulReq = data.getObject("request");
							String filename = ulReq.getString("filename");
							int filesize = ulReq.getNumber("filesize").intValue();
							String mime = ulReq.getString("mime");
							String object = ulReq.getString("object");
							String uid = ulReq.getString("uid");
							startUpload(uploadUid, filename, filesize, mime, object, uid);
						} else if(data.containsKey("data")) {
							int seq = data.getNumber("sequence").intValue();
							String base64data = data.getString("data");
							if(base64data.indexOf(",") > -1) base64data = base64data.split(",")[1];
							byte[] bytes = null;
							if(base64data.contains("-") || base64data.contains("_")) {
								bytes = Base64.getUrlDecoder().decode(base64data);
							} else {
								bytes = Base64.getDecoder().decode(base64data);
							}
							uploadChunk(uploadUid, seq, bytes);
						} else if(data.containsKey("complete")) {
							finishUpload(uploadUid);
						}
					} catch(Exception e) {
						sendUploadError(uploadUid, StringUtils.rollUpExceptions(e));
						throw new RedbackException("Error handling upload", e);
					}
				} else if(type.equals("heartbeat")) {
					lastHeartbeat = System.currentTimeMillis();
					heartbeatCount++;
					sendClientData(new DataMap("type", "heartbeat"));
				}
			}
		} catch(Exception e) {
			throw new RedbackException("error receiving client data", e);
		}
	}
	
	public void closed() throws RedbackException {
		try {
			clientStreamClosed();
		} catch(Exception e) {
			throw new RedbackException("error closing client stream", e);
		}
	}
	
	public void sendClientData(DataMap data) {
		String payloadStr = data.toString(true);
		sendStreamData(new Payload(payloadStr));
		lastOut = System.currentTimeMillis();
		bytesOut += payloadStr.length();
		countOut++;
	}
	
	public void sendRequestResultData(String reqUid, DataMap data) {
		DataMap respWrapper = new DataMap();
		respWrapper.put("type", "serviceresponse");
		respWrapper.put("requid", reqUid);
		respWrapper.put("response", data);
		sendClientData(respWrapper);
	}
	
	public void sendRequestResultText(String reqUid, String text) {
		DataMap respWrapper = new DataMap();
		respWrapper.put("type", "serviceresponse");
		respWrapper.put("requid", reqUid);
		respWrapper.put("response", text);
		sendClientData(respWrapper);
	}
	
	public void sendRequestError(String reqUid, FunctionErrorException e) { 
		DataMap respWrapper = new DataMap();
		respWrapper.put("type", "serviceerror");
		respWrapper.put("requid", reqUid);
		respWrapper.put("error", StringUtils.rollUpExceptions(e));
		respWrapper.put("code", String.valueOf(e.getErrorCode())); //TODO: Change back to int when the mobile bug is fixed
		sendClientData(respWrapper);					
	}

	public void sendRequestTimeout(String reqUid) { 
		DataMap respWrapper = new DataMap();
		respWrapper.put("type", "serviceerror");
		respWrapper.put("requid", reqUid);
		respWrapper.put("error", "service request timed out");
		sendClientData(respWrapper);						
	}	
	
	public void sendStreamData(String reqUid, DataMap data) {
		DataMap respWrapper = new DataMap();
		respWrapper.put("type", "streamdata");
		respWrapper.put("requid", reqUid);
		respWrapper.put("data", data);
		sendClientData(respWrapper);
	}
	
	public void sendStreamError(String reqUid, FunctionErrorException e) { 
		DataMap respWrapper = new DataMap();
		respWrapper.put("type", "streamerror");
		respWrapper.put("requid", reqUid);
		respWrapper.put("error", StringUtils.rollUpExceptions(e));
		respWrapper.put("code", String.valueOf(e.getErrorCode())); //TODO: Change back to int when the mobile bug is fixed
		sendClientData(respWrapper);					
	}

	public void sendStreamTimeout(String reqUid) { 
		DataMap respWrapper = new DataMap();
		respWrapper.put("type", "streamerror");
		respWrapper.put("requid", reqUid);
		respWrapper.put("error", "service request timed out");
		sendClientData(respWrapper);						
	}	

	public void sendStreamComplete(String reqUid) { 
		DataMap respWrapper = new DataMap();
		respWrapper.put("type", "streamcomplete");
		respWrapper.put("requid", reqUid);
		sendClientData(respWrapper);						
	}	
	
	public void sendUploadNext(String uploaduid) {
		DataMap msg = new DataMap();
		msg.put("type", "uploadctl");
		msg.put("uploaduid", uploaduid);
		msg.put("ctl", "next");
		sendClientData(msg);
	}
	
	public void sendUploadError(String uploaduid, String error) {
		DataMap msg = new DataMap();
		msg.put("type", "uploadctl");
		msg.put("uploaduid", uploaduid);
		msg.put("ctl", "error");
		msg.put("error", error);
		sendClientData(msg);
	}
	
	public void sendUploadResult(String uploaduid, DataMap resp) {
		DataMap msg = new DataMap();
		msg.put("type", "uploadctl");
		msg.put("uploaduid", uploaduid);
		msg.put("ctl", "result");
		msg.put("result", resp);
		sendClientData(msg);		
	}
	
	public void sendLogRequest() {
		DataMap msg = new DataMap();
		msg.put("type", "log");
		msg.put("action", "send");
		msg.put("filename", "log_" + session.getUserProfile().getUsername() + ".json");
		sendClientData(msg);		
	}
	
	public void sendChatUpdate(DataMap data) {
		DataMap msg = new DataMap();
		msg.put("type", "chatupdate");
		msg.put("data", data);
		sendClientData(msg);		
	}
	
	public abstract void registerDevice(String deviceId, String deviceModel, String os, String appVersion, String locationPermissions, boolean notifAuthorized, boolean nfcAvailable, String screenSize) throws RedbackException;
	
	public abstract void subscribeObject(String objectname, String uid) throws RedbackException;
	
	public abstract void subscribeFilter(String objectname, DataMap filter, String id) throws RedbackException;
	
	public abstract void requestService(String requid, String service, DataMap data, int timeout) throws RedbackException;

	public abstract void requestStream(String requid, String service, DataMap data, boolean autoNext) throws RedbackException;

	public abstract void sendStreamNext(String requid) throws RedbackException;

	public abstract void startUpload(String uploaduid, String filename, int filesize, String mime, String object, String uid) throws RedbackException;

	public abstract void uploadChunk(String uploaduid, int chunkSequence, byte[] bytes) throws RedbackException;
	
	public abstract void finishUpload(String uploaduid) throws RedbackException;

	public abstract void receiveObjectData(DataMap data) throws RedbackException;
	
	public abstract void receiveNotification(DataMap data) throws RedbackException;
	
	public abstract void receiveChatUpdate(DataMap data) throws RedbackException;
	
	public abstract void clientStreamClosed() throws RedbackException;
	
	protected DataMap getStats() {
		long now = System.currentTimeMillis();
		DataMap stats = new DataMap();
		stats.put("user", session.getUserProfile().getUsername());
		stats.put("life", (now - start));
		stats.put("bytesin", bytesIn);
		stats.put("countin", countIn);
		stats.put("lastin", lastIn > 0 ? (now - lastIn) : -1);
		stats.put("bytesout", bytesOut);
		stats.put("countout", countOut);
		stats.put("lastout", lastOut > 0 ? (now - lastOut) : -1);
		stats.put("hb", heartbeatCount);
		stats.put("lasthb", lastHeartbeat > 0 ? (now - lastHeartbeat) : -1);
		return stats;
	}
}
