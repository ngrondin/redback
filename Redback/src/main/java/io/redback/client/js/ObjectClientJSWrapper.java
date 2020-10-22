package io.redback.client.js;

import java.util.Arrays;
//import java.util.logging.Logger;

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
	
	//private Logger logger = Logger.getLogger("io.redback");
	protected ObjectClient objectClient;
	protected Session session;
	protected String domainLock;
	protected String[] members = {"getObject", "listObjects", "listAllObjects", "listObjects", "createObject"};

	public ObjectClientJSWrapper(ObjectClient oc, Session s)
	{
		objectClient = oc;
		session = s;
	}
	
	public ObjectClientJSWrapper(ObjectClient oc, Session s, String dl)
	{
		objectClient = oc;
		session = s;
		domainLock = dl;
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
						if(domainLock == null || (domainLock != null && ror.getDomain().equals(domainLock)))
							return new RedbackObjectRemoteJSWrapper(ror);
						else
							return null;
					}
					catch(Exception e)
					{
						throw new RuntimeException("Error getting remote object", e);
					}
				}
			};
		} else if(key.equals("listObjects")) {
			return new ProxyExecutable() {
				public Object execute(Value... arguments) {
					String objectname = arguments[0].asString();
					DataMap filter = (DataMap)JSConverter.toJava(arguments[1]);
					if(domainLock != null)
						filter.put("domain", domainLock);
					try
					{
						Object o = objectClient.listObjects(session, objectname, filter);
						return JSConverter.toJS(o);
					}
					catch(Exception e)
					{
						throw new RuntimeException("Error listing remote object", e);
					}
				}
			};
		} else if(key.equals("listAllObjects")) {
			return new ProxyExecutable() {
				public Object execute(Value... arguments) {
					String objectname = arguments[0].asString();
					DataMap filter = (DataMap)JSConverter.toJava(arguments[1]);
					if(domainLock != null)
						filter.put("domain", domainLock);
					try
					{
						Object o = objectClient.listAllObjects(session, objectname, filter, true);
						return JSConverter.toJS(o);
					}
					catch(Exception e)
					{
						throw new RuntimeException("Error listing remote object", e);
					}
				}
			};
		} else if(key.equals("createObject")) {
			return new ProxyExecutable() {
				public Object execute(Value... arguments) {
					String objectname = arguments[0].asString();
					String domain = arguments.length == 2 ? null : arguments[1].asString(); 
					DataMap data = (DataMap)JSConverter.toJava(arguments.length == 2 ? arguments[1] : arguments[2]);
					if(domainLock != null)
						domain = domainLock;
					try
					{
						Object o = objectClient.createObject(session, objectname, domain, data, true);
						return JSConverter.toJS(o);
					}
					catch(Exception e)
					{
						throw new RuntimeException("Error listing remote object", e);
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
