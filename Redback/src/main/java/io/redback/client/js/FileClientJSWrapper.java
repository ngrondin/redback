package io.redback.client.js;


import io.redback.client.FileClient;
import io.redback.exceptions.RedbackException;
import io.redback.security.Session;
import io.redback.utils.js.CallableJSWrapper;
import io.redback.utils.js.ObjectJSWrapper;

public class FileClientJSWrapper extends ObjectJSWrapper {
	
	protected FileClient fileClient;
	protected Session session;

	public FileClientJSWrapper(FileClient fc, Session s)
	{
		super(new String[] {"linkFileTo", "getMetadata"});
		fileClient = fc;
		session = s;
	}
	
	public Object get(String key) {
		if(key.equals("linkFileTo")) {
			return new CallableJSWrapper() {
				public Object call(Object... arguments) throws RedbackException {
					String fileUid = arguments[0].toString();
					String object = arguments[1].toString();
					String objectuid = arguments[2].toString();
					fileClient.linkFileTo(session, fileUid, object, objectuid);
					return null;
				}
			};
		} else if(key.equals("getMetadata")) {
			return new CallableJSWrapper() {
				public Object call(Object... arguments) throws RedbackException {
					String fileUid = arguments[0].toString();
					return fileClient.getMetadata(session, fileUid);
				}
			};
		} else {
			return null;
		}
	}
}
