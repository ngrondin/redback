package io.redback.services;

import java.util.List;
import java.util.logging.Logger;

import io.firebus.Firebus;
import io.firebus.Payload;
import io.firebus.exceptions.FunctionErrorException;
import io.firebus.exceptions.FunctionTimeoutException;
import io.firebus.information.ServiceInformation;
import io.firebus.utils.DataException;
import io.firebus.utils.DataList;
import io.firebus.utils.DataMap;
import io.redback.RedbackException;
import io.redback.security.Session;
import io.redback.utils.RedbackFile;

public abstract class FileServer extends AuthenticatedServiceProvider
{
	private Logger logger = Logger.getLogger("io.redback");

	public FileServer(String n, DataMap c, Firebus f)
	{
		super(n, c, f);
	}
	
	public Payload unAuthenticatedService(Session session, Payload payload) throws FunctionErrorException
	{
		throw new FunctionErrorException("Redback File Service always needs to receive authenticated requests");
	}

	public Payload authenticatedService(Session session, Payload payload) throws FunctionErrorException
	{
		try
		{
			logger.info("Authenticated file service start");
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
					else if(action.equals("link")) 
					{
						String object = request.getString("object");
						String uid = request.getString("uid");
						String fileUid = request.getString("fileuid");
						linkFileTo(fileUid, object, uid);
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
							fileInfo.put("date", file.date.toInstant().toString());
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
			logger.info("Authenticated file service finished");
			return response;
		}
		catch(RedbackException | DataException | FunctionTimeoutException | FunctionErrorException e)
		{
			logger.severe(e.getMessage());
			throw new FunctionErrorException("Error on the file server", e);
		}
		
	}

	public ServiceInformation getServiceInformation()
	{
		// TODO Auto-generated method stub
		return null;
	}

	public abstract RedbackFile getFile(String fileUid) throws DataException, RedbackException, FunctionErrorException, FunctionTimeoutException;

	public abstract List<RedbackFile> listFilesFor(String object, String uid) throws DataException, RedbackException, FunctionErrorException, FunctionTimeoutException;

	public abstract void linkFileTo(String fileUid, String object, String uid) throws DataException, RedbackException, FunctionErrorException, FunctionTimeoutException;

	public abstract RedbackFile putFile(String fileName, String mime, String username, byte[] bytes) throws DataException, RedbackException, FunctionErrorException, FunctionTimeoutException;
	
}
