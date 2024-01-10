package io.redback.client.js;


import java.util.Base64;
import java.util.List;

import io.firebus.data.DataList;
import io.firebus.data.DataMap;
import io.redback.client.FileClient;
import io.redback.exceptions.RedbackException;
import io.redback.security.Session;
import io.redback.utils.RedbackFile;
import io.redback.utils.RedbackFileMetaData;
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
		} else if(key.equals("listFileUidsFor")) {
			return new CallableJSWrapper() {
				public Object call(Object... arguments) throws RedbackException {
					String object = arguments[0].toString();
					String objectuid = arguments[1].toString();
					List<String> list = fileClient.listFileUidsFor(session, object, objectuid);
					DataList ret = new DataList();
					for(String uid: list)
						ret.add(uid);
					return ret;
				}
			};			
		} else if(key.equals("getMetadata")) {
			return new CallableJSWrapper() {
				public Object call(Object... arguments) throws RedbackException {
					String fileUid = arguments[0].toString();
					return fileClient.getMetadata(session, fileUid);
				}
			};
		} else if(key.equals("getFile")) {
			return new CallableJSWrapper() {
				public Object call(Object... arguments) throws RedbackException {
					String fileUid = arguments[0].toString();
					RedbackFile file = fileClient.getFile(session, fileUid);
					DataMap ret = new DataMap();
					ret.put("filename", file.metadata.fileName);
					ret.put("fileuid", file.metadata.fileuid);
					ret.put("mime", file.metadata.mime);
					ret.put("username", file.metadata.username);
					ret.put("date", file.metadata.date);
					String base64 = Base64.getEncoder().encodeToString(file.bytes);
					ret.put("base64", base64);
					return ret;
				}
			};	
		} else if(key.equals("putFile")) {
			return new CallableJSWrapper() {
				public Object call(Object... arguments) throws RedbackException {
					String filename = arguments[0].toString();
					String mime = arguments[1].toString();
					String base64 = arguments[2].toString();
					boolean isUrlEncoded = arguments.length >= 4 ? (boolean)arguments[3] : false;
					byte[] bytes = isUrlEncoded ? Base64.getUrlDecoder().decode(base64) : Base64.getDecoder().decode(base64);
					RedbackFileMetaData md = fileClient.putFile(session, filename, mime, session.getUserProfile().getUsername(), bytes);
					DataMap ret = new DataMap();
					ret.put("thumbnail", md.thumbnail);
					ret.put("fileuid", md.fileuid);
					return ret;
				}
			};				
		} else {
			return null;
		}
	}
}
