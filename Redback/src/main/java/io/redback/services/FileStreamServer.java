package io.redback.services;

import io.firebus.Firebus;
import io.firebus.Payload;
import io.firebus.information.StreamInformation;
import io.firebus.utils.DataMap;
import io.redback.RedbackException;
import io.redback.security.Session;

public abstract class FileStreamServer extends AuthenticatedStreamProvider 
{

	public FileStreamServer(String n, DataMap c, Firebus f) 
	{
		super(n, c, f);
	}

	public StreamInformation getStreamInformation() {
		return null;
	}

	protected Payload onNewStream(Session session, Payload payload) throws RedbackException {
		try {
			DataMap request = new DataMap(payload.getString());
			String action = request.getString("action");
			String fileuid = request.getString("fileuid");
			if(action.equals("put")) {
				String mime = request.getString("mime");
				DataMap metadata = new DataMap();
				metadata.put("filename", fileuid);
				metadata.put("mime", mime);
				String fileUid = acceptPutStream(session, metadata);
				DataMap resp = new DataMap("fileuid", fileUid);
				return new Payload(resp.toString());
			} else if(action.equals("get")) {
				DataMap md = acceptGetStream(session, fileuid);
				return new Payload(md.toString());
			} else {
				throw new RedbackException("Invalid action on file stream");
			}
		} catch(Exception e) {
			throw new RedbackException("Error accepting a new file stream", e);
		}
	}

	public void clearCaches() {
		
	}

	public abstract String acceptPutStream(Session session, DataMap metadata) throws RedbackException;
	
	public abstract DataMap acceptGetStream(Session session, String fileUid) throws RedbackException;
}
