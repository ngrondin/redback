package io.redback.client.js;

import java.util.Arrays;
import java.util.logging.Logger;

import org.graalvm.polyglot.Value;
import org.graalvm.polyglot.proxy.ProxyArray;
import org.graalvm.polyglot.proxy.ProxyExecutable;
import org.graalvm.polyglot.proxy.ProxyObject;

import io.firebus.utils.DataEntity;
import io.firebus.utils.DataMap;
import io.redback.client.DomainClient;
import io.redback.security.Session;
import io.redback.utils.js.JSConverter;

public class DomainClientJSWrapper implements ProxyObject {
	
	private Logger logger = Logger.getLogger("io.redback");
	protected DomainClient domainClient;
	protected Session session;
	protected String domain;
	protected String[] members = {"putVariable", "getVariable", "executeFunction", "clearCache"};

	public DomainClientJSWrapper(DomainClient dc, Session s, String d)
	{
		domainClient = dc;
		session = s;
		domain = d;
	}
	
	public Object getMember(String key) {
		if(key.equals("putVariable")) {
			return new ProxyExecutable() {
				public Object execute(Value... arguments) {
					String name = arguments[0].asString();
					String category = arguments[1].asString();
					DataEntity var = (DataMap)JSConverter.toJava(arguments[2]);
					try
					{
						domainClient.putVariable(session, domain, name, category, var);
					}
					catch(Exception e)
					{
						logger.severe("Error putting variable :" + e);
					}
					return null;
				}
			};
		} else if(key.equals("getVariable")) {
			return new ProxyExecutable() {
				public Object execute(Value... arguments) {
					String name = arguments[0].asString();
					try
					{
						Object o = domainClient.getVariable(session, domain, name);
						return JSConverter.toJS(o);
					}
					catch(Exception e)
					{
						logger.severe("Error getting domain variable :" + e);
					}
					return null;
				}
			};
		} else if(key.equals("executeFunction")) {
			return new ProxyExecutable() {
				public Object execute(Value... arguments) {
					String name = arguments[0].asString();
					DataMap param = (DataMap)JSConverter.toJava(arguments[1]);
					try
					{
						domainClient.executeFunction(session, domain, name, param);
					}
					catch(Exception e)
					{
						logger.severe("Error executing domain function :" + e);
					}
					return null;
				}
			};
		} else if(key.equals("clearCache")) {
			return new ProxyExecutable() {
				public Object execute(Value... arguments) {
					String name = arguments[0].asString();
					try
					{
						domainClient.clearCache(session, domain, name);
					}
					catch(Exception e)
					{
						logger.severe("Error clearing domain cache :" + e);
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
