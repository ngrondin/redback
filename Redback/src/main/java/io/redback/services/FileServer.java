package io.redback.services;


import java.util.List;

import io.firebus.Firebus;
import io.firebus.Payload;
import io.firebus.StreamEndpoint;
import io.firebus.exceptions.FunctionErrorException;
import io.firebus.information.ServiceInformation;
import io.firebus.information.StreamInformation;
import io.firebus.interfaces.StreamProvider;
import io.firebus.utils.DataException;
import io.firebus.utils.DataList;
import io.firebus.utils.DataMap;
import io.redback.RedbackException;
import io.redback.security.Session;
import io.redback.services.common.AuthenticatedServiceProvider;
import io.redback.utils.RedbackFile;
import io.redback.utils.RedbackFileMetaData;

public abstract class FileServer extends AuthenticatedServiceProvider  implements StreamProvider
{
	protected boolean enableStream;
	
	public FileServer(String n, DataMap c, Firebus f)
	{
		super(n, c, f);
		enableStream = config.getBoolean("enablestream");
		if(enableStream) {
			firebus.registerStreamProvider(n, this, 10);
		}
	}
	
	public Payload redbackUnauthenticatedService(Session session, Payload payload) throws RedbackException
	{
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
				RedbackFileMetaData newFilemd = putFile(fileName, mime, session.getUserProfile().getUsername(), payload.getBytes());
				if(payload.metadata.containsKey("object") && payload.metadata.containsKey("uid"))
				{
					String object = payload.metadata.get("object");
					String uid = payload.metadata.get("uid");
					linkFileTo(newFilemd.fileuid, object, uid);
				}
				DataMap resp = new DataMap();
				resp.put("fileuid", newFilemd.fileuid);
				resp.put("thumbnail", newFilemd.thumbnail);
				response = new Payload(resp.toString());
				response.metadata.put("mime", "application/json");
			}
			else
			{
				DataMap request = new DataMap(payload.getString());
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
						response = new Payload(filemd.getDataMap(addThumbnail).toString());
						response.metadata.put("mime", "application/json");
					} 
					else if(action.equals("link")) 
					{
						String object = request.getString("object");
						String uid = request.getString("uid");
						String fileUid = request.getString("fileuid");
						linkFileTo(fileUid, object, uid);
						response = new Payload((new DataMap("result", "ok")).toString());
						response.metadata.put("mime", "application/json");
					}
					else if(action.equals("unlink")) 
					{
						String object = request.getString("object");
						String uid = request.getString("uid");
						String fileUid = request.getString("fileuid");
						unlinkFileFrom(fileUid, object, uid);
						response = new Payload((new DataMap("result", "ok")).toString());
						response.metadata.put("mime", "application/json");
					}					
					else if(action.equals("list"))
					{
						String object = request.getString("object");
						String uid = request.getString("uid");
						List<RedbackFileMetaData> fileData = listFilesFor(object, uid);
						DataMap resp = new DataMap();
						DataList respList = new DataList();
						for(RedbackFileMetaData filemd : fileData) 
							respList.add(filemd.getDataMap(addThumbnail));
						resp.put("list", respList);
						response = new Payload(resp.toString());
						response.metadata.put("mime", "application/json");
					}
					else if(action.equals("listmulti"))
					{
						DataMap resp = new DataMap();
						DataList itemList = new DataList();
						DataList list = request.getList("objects");
						if(list != null) {
							for(int i = 0; i < list.size(); i++) {
								DataMap item = list.getObject(i);
								String object = item.getString("object");
								String uid = item.getString("uid");
								List<RedbackFileMetaData> fileData = listFilesFor(object, uid);
								DataMap itemResp = new DataMap();
								itemResp.put("object", object);
								itemResp.put("uid", uid);
								DataList fileList = new DataList();
								for(RedbackFileMetaData filemd : fileData) 
									fileList.add(filemd.getDataMap(addThumbnail));
								itemResp.put("list", fileList);
								itemList.add(itemResp);
							}
						}
						resp.put("list", itemList);
						response = new Payload(resp.toString());
						response.metadata.put("mime", "application/json");
					}					
				}
			}
			if(response == null)
			{
				response = new Payload("{error:\"no action taken\"}");
			}
			return response;
		} catch(DataException e) {
			throw new RedbackException("Error in file server", e);
		}
		
	}

	public ServiceInformation getServiceInformation()
	{
		return null;
	}
	
	public Payload acceptStream(Payload payload, StreamEndpoint streamEndpoint) throws FunctionErrorException {
		try {
			Session session = accessManagementClient.getSession(payload);
			if(session.getUserProfile() != null) {
				DataMap request = new DataMap(payload.getString());
				String action = request.getString("action");
				if(action.equals("get")) {
					String fileuid = request.getString("fileuid");
					if(fileuid != null) {
						acceptGetStream(session, streamEndpoint, fileuid);
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
				}
				return null;
			} else {
				throw new FunctionErrorException("All stream requests need to be authenticated");
			}
		} catch(Exception e) {
			throw new FunctionErrorException("Cannot accept stream", e);
		}
	}


	public int getStreamIdleTimeout() {
		return 10000;
	}


	public StreamInformation getStreamInformation() {
		return null;
	}

	public abstract RedbackFile getFile(String fileUid) throws RedbackException;

	public abstract RedbackFileMetaData getMetadata(String fileUid) throws RedbackException;

	public abstract List<RedbackFileMetaData> listFilesFor(String object, String uid) throws RedbackException;

	public abstract void linkFileTo(String fileUid, String object, String uid) throws RedbackException;
	
	public abstract void unlinkFileFrom(String fileUid, String object, String uid) throws RedbackException;

	public abstract RedbackFileMetaData putFile(String fileName, String mime, String username, byte[] bytes) throws RedbackException;
	
	public abstract void acceptGetStream(Session session, StreamEndpoint streamEndpoint, String fileUid) throws RedbackException;
	
	public abstract void acceptPutStream(Session session, StreamEndpoint streamEndpoint, String filename, int filesize, String mime, String objectname, String objectuid) throws RedbackException;
	
	public abstract void acceptListFilesForStream(Session session, StreamEndpoint streamEndpoint, String objectname, String objectuid) throws RedbackException;
	
}
