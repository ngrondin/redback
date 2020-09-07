package io.redback.client.js;

import java.util.Arrays;
import java.util.logging.Logger;

import org.graalvm.polyglot.Value;
import org.graalvm.polyglot.proxy.ProxyArray;
import org.graalvm.polyglot.proxy.ProxyExecutable;
import org.graalvm.polyglot.proxy.ProxyObject;

import io.firebus.utils.DataMap;
import io.redback.client.ObjectClient;
import io.redback.client.RedbackObjectRemote;
import io.redback.security.Session;
import io.redback.utils.js.JSConverter;

public class ObjectClientJSWrapper implements ProxyObject {
	
	private Logger logger = Logger.getLogger("io.redback");
	protected ObjectClient objectClient;
	protected Session session;
	protected String[] members = {"getObject", "listObjects", "listAllObjects", "listObjects"};

	public ObjectClientJSWrapper(ObjectClient oc, Session s)
	{
		objectClient = oc;
		session = s;
	}
	
	public Object getMember(String key) {
		if(key.equals("getObject")) {
			return new ProxyExecutable() {
				public Object execute(Value... arguments) {
					String objectname = arguments[0].asString();
					String uid = arguments[1].asString();
					try
					{
						RedbackObjectRemote ror = objectClient.getObject(session, objectname, uid);
						return new RedbackObjectRemoteJSWrapper(ror);
					}
					catch(Exception e)
					{
						logger.severe("Error getting remote object :" + e);
					}
					return null;
				}
			};
		} else if(key.equals("listObjects")) {
			return new ProxyExecutable() {
				public Object execute(Value... arguments) {
					String objectname = arguments[0].asString();
					DataMap filter = (DataMap)JSConverter.toJava(arguments[1]);
					try
					{
						Object o = objectClient.listObjects(session, objectname, filter);
						return JSConverter.toJS(o);
					}
					catch(Exception e)
					{
						logger.severe("Error listing remote objects :" + e);
					}
					return null;
				}
			};
		} else if(key.equals("listAllObjects")) {
			return new ProxyExecutable() {
				public Object execute(Value... arguments) {
					String objectname = arguments[0].asString();
					DataMap filter = (DataMap)JSConverter.toJava(arguments[1]);
					try
					{
						Object o = objectClient.listAllObjects(session, objectname, filter, true);
						return JSConverter.toJS(o);
					}
					catch(Exception e)
					{
						logger.severe("Error listing remote objects :" + e);
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
