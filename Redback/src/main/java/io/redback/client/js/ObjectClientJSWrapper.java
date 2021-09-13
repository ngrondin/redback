package io.redback.client.js;

import io.firebus.data.DataMap;
import io.redback.client.ObjectClient;
import io.redback.client.RedbackObjectRemote;
import io.redback.exceptions.RedbackException;
import io.redback.security.Session;
import io.redback.utils.js.CallableJSWrapper;
import io.redback.utils.js.ObjectJSWrapper;

public class ObjectClientJSWrapper extends ObjectJSWrapper {
	protected ObjectClient objectClient;
	protected Session session;
	protected String domainLock;

	public ObjectClientJSWrapper(ObjectClient oc, Session s)
	{
		super(new String[] {"getObject", "listObjects", "listAllObjects", "listObjects", "createObject", "execute"});
		objectClient = oc;
		session = s;
	}
	
	public ObjectClientJSWrapper(ObjectClient oc, Session s, String dl)
	{
		super(new String[] {"getObject", "listObjects", "listAllObjects", "listObjects", "createObject", "execute"});
		objectClient = oc;
		session = s;
		domainLock = dl;
	}
	
	public Object get(String key) {
		if(key.equals("getObject")) {
			return new CallableJSWrapper() {
				public Object call(Object... arguments) throws RedbackException {
					String objectname = (String)arguments[0];
					String uid = (String)arguments[1];
					RedbackObjectRemote ror = objectClient.getObject(session, objectname, uid);
					if(domainLock == null || (domainLock != null && ror.getDomain().equals(domainLock)))
						return new RedbackObjectRemoteJSWrapper(ror);
					else
						return null;
				}
			};
		} else if(key.equals("listObjects")) {
			return new CallableJSWrapper() {
				public Object call(Object... arguments) throws RedbackException {
					String objectname = (String)arguments[0];
					DataMap filter = (DataMap)(arguments[1]);
					DataMap sort = arguments.length > 2 ? (DataMap)(arguments[2]) : null;
					if(domainLock != null)
						filter.put("domain", domainLock);
					return objectClient.listObjects(session, objectname, filter, sort);
				}
			};
		} else if(key.equals("listAllObjects")) {
			return new CallableJSWrapper() {
				public Object call(Object... arguments) throws RedbackException {
					String objectname = (String)arguments[0];
					DataMap filter = (DataMap)(arguments[1]);
					DataMap sort = arguments.length > 2 ? (DataMap)(arguments[2]) : null;
					boolean addRelated = arguments.length > 3 ? (Boolean)arguments[3] : true;
					if(domainLock != null)
						filter.put("domain", domainLock);
					return objectClient.listAllObjects(session, objectname, filter, sort, addRelated);
				}
			};
		} else if(key.equals("createObject")) {
			return new CallableJSWrapper() {
				public Object call(Object... arguments) throws RedbackException {
					String objectname = (String)arguments[0];
					String domain = arguments.length == 2 ? null : (String)arguments[1]; 
					DataMap data = (DataMap)(arguments.length == 2 ? arguments[1] : arguments[2]);
					if(domainLock != null)
						domain = domainLock;
					return objectClient.createObject(session, objectname, domain, data, true);
				}
			};
		} else if(key.equals("execute")) {
			return new CallableJSWrapper() {
				public Object call(Object... arguments) throws RedbackException {
					String objectname = (String)arguments[0];
					String uid = (String)arguments[1];
					String function = (String)arguments[2];
					DataMap data = arguments.length > 3 ? (DataMap)(arguments[3]) : null;
					objectClient.execute(session, objectname, uid, function, data);
					return null;
				}
			};		} else {
			return null;
		}
	}	
}
