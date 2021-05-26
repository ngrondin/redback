package io.redback.services;


import org.apache.commons.codec.binary.Base64;

import io.firebus.Payload;
import io.firebus.utils.DataList;
import io.firebus.utils.DataMap;
import io.redback.RedbackException;
import io.redback.security.Session;
import io.redback.services.common.StreamHandler;
import io.redback.utils.StringUtils;

public abstract class ClientStreamHandler extends StreamHandler {

	public ClientStreamHandler(Session s) {
		super(s);
	}

	public void receiveData(Payload payload) throws RedbackException {
		try {
			DataMap data = new DataMap(payload.getString());
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
					requestService(reqUid, serviceName, request);
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
		this.sendStreamData(new Payload(data.toString()));
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
	
	public abstract void registerDevice(String deviceId, String deviceModel, String os, String appVersion, String locationPermissions) throws RedbackException;
	
	public abstract void subscribeObject(String objectname, String uid) throws RedbackException;
	
	public abstract void subscribeFilter(String objectname, DataMap filter, String id) throws RedbackException;
	
	public abstract void requestService(String requid, String service, DataMap data) throws RedbackException;
	
	public abstract void startUpload(String uploaduid, String filename, int filesize, String mime, String object, String uid) throws RedbackException;

	public abstract void uploadChunk(String uploaduid, int chunkSequence, byte[] bytes) throws RedbackException;
	
	public abstract void finishUpload(String uploaduid) throws RedbackException;

	public abstract void receiveObjectData(DataMap data) throws RedbackException;
	
	public abstract void receiveNotification(DataMap data) throws RedbackException;
	
	public abstract void receiveChat(DataMap data) throws RedbackException;
	
	public abstract void clientStreamClosed() throws RedbackException;
}
