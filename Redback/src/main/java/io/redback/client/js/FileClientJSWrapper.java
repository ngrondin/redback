package io.redback.client.js;

import java.util.Arrays;
import java.util.logging.Logger;

import org.graalvm.polyglot.Value;
import org.graalvm.polyglot.proxy.ProxyArray;
import org.graalvm.polyglot.proxy.ProxyExecutable;
import org.graalvm.polyglot.proxy.ProxyObject;

import io.redback.client.FileClient;
import io.redback.security.Session;

public class FileClientJSWrapper implements ProxyObject {
	
	private Logger logger = Logger.getLogger("io.redback");
	protected FileClient fileClient;
	protected Session session;
	protected String[] members = {"linkFileTo"};

	public FileClientJSWrapper(FileClient fc, Session s)
	{
		fileClient = fc;
		session = s;
	}
	
	public Object getMember(String key) {
		if(key.equals("linkFileTo")) {
			return new ProxyExecutable() {
				public Object execute(Value... arguments) {
					String fileUid = arguments[0].toString();
					String object = arguments[1].toString();
					String objectuid = arguments[2].toString();
					try
					{
						fileClient.linkFileTo(session, fileUid, object, objectuid);
					}
					catch(Exception e)
					{
						logger.severe("Error link file to object :" + e);
					}
					return null;
				}
			};
		} else {
			return null;
		}
	}

	public Object getMemberKeys() {
		return ProxyArray.fromArray(((Object[])members));
	}
	
	public boolean hasMember(String key) {
		return Arrays.asList(members).contains(key);
	}

	public void putMember(String key, Value value) {
		
	}
	
	
}
