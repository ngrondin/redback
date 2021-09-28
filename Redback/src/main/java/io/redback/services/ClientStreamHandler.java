package io.redback.services;


import org.apache.commons.codec.binary.Base64;

import io.firebus.Payload;
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
	
	public ClientStreamHandler(Session s) {
		super(s);
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
					registerDevice(clientData.getString("deviceid"), clientData.getString("devicemodel"), clientData.getString("os"), clientData.getString("appversion"), clientData.getString("locationpermission"));
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
							if(base64data.indexOf(",") > -1)
								base64data = base64data.split(",")[1];
							byte[] bytes = Base64.decodeBase64(base64data);
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

	public Payload getAcceptPayload() throws RedbackException {
		return null;
	}
	
	public void sendClientData(DataMap data) {
		String payloadStr = data.toString();
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
	
	public abstract void registerDevice(String deviceId, String deviceModel, String os, String appVersion, String locationPermissions) throws RedbackException;
	
	public abstract void subscribeObject(String objectname, String uid) throws RedbackException;
	
	public abstract void subscribeFilter(String objectname, DataMap filter, String id) throws RedbackException;
	
	public abstract void requestService(String requid, String service, DataMap data, int timeout) throws RedbackException;
	
	public abstract void startUpload(String uploaduid, String filename, int filesize, String mime, String object, String uid) throws RedbackException;

	public abstract void uploadChunk(String uploaduid, int chunkSequence, byte[] bytes) throws RedbackException;
	
	public abstract void finishUpload(String uploaduid) throws RedbackException;

	public abstract void receiveObjectData(DataMap data) throws RedbackException;
	
	public abstract void receiveNotification(DataMap data) throws RedbackException;
	
	public abstract void receiveChat(DataMap data) throws RedbackException;
	
	public abstract void clientStreamClosed() throws RedbackException;
	
	protected String getStatString() {
		long now = System.currentTimeMillis();
		StringBuilder sb = new StringBuilder();
		sb.append("life: ");
		sb.append(now - start);
		sb.append("ms");
		sb.append("  in: ");
		sb.append(bytesIn);
		sb.append("b/");
		sb.append(countIn);
		sb.append("/");
		sb.append(lastIn > 0 ? (now - lastIn) + "ms" : "-");
		sb.append("  out: ");
		sb.append(bytesOut);
		sb.append("b/");
		sb.append(countOut);
		sb.append("/");
		sb.append(lastOut > 0 ? (now - lastOut) + "ms" : "-");
		sb.append("  hb_count: ");
		sb.append(heartbeatCount);
		sb.append("  last_hb: ");
		sb.append(lastHeartbeat > 0 ? (now - lastHeartbeat) + "ms" : "-");
		return sb.toString();
	}
}
