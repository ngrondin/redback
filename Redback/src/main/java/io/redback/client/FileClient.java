package io.redback.client;

import java.util.Date;
import java.util.List;

import io.firebus.Firebus;
import io.firebus.Payload;
import io.firebus.utils.DataMap;
import io.redback.RedbackException;
import io.redback.security.Session;
import io.redback.utils.RedbackFile;

public class FileClient extends Client {

	public FileClient(Firebus fb, String sn) 
	{
		super(fb, sn);
	}
	

	public RedbackFile getFile(Session session, String fileUid) throws RedbackException {
		try {
			DataMap req = new DataMap();
			req.put("fileuid", fileUid);
			Payload resp = requestPayload(session, new Payload(req.toString()));
			String mime = resp.metadata.get("mime");
			String filename = resp.metadata.get("filename");
			String username = resp.metadata.get("username");
			String dateStr = resp.metadata.get("date");
			String thumbnail = resp.metadata.get("thumbnail");
			byte[] bytes = resp.getBytes();
			return new RedbackFile(fileUid, filename, mime, thumbnail, username, new Date(dateStr), bytes);
		} catch(Exception e) {
			throw new RedbackException("Error getting file", e);
		}
	}

	public List<RedbackFile> listFilesFor(Session session, String object, String uid)  throws RedbackException {
		//TODO complete this
		return null;
	}

	public void linkFileTo(Session session, String fileUid, String object, String uid)  throws RedbackException {
		try {
			DataMap req = new DataMap();
			req.put("fileuid", fileUid);
			req.put("object", object);
			req.put("uid", uid);
			request(session, req);
		} catch(Exception e) {
			throw new RedbackException("Error link files to object", e);
		}		
	}

	public RedbackFile putFile(Session session, String fileName, String mime, String username, byte[] bytes) throws RedbackException {
		//TODO complete this
		return null;
	}

}
