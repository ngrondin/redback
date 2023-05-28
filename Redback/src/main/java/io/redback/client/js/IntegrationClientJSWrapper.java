package io.redback.client.js;


import io.firebus.data.DataMap;
import io.redback.client.IntegrationClient;
import io.redback.exceptions.RedbackException;
import io.redback.security.Session;
import io.redback.utils.js.CallableJSWrapper;
import io.redback.utils.js.ObjectJSWrapper;

public class IntegrationClientJSWrapper extends ObjectJSWrapper {
	
	protected IntegrationClient integrationClient;
	protected String domain;
	protected Session session;

	public IntegrationClientJSWrapper(IntegrationClient ic, Session s, String d)
	{
		super(new String[] {"get", "list", "update", "create", "delete", "clearCachedClientData"});
		integrationClient = ic;
		domain = d;
		session = s;
	}
	
	public Object get(String key) {
		if(key.equals("get")) {
			return new CallableJSWrapper() {
				public Object call(Object... arguments) throws RedbackException {
					String client = (String)arguments[0];
					String objectName = (String)arguments[1];
					String uid = (String)arguments[2];
					DataMap options = arguments.length > 3 ? (DataMap)(arguments[3]) : null;
					return integrationClient.get(session, client, domain, objectName, uid, options);
				}
			};
		} else if(key.equals("list")) {
			return new CallableJSWrapper() {
				public Object call(Object... arguments) throws RedbackException {
					String client = (String)arguments[0];
					String objectName = (String)arguments[1];
					DataMap filter = (DataMap)(arguments[2]);
					DataMap options = arguments.length > 3 ? (DataMap)(arguments[3]) : null;
					return integrationClient.list(session, client, domain, objectName, filter, options);
				}
			};
		} else if(key.equals("update")) {
			return new CallableJSWrapper() {
				public Object call(Object... arguments) throws RedbackException {
					String client = (String)arguments[0];
					String objectName = (String)arguments[1];
					String uid = (String)arguments[2];
					Object data = arguments[3];
					DataMap options = arguments.length > 4 ? (DataMap)(arguments[4]) : null;
					return integrationClient.update(session, client, domain, objectName, uid, data, options);
				}
			};
		} else if(key.equals("create")) {
			return new CallableJSWrapper() {
				public Object call(Object... arguments) throws RedbackException {
					String client = (String)arguments[0];
					String objectName = (String)arguments[1];
					Object data = arguments[2];
					DataMap options = arguments.length > 3 ? (DataMap)(arguments[3]) : null;
					return integrationClient.create(session, client, domain, objectName, data, options);
				}
			};
		} else if(key.equals("delete")) {
			return new CallableJSWrapper() {
				public Object call(Object... arguments) throws RedbackException {
					String client = (String)arguments[0];
					String objectName = (String)arguments[1];
					String uid = (String)arguments[2];
					DataMap options = arguments.length > 3 ? (DataMap)(arguments[3]) : null;
					return integrationClient.delete(session, client, domain, objectName, uid, options);
				}
			};
		} else if(key.equals("clearCachedClientData")) {
			return new CallableJSWrapper() {
				public Object call(Object... arguments) throws RedbackException {
					String client = (String)arguments[0];
					integrationClient.clearCachedClientData(session, client, domain);
					return null;
				}
			};
		} else {
			return new IntegrationClientInstanceJSWrapper(integrationClient, session, domain, key);
		}
	}	
}
