package io.redback.services;


import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import io.firebus.Firebus;
import io.firebus.Payload;
import io.firebus.StreamEndpoint;
import io.firebus.data.DataException;
import io.firebus.data.DataList;
import io.firebus.data.DataMap;
import io.firebus.information.ServiceInformation;
import io.firebus.information.StreamInformation;
import io.redback.exceptions.RedbackException;
import io.redback.exceptions.RedbackInvalidRequestException;
import io.redback.security.Session;
import io.redback.services.common.AuthenticatedDualProvider;
import io.redback.utils.RedbackFile;
import io.redback.utils.RedbackFileMetaData;
import io.redback.utils.RedbackObjectIdentifier;

public abstract class FileServer extends AuthenticatedDualProvider
{
	protected boolean enableStream;
	
	public FileServer(String n, DataMap c, Firebus f) {
		super(n, c, f);
	}
	
	public Payload redbackUnauthenticatedService(Session session, Payload payload) throws RedbackException {
		throw new RedbackException("Redback File Service always needs to receive authenticated requests");
	}

	public Payload redbackAcceptUnauthenticatedStream(Session session, Payload payload, StreamEndpoint streamEndpoint) throws RedbackException {
		throw new RedbackException("Redback File Service always needs to receive authenticated requests");	
	}
	
	public Payload redbackAuthenticatedService(Session session, Payload payload) throws RedbackException
	{
		try {
			Payload response = null;
			if(payload.metadata.containsKey("filename") && payload.metadata.containsKey("mime"))
			{
				String fileName = payload.metadata.get("filename");
				String mime = payload.metadata.get("mime");
				RedbackFileMetaData newFilemd = putFile(session, fileName, mime, session.getUserProfile().getUsername(), payload.getBytes());
				if(payload.metadata.containsKey("object") && payload.metadata.containsKey("uid"))
				{
					String object = payload.metadata.get("object");
					String uid = payload.metadata.get("uid");
					linkFileTo(session, newFilemd.fileuid, object, uid);
				}
				DataMap resp = new DataMap();
				resp.put("fileuid", newFilemd.fileuid);
				resp.put("thumbnail", newFilemd.thumbnail);
				resp.put("mime", newFilemd.mime);
				response = new Payload(resp);
				response.metadata.put("mime", "application/json");
			}
			else
			{
				DataMap request = payload.getDataMap();
				String action = request.getString("action");
				if(action == null) {
					if(request.containsKey("fileuid") && request.containsKey("object") && request.containsKey("uid"))
						action = "link";
					else if(request.containsKey("fileuid"))
						action = "get";
					else if(request.containsKey("object") && request.containsKey("uid"))
						action = "list";
				}
				DataMap options = request.getObject("options");
				boolean addThumbnail = true;
				
				if(action != null) 
				{
					if(options != null)
					{
						addThumbnail = options.getBoolean("addthumbnail");
					}
					
					if(action.equals("get"))
					{
						RedbackFile file = getFile(request.getString("fileuid"));
						response = new Payload(file.bytes);
						response.metadata.put("mime", file.metadata.mime);
						response.metadata.put("filename", file.metadata.fileName);
						response.metadata.put("uid", file.metadata.fileuid);
						response.metadata.put("username", file.metadata.username);
						response.metadata.put("date", file.metadata.date.toInstant().toString());
					} 
					else if(action.equals("getmetadata")) 
					{
						RedbackFileMetaData filemd = this.getMetadata(request.getString("fileuid"));
						response = new Payload(filemd.getDataMap(addThumbnail));
						response.metadata.put("mime", "application/json");
					} 
					else if(action.equals("link")) 
					{
						String object = request.getString("object");
						String uid = request.getString("uid");
						String fileUid = request.getString("fileuid");
						linkFileTo(session, fileUid, object, uid);
						response = new Payload((new DataMap("result", "ok")));
						response.metadata.put("mime", "application/json");
					}
					else if(action.equals("unlink")) 
					{
						String object = request.getString("object");
						String uid = request.getString("uid");
						String fileUid = request.getString("fileuid");
						unlinkFileFrom(session, fileUid, object, uid);
						response = new Payload((new DataMap("result", "ok")));
						response.metadata.put("mime", "application/json");
					}					
					else if(action.equals("list"))
					{
						String object = request.getString("object");
						String uid = request.getString("uid");
						int page = request.containsKey("page") ? request.getNumber("page").intValue() : 0;
						int pageSize = request.containsKey("pagesize") ? request.getNumber("pagesize").intValue() : 50;
						List<RedbackFileMetaData> list = listFilesFor(session, object, uid, page, pageSize);
						DataMap resp = new DataMap();
						DataList respList = new DataList();
						for(RedbackFileMetaData fileMD : list) 
							respList.add(fileMD.getDataMap(addThumbnail));
						resp.put("list", respList);
						response = new Payload(resp);
						response.metadata.put("mime", "application/json");
					}
					else if(action.equals("listmulti"))
					{
						List<RedbackObjectIdentifier> objectIdentifiers = new ArrayList<RedbackObjectIdentifier>();
						int page = request.containsKey("page") ? request.getNumber("page").intValue() : 0;
						int pageSize = request.containsKey("pagesize") ? request.getNumber("pagesize").intValue() : 50;
						DataList list = request.getList("objects");
						if(list != null) {
							for(int i = 0; i < list.size(); i++) {
								DataMap item = list.getObject(i);
								objectIdentifiers.add(new RedbackObjectIdentifier(item.getString("object"), item.getString("uid")));
							}
						}
						Map<RedbackObjectIdentifier, List<RedbackFileMetaData>> filesMap = listFilesForMulti(session, objectIdentifiers, page, pageSize);
						DataList respList = new DataList();
						for(RedbackObjectIdentifier objectIdentifier : filesMap.keySet()) {
							DataList fileList = new DataList();
							for(RedbackFileMetaData fileMD: filesMap.get(objectIdentifier)) {
								fileList.add(fileMD.getDataMap(addThumbnail));
							}
							DataMap objectMap = new DataMap("object", objectIdentifier.objectname, "uid", objectIdentifier.uid, "list", fileList);
							respList.add(objectMap);
						}
						DataMap resp = new DataMap("list", respList);
						response = new Payload(resp);
						response.metadata.put("mime", "application/json");
					}					
				}
			}
			if(response == null)
			{
				response = new Payload(new DataMap("error", "no action taken"));
			}
			return response;
		} catch(DataException e) {
			throw new RedbackException("Error in file server", e);
		}
	}

