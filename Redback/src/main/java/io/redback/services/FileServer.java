package io.redback.services;

import java.util.List;

import io.firebus.Firebus;
import io.firebus.Payload;
import io.firebus.information.ServiceInformation;
import io.firebus.utils.DataException;
import io.firebus.utils.DataList;
import io.firebus.utils.DataMap;
import io.redback.RedbackException;
import io.redback.security.Session;
import io.redback.utils.RedbackFile;

public abstract class FileServer extends AuthenticatedServiceProvider
{
	public FileServer(String n, DataMap c, Firebus f)
	{
		super(n, c, f);
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
				RedbackFile newFile = putFile(fileName, mime, session.getUserProfile().getUsername(), payload.getBytes());
				if(payload.metadata.containsKey("object") && payload.metadata.containsKey("uid"))
				{
					String object = payload.metadata.get("object");
					String uid = payload.metadata.get("uid");
					linkFileTo(newFile.uid, object, uid);
				}
				DataMap resp = new DataMap();
				resp.put("fileuid", newFile.uid);
				resp.put("thumbnail", newFile.thumbnail);
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
				if(action != null) 
				{
					if(action.equals("get"))
					{
						RedbackFile file = getFile(request.getString("fileuid"));
						response = new Payload(file.bytes);
						response.metadata.put("mime", file.mime);
						response.metadata.put("filename", file.fileName);
						response.metadata.put("uid", file.uid);
						response.metadata.put("username", file.username);
						response.metadata.put("date", file.date.toInstant().toString());
					} 
					else if(action.equals("getmetadata")) 
					{
						RedbackFile file = getMetadata(request.getString("fileuid"));
						DataMap fileInfo = new DataMap();
						fileInfo.put("fileuid", file.uid);
						fileInfo.put("filename", file.fileName);
						fileInfo.put("mime", file.mime);
						fileInfo.put("thumbnail", file.thumbnail);
						fileInfo.put("username", file.username);
						fileInfo.put("date", file.date.toInstant().toString());
						response = new Payload(fileInfo.toString());
					} 
					else if(action.equals("link")) 
					{
						String object = request.getString("object");
						String uid = request.getString("uid");
						String fileUid = request.getString("fileuid");
						linkFileTo(fileUid, object, uid);
						response = new Payload((new DataMap("result", "ok")).toString());
					}
					else if(action.equals("list"))
					{
						String object = request.getString("object");
						String uid = request.getString("uid");
						List<RedbackFile> files = listFilesFor(object, uid);
						DataMap resp = new DataMap();
						DataList list = new DataList();
						for(int i = 0; i < files.size(); i++) 
						{
							RedbackFile file = files.get(i);
							DataMap fileInfo = new DataMap();
							fileInfo.put("fileuid", file.uid);
							fileInfo.put("filename", file.fileName);
							fileInfo.put("mime", file.mime);
							fileInfo.put("thumbnail", file.thumbnail);
							fileInfo.put("username", file.username);
							fileInfo.put("date", file.date != null ? file.date.toInstant().toString() : null);
							fileInfo.put("relatedobject", object);
							fileInfo.put("relateduid", uid);
							list.add(fileInfo);
						}
						resp.put("list", list);
						response = new Payload(resp.toString());
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
		// TODO Auto-generated method stub
		return null;
	}

	public abstract RedbackFile getFile(String fileUid) throws RedbackException;

	public abstract RedbackFile getMetadata(String fileUid) throws RedbackException;

	public abstract List<RedbackFile> listFilesFor(String object, String uid) throws RedbackException;

	public abstract void linkFileTo(String fileUid, String object, String uid) throws RedbackException;

	public abstract RedbackFile putFile(String fileName, String mime, String username, byte[] bytes) throws RedbackException;
	
}
