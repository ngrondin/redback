package io.redback.client.js;

import java.util.Arrays;

import org.graalvm.polyglot.Value;
import org.graalvm.polyglot.proxy.ProxyArray;
import org.graalvm.polyglot.proxy.ProxyExecutable;
import org.graalvm.polyglot.proxy.ProxyObject;

import io.firebus.utils.DataMap;
import io.redback.client.IntegrationClient;
import io.redback.security.Session;
import io.redback.utils.js.JSConverter;

public class IntegrationClientJSWrapper implements ProxyObject {
	
	protected IntegrationClient integrationClient;
	protected String[] members = {"get", "list", "update", "create", "delete"};
	protected String domain;
	protected Session session;

	public IntegrationClientJSWrapper(IntegrationClient ic, Session s, String d)
	{
		integrationClient = ic;
		domain = d;
		session = s;
	}
	
	public Object getMember(String key) {
		if(key.equals("get")) {
			return new ProxyExecutable() {
				public Object execute(Value... arguments) {
					String client = arguments[0].asString();
					String objectName = arguments[1].asString();
					String uid = arguments[2].asString();
					DataMap options = arguments.length > 3 ? (DataMap)JSConverter.toJava(arguments[3]) : null;
					try
					{
						DataMap ret = integrationClient.get(session, client, domain, objectName, uid, options);
						return JSConverter.toJS(ret);
					}
					catch(Exception e)
					{
						throw new RuntimeException("Error on integration get", e);
					}
				}
			};
		} else if(key.equals("list")) {
			return new ProxyExecutable() {
				public Object execute(Value... arguments) {
					String client = arguments[0].asString();
					String objectName = arguments[1].asString();
					DataMap filter = (DataMap)JSConverter.toJava(arguments[2]);
					DataMap options = arguments.length > 3 ? (DataMap)JSConverter.toJava(arguments[3]) : null;
					try
					{
						DataMap ret = integrationClient.list(session, client, domain, objectName, filter, options);
						return JSConverter.toJS(ret);
					}
					catch(Exception e)
					{
						throw new RuntimeException("Error on integration list", e);
					}
				}
			};
		} else if(key.equals("update")) {
			return new ProxyExecutable() {
				public Object execute(Value... arguments) {
					String client = arguments[0].asString();
					String objectName = arguments[1].asString();
					String uid = arguments[2].asString();
					DataMap data = (DataMap)JSConverter.toJava(arguments[3]);
					DataMap options = arguments.length > 4 ? (DataMap)JSConverter.toJava(arguments[4]) : null;
					try
					{
						DataMap ret = integrationClient.update(session, client, domain, objectName, uid, data, options);
						return JSConverter.toJS(ret);
					}
					catch(Exception e)
					{
						throw new RuntimeException("Error on integration update", e);
					}
				}
			};
		} else if(key.equals("create")) {
			return new ProxyExecutable() {
				public Object execute(Value... arguments) {
					String client = arguments[0].asString();
					String objectName = arguments[1].asString();
					DataMap data = (DataMap)JSConverter.toJava(arguments[2]);
					DataMap options = arguments.length > 3 ? (DataMap)JSConverter.toJava(arguments[3]) : null;
					try
					{
						DataMap ret = integrationClient.create(session, client, domain, objectName, data, options);
						return JSConverter.toJS(ret);
					}
					catch(Exception e)
					{
						throw new RuntimeException("Error on integration create", e);
					}
				}
			};
		} else if(key.equals("delete")) {
			return new ProxyExecutable() {
				public Object execute(Value... arguments) {
					String client = arguments[0].asString();
					String objectName = arguments[1].asString();
					String uid = arguments[2].asString();
					DataMap options = arguments.length > 3 ? (DataMap)JSConverter.toJava(arguments[3]) : null;
					try
					{
						DataMap ret = integrationClient.delete(session, client, domain, objectName, uid, options);
						return JSConverter.toJS(ret);
					}
					catch(Exception e)
					{
						throw new RuntimeException("Error on integration delete", e);
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