	public Payload redbackAcceptAuthenticatedStream(Session session, Payload payload, StreamEndpoint streamEndpoint) throws RedbackException {
		try {
			DataMap request = payload.getDataMap();
			String action = request.getString("action");
			Payload acceptPayload = null;
			if(action.equals("get")) {
				String fileuid = request.getString("fileuid");
				if(fileuid != null) {
					RedbackFileMetaData fmd = acceptGetStream(session, streamEndpoint, fileuid);
					acceptPayload = new Payload(new DataMap("filename", fmd.fileName, "mime", fmd.mime));
				} else { // For backwards compatibility, to be removed
					String objectname = request.getString("object");
					String objectuid = request.getString("uid");
					if(objectname != null && objectuid != null) {
						acceptListFilesForStream(session, streamEndpoint, objectname, objectuid);
					}
				}
			} else if(action.equals("put")) {
				final String fileName = request.getString("filename");
				final int fileSize = request.containsKey("filesize") ? request.getNumber("filesize").intValue() : -1;
				final String mime = request.getString("mime");
				final String objectname = request.getString("object");
				final String objectuid = request.getString("uid");
				acceptPutStream(session, streamEndpoint, fileName, fileSize, mime, objectname, objectuid);
			} else if(action.equals("list")) { // This section is only for backwards compatibility of getting the list on the same get path
				String objectname = request.getString("object");
				String objectuid = request.getString("uid");
				acceptListFilesForStream(session, streamEndpoint, objectname, objectuid);
			} else {
				throw new RedbackInvalidRequestException("Invalid file server action '" + action + "'");
			}
			return acceptPayload;			
		} catch(DataException e) {
			throw new RedbackException("Error in file server", e);
		}
	}

	public ServiceInformation getServiceInformation()
	{
		return null;
	}

	public StreamInformation getStreamInformation() {
		return null;
	}

	public int getStreamIdleTimeout() {
		return 10000;
	}

	public abstract RedbackFile getFile(String fileUid) throws RedbackException;

	public abstract RedbackFileMetaData getMetadata(String fileUid) throws RedbackException;

	public abstract List<RedbackFileMetaData> listFilesFor(Session session, String object, String uid, int page, int pageSize) throws RedbackException;
	
	public abstract Map<RedbackObjectIdentifier, List<RedbackFileMetaData>> listFilesForMulti(Session session, List<RedbackObjectIdentifier> objects, int page, int pageSize) throws RedbackException;

	public abstract void linkFileTo(Session session, String fileUid, String object, String uid) throws RedbackException;
	
	public abstract void unlinkFileFrom(Session session, String fileUid, String object, String uid) throws RedbackException;

	public abstract RedbackFileMetaData putFile(Session session, String fileName, String mime, String username, byte[] bytes) throws RedbackException;
	
	public abstract RedbackFileMetaData acceptGetStream(Session session, StreamEndpoint streamEndpoint, String fileUid) throws RedbackException;
	
	public abstract void acceptPutStream(Session session, StreamEndpoint streamEndpoint, String filename, int filesize, String mime, String objectname, String objectuid) throws RedbackException;
	
	public abstract void acceptListFilesForStream(Session session, StreamEndpoint streamEndpoint, String objectname, String objectuid) throws RedbackException;
	
}
