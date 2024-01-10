package io.redback.client.js;


import io.firebus.data.DataMap;
import io.firebus.logging.Logger;
import io.redback.client.IntegrationClient;
import io.redback.exceptions.RedbackException;
import io.redback.security.Session;
import io.redback.utils.js.CallableJSWrapper;
import io.redback.utils.js.ObjectJSWrapper;

public class IntegrationClientJSWrapper extends ObjectJSWrapper {
	
	protected IntegrationClient integrationClient;
	protected String domainLock;
	protected Session session;

	public IntegrationClientJSWrapper(IntegrationClient ic, Session s)
	{
		super(new String[] {"get", "list", "update", "create", "delete", "clearCachedClientData"});
		integrationClient = ic;
		session = s;
	}
	
	public IntegrationClientJSWrapper(IntegrationClient ic, Session s, String dl)
	{
		super(new String[] {"get", "list", "update", "create", "delete", "clearCachedClientData"});
		integrationClient = ic;
		domainLock = dl;
		session = s;
	}
	
	public Object get(String key) {
		if(key.equals("get")) {
			Logger.severe("Integration Client deprecated function");
			return new CallableJSWrapper() {
				public Object call(Object... arguments) throws RedbackException {
					String client = (String)arguments[0];
					String objectName = (String)arguments[1];
					String uid = (String)arguments[2];
					DataMap options = arguments.length > 3 ? (DataMap)(arguments[3]) : null;
					return integrationClient.get(session, client, domainLock, objectName, uid, options);
				}
			};
		} else if(key.equals("list")) {
			Logger.severe("Integration Client deprecated function");
			return new CallableJSWrapper() {
				public Object call(Object... arguments) throws RedbackException {
					String client = (String)arguments[0];
					String objectName = (String)arguments[1];
					DataMap filter = (DataMap)(arguments[2]);
					DataMap options = arguments.length > 3 ? (DataMap)(arguments[3]) : null;
					return integrationClient.list(session, client, domainLock, objectName, filter, options);
				}
			};
		} else if(key.equals("update")) {
			Logger.severe("Integration Client deprecated function");
			return new CallableJSWrapper() {
				public Object call(Object... arguments) throws RedbackException {
					String client = (String)arguments[0];
					String objectName = (String)arguments[1];
					String uid = (String)arguments[2];
					Object data = arguments[3];
					DataMap options = arguments.length > 4 ? (DataMap)(arguments[4]) : null;
					return integrationClient.update(session, client, domainLock, objectName, uid, data, options);
				}
			};
		} else if(key.equals("create")) {
			Logger.severe("Integration Client deprecated function");
			return new CallableJSWrapper() {
				public Object call(Object... arguments) throws RedbackException {
					String client = (String)arguments[0];
					String objectName = (String)arguments[1];
					Object data = arguments[2];
					DataMap options = arguments.length > 3 ? (DataMap)(arguments[3]) : null;
					return integrationClient.create(session, client, domainLock, objectName, data, options);
				}
			};
		} else if(key.equals("delete")) {
			Logger.severe("Integration Client deprecated function");
			return new CallableJSWrapper() {
				public Object call(Object... arguments) throws RedbackException {
					String client = (String)arguments[0];
					String objectName = (String)arguments[1];
					String uid = (String)arguments[2];
					DataMap options = arguments.length > 3 ? (DataMap)(arguments[3]) : null;
					return integrationClient.delete(session, client, domainLock, objectName, uid, options);
				}
			};
		} else if(key.equals("client")) {
			return new CallableJSWrapper() {
				public Object call(Object... arguments) throws RedbackException {
					String name = (String)arguments[0];
					String domain = domainLock != null ? domainLock : (arguments.length > 1 ? arguments[1].toString() : null);
					if(domain != null) {
						return new IntegrationClientInstanceJSWrapper(integrationClient, session, domain, name);
					} else {
						throw new RedbackException("Integration client no domain specified");
					}
					
				}
			};			
		} else if(domainLock != null) {
			return new IntegrationClientInstanceJSWrapper(integrationClient, session, domainLock, key);
		} else {
			return null; 
		}
	}	
}
