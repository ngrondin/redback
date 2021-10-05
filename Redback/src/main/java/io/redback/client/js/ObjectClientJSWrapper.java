package io.redback.client.js;

import java.util.ArrayList;
import java.util.Arrays;
//import java.util.logging.Logger;
import java.util.List;

import org.graalvm.polyglot.Value;
import org.graalvm.polyglot.proxy.ProxyArray;
import org.graalvm.polyglot.proxy.ProxyExecutable;
import org.graalvm.polyglot.proxy.ProxyObject;

import io.firebus.data.DataList;
import io.firebus.data.DataMap;
import io.redback.client.ObjectClient;
import io.redback.client.RedbackAggregateRemote;
import io.redback.client.RedbackObjectRemote;
import io.redback.managers.objectmanager.requests.MultiRequest;
import io.redback.managers.objectmanager.requests.MultiResponse;
import io.redback.security.Session;
import io.redback.utils.js.JSConverter;

public class ObjectClientJSWrapper implements ProxyObject {
	
	//private Logger logger = Logger.getLogger("io.redback");
	protected ObjectClient objectClient;
	protected Session session;
	protected String domainLock;
	protected String[] members = {"getObject", "listObjects", "listAllObjects", "listObjects", "createObject", "updateObject", "execute", "multi", "aggregate"};

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
					DataMap sort = arguments.length > 2 ? (DataMap)JSConverter.toJava(arguments[2]) : null;
					if(domainLock != null)
						filter.put("domain", domainLock);
					try
					{
						Object o = objectClient.listObjects(session, objectname, filter, sort);
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
					DataMap sort = arguments.length > 2 ? (DataMap)JSConverter.toJava(arguments[2]) : null;
					boolean addRelated = arguments.length > 3 ? arguments[3].asBoolean() : true;
					if(domainLock != null)
						filter.put("domain", domainLock);
					try
					{
						Object o = objectClient.listAllObjects(session, objectname, filter, sort, addRelated);
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
		} else if(key.equals("updateObject")) {
			return new ProxyExecutable() {
				public Object execute(Value... arguments) {
					String objectname = arguments[0].asString();
					String uid = arguments[1].asString(); 
					DataMap data = (DataMap)JSConverter.toJava(arguments[2]);
					try
					{
						Object o = objectClient.updateObject(session, objectname, uid, data, true);
						return JSConverter.toJS(o);
					}
					catch(Exception e)
					{
						throw new RuntimeException("Error updating remote object", e);
					}
				}
			};			
		} else if(key.equals("execute")) {
			return new ProxyExecutable() {
				public Object execute(Value... arguments) {
					String objectname = arguments[0].asString();
					String uid = arguments[1].asString();
					String function = arguments[2].asString();
					DataMap data = arguments.length > 3 ? (DataMap)JSConverter.toJava(arguments[3]) : null;
					try
					{
						objectClient.execute(session, objectname, uid, function, data);
						return null;
					}
					catch(Exception e)
					{
						throw new RuntimeException("Error executing function on remote object", e);
					}
				}
			};		
		} else if(key.equals("multi")) {
			return new ProxyExecutable() {
				public Object execute(Value... arguments) {
					DataList list = (DataList)JSConverter.toJava(arguments[0]);
					try
					{
						MultiResponse mr = objectClient.multi(session, new MultiRequest(list));
						return null;//TODO Return a response for multi gets and lists
					}
					catch(Exception e)
					{
						throw new RuntimeException("Error executing function on remote object", e);
					}
				}
			};		
		} else if(key.equals("aggregate")) {
			return new ProxyExecutable() {
				public Object execute(Value... arguments) {
					String objectname = arguments[0].asString();
					DataMap filter = (DataMap)JSConverter.toJava(arguments[1]);
					DataList tuple = (DataList)JSConverter.toJava(arguments[2]);
					DataList metrics = (DataList)JSConverter.toJava(arguments[3]);
					DataMap sort = (DataMap)JSConverter.toJava(arguments[4]);
					try
					{
						List<RedbackAggregateRemote> rarList = objectClient.aggregate(session, objectname, filter, null, tuple, metrics, sort, null, true, 0, 5000);
						List<RedbackAggregateRemoteJSWrapper> list = new ArrayList<RedbackAggregateRemoteJSWrapper>();
						for(RedbackAggregateRemote rar: rarList)
							list.add(new RedbackAggregateRemoteJSWrapper(rar));
						return list;
					}
					catch(Exception e)
					{
						throw new RuntimeException("Error executing function on remote object", e);
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
