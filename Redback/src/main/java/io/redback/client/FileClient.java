package io.redback.client;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import io.firebus.Firebus;
import io.firebus.Payload;
import io.firebus.StreamEndpoint;
import io.firebus.data.DataList;
import io.firebus.data.DataMap;
import io.redback.exceptions.RedbackException;
import io.redback.security.Session;
import io.redback.utils.RedbackFile;
import io.redback.utils.RedbackFileMetaData;

public class FileClient extends Client {

	public FileClient(Firebus fb, String sn) 
	{
		super(fb, sn);
	}
	

	public RedbackFile getFile(Session session, String fileUid) throws RedbackException {
		try {
			DataMap req = new DataMap();
			req.put("fileuid", fileUid);
			Payload resp = requestPayload(session, new Payload(req));
			String mime = resp.metadata.get("mime");
			String filename = resp.metadata.get("filename");
			String username = resp.metadata.get("username");
			String dateStr = resp.metadata.get("date");
			String thumbnail = resp.metadata.get("thumbnail");
			RedbackFileMetaData filemd = new RedbackFileMetaData(fileUid, filename, mime, thumbnail, username, Date.from(ZonedDateTime.parse(dateStr).toInstant()), null);
			byte[] bytes = resp.getBytes();
			return new RedbackFile(filemd, bytes);
		} catch(Exception e) {
			throw new RedbackException("Error getting file", e);
		}
	}
	
	public StreamEndpoint getFileStream(Session session, String fileUid) throws RedbackException {
		try {
			DataMap req = new DataMap();
			req.put("fileuid", fileUid);
			StreamEndpoint sep = this.requestStream(session, req);
			return sep;
		} catch(Exception e) {
			throw new RedbackException("Error getting file", e);
		}	
	}
	
	public DataMap getMetadata(Session session, String fileUid) throws RedbackException {
		try {
			DataMap req = new DataMap();
			req.put("action", "getmetadata");
			req.put("fileuid", fileUid);
			DataMap resp = requestDataMap(session, req);
			return resp;
		} catch(Exception e) {
			throw new RedbackException("Error getting file", e);
		}
	}

	public List<String> listFileUidsFor(Session session, String object, String uid)  throws RedbackException {
		List<String> uids = new ArrayList<String>();
		try {
			DataMap req = new DataMap();
			req.put("object", object);
			req.put("uid", uid);
			Payload resp = requestPayload(session, new Payload(req));
			DataMap respMap = new DataMap(resp.getString());
			DataList list = respMap.getList("list");
			for(int i = 0; i < list.size(); i++)
				uids.add(list.getObject(i).getString("fileuid"));
			return uids;
		} catch(Exception e) {
			throw new RedbackException("Error listing file uids", e);
		}
	}
	
	public List<RedbackFile> listFilesFor(Session session, String object, String uid)  throws RedbackException {
		List<RedbackFile> files = new ArrayList<RedbackFile>();
		try {
			DataMap req = new DataMap();
			req.put("object", object);
			req.put("uid", uid);
			Payload resp = requestPayload(session, new Payload(req));
			DataMap respMap = new DataMap(resp.getString());
			DataList list = respMap.getList("list");
			for(int i = 0; i < list.size(); i++) {
				DataMap map = list.getObject(i);
				String fileUid = map.getString("fileuid");
				RedbackFile file = getFile(session, fileUid);
				files.add(file);
			}
			
		} catch(Exception e) {
			throw new RedbackException("Error listing file", e);
		}
		return files;
	}

	public void linkFileTo(Session session, String fileUid, String object, String uid)  throws RedbackException {
		try {
			DataMap req = new DataMap();
			req.put("fileuid", fileUid);
			req.put("object", object);
			req.put("uid", uid);
			requestDataMap(session, req);
		} catch(Exception e) {
			throw new RedbackException("Error link files to object", e);
		}		
	}

	public RedbackFileMetaData putFile(Session session, String fileName, String mime, String username, byte[] bytes) throws RedbackException {
		try {
			Payload payload = new Payload(bytes);
			payload.metadata.put("filename", fileName);
			payload.metadata.put("mime", mime);
			Payload respPayload = requestPayload(session, payload);
			DataMap resp = new DataMap(respPayload.getString());
			return new RedbackFileMetaData(resp.getString("fileuid"), fileName, mime, resp.getString("thumbnail"), username, new Date(), null);
		} catch(Exception e) {
			throw new RedbackException("Error link files to object", e);
		}			
	}
	
	public StreamEndpoint putFileStream(Session session, String fileName, int filesize, String mime) throws RedbackException {
		try {
			DataMap req = new DataMap();
			req.put("action", "put");
			req.put("filename", fileName);
			req.put("filesize", filesize);
			req.put("mime", mime);
			req.put("username", session.getUserProfile().getUsername());
			StreamEndpoint sep = requestStream(session, req);
			return sep;
		} catch(Exception e) {
			throw new RedbackException("Error link files to object", e);
		}			
	}

}
