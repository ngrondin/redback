package io.redback.client.js;

import io.firebus.data.DataEntity;
import io.firebus.data.DataMap;
import io.redback.client.DomainClient;
import io.redback.exceptions.RedbackException;
import io.redback.security.Session;
import io.redback.utils.js.CallableJSWrapper;
import io.redback.utils.js.ObjectJSWrapper;

public class DomainClientJSWrapper extends ObjectJSWrapper {
	
	protected DomainClient domainClient;
	protected Session session;
	protected String domain;

	public DomainClientJSWrapper(DomainClient dc, Session s, String d)
	{
		super(new String[] {"putVariable", "getVariable", "executeFunction", "clearCache"});
		domainClient = dc;
		session = s;
		domain = d;
	}
	
	public Object get(String key) {
		if(key.equals("putVariable")) {
			return new CallableJSWrapper() {
				public Object call(Object... arguments) throws RedbackException {
					String name = (String)arguments[0];
					DataEntity var = (DataEntity)arguments[1];
					domainClient.putVariable(session, domain, name, var);
					return null;
				}
			};
		} else if(key.equals("getVariable")) {
			return new CallableJSWrapper() {
				public Object call(Object... arguments) throws RedbackException {
					String name = (String)arguments[0];
					return domainClient.getVariable(session, domain, name);
				}
			};
		} else if(key.equals("executeFunction")) {
			return new CallableJSWrapper() {
				public Object call(Object... arguments) throws RedbackException {
					String name = (String)arguments[0];
					DataMap param = (DataMap)arguments[1];
					boolean async = arguments.length > 2 && arguments[2] instanceof Boolean ? (Boolean)arguments[2] : false;
					return domainClient.executeFunction(session, domain, name, param, async);
				}
			};
		} else if(key.equals("clearCache")) {
			return new CallableJSWrapper() {
				public Object call(Object... arguments) throws RedbackException {
					String name = (String)arguments[0];
					domainClient.clearCache(session, domain, name);
					return null;
				}
			};		
		} else {
			return null;
		}
	}
}
