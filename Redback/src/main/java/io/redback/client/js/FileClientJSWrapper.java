package io.redback.client.js;

import java.util.Arrays;

import org.graalvm.polyglot.Value;
import org.graalvm.polyglot.proxy.ProxyArray;
import org.graalvm.polyglot.proxy.ProxyExecutable;
import org.graalvm.polyglot.proxy.ProxyObject;

import io.firebus.data.DataMap;
import io.redback.client.FileClient;
import io.redback.security.Session;
import io.redback.utils.js.JSConverter;

public class FileClientJSWrapper implements ProxyObject {
	
	protected FileClient fileClient;
	protected Session session;
	protected String[] members = {"linkFileTo", "getMetadata"};

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
						return null;
					}
					catch(Exception e)
					{
						throw new RuntimeException("Error linking file to object", e);
					}
				}
			};
		} else if(key.equals("getMetadata")) {
			return new ProxyExecutable() {
				public Object execute(Value... arguments) {
					String fileUid = arguments[0].toString();
					try
					{
						DataMap resp = fileClient.getMetadata(session, fileUid);
						return JSConverter.toJS(resp);
					}
					catch(Exception e)
					{
						throw new RuntimeException("Error getting metadata of file", e);
					}
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
