package io.redback.client.js;


import io.firebus.data.DataMap;
import io.redback.client.IntegrationClient;
import io.redback.exceptions.RedbackException;
import io.redback.security.Session;
import io.redback.utils.js.CallableJSWrapper;
import io.redback.utils.js.ObjectJSWrapper;

public class IntegrationClientInstanceJSWrapper extends ObjectJSWrapper {
	
	protected IntegrationClient integrationClient;
	protected String domain;
	protected Session session;
	protected String name;

	public IntegrationClientInstanceJSWrapper(IntegrationClient ic, Session s, String d, String n)
	{
		super(new String[] {"get", "list", "update", "create", "delete"});
		integrationClient = ic;
		domain = d;
		session = s;
		name = n;
	}
	
	public Object get(String key) {
		if(key.equals("get")) {
			return new CallableJSWrapper() {
				public Object call(Object... arguments) throws RedbackException {
					String objectName = (String)arguments[0];
					String uid = (String)arguments[1];
					DataMap options = arguments.length > 2 ? (DataMap)(arguments[2]) : null;
					return integrationClient.get(session, name, domain, objectName, uid, options);
				}
			};
		} else if(key.equals("list")) {
			return new CallableJSWrapper() {
				public Object call(Object... arguments) throws RedbackException {
					String objectName = (String)arguments[0];
					DataMap filter = (DataMap)(arguments[1]);
					DataMap options = arguments.length > 2 ? (DataMap)(arguments[2]) : null;
					return integrationClient.list(session, name, domain, objectName, filter, options);
				}
			};
		} else if(key.equals("update")) {
			return new CallableJSWrapper() {
				public Object call(Object... arguments) throws RedbackException {
					String objectName = (String)arguments[0];
					String uid = (String)arguments[1];
					Object data = arguments[2];
					DataMap options = arguments.length > 3 ? (DataMap)(arguments[3]) : null;
					return integrationClient.update(session, name, domain, objectName, uid, data, options);
				}
			};
		} else if(key.equals("create")) {
			return new CallableJSWrapper() {
				public Object call(Object... arguments) throws RedbackException {
					String objectName = (String)arguments[0];
					Object data = arguments[1];
					DataMap options = arguments.length > 2 ? (DataMap)(arguments[2]) : null;
					return integrationClient.create(session, name, domain, objectName, data, options);
				}
			};
		} else if(key.equals("delete")) {
			return new CallableJSWrapper() {
				public Object call(Object... arguments) throws RedbackException {
					String objectName = (String)arguments[0];
					String uid = (String)arguments[1];
					DataMap options = arguments.length > 2 ? (DataMap)(arguments[2]) : null;
					return integrationClient.delete(session, name, domain, objectName, uid, options);
				}
			};
		} else {
			return new CallableJSWrapper() {
				public Object call(Object... arguments) throws RedbackException {
					Object data = arguments.length > 0 ? arguments[0] : null;
					DataMap options = arguments.length > 1 ? (DataMap)(arguments[1]) : null;
					return integrationClient.execute(session, name, domain, key, data, options);
				}
			};
		} 
	}	
}
