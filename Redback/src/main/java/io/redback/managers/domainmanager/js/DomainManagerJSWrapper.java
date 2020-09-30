package io.redback.managers.domainmanager.js;

import java.util.Arrays;
import java.util.logging.Logger;

import org.graalvm.polyglot.Value;
import org.graalvm.polyglot.proxy.ProxyArray;
import org.graalvm.polyglot.proxy.ProxyExecutable;
import org.graalvm.polyglot.proxy.ProxyObject;

import io.firebus.utils.DataEntity;
import io.firebus.utils.DataLiteral;
import io.firebus.utils.DataMap;
import io.redback.managers.domainmanager.DomainManager;
import io.redback.security.Session;
import io.redback.utils.js.JSConverter;

public class DomainManagerJSWrapper implements ProxyObject {
	
	private Logger logger = Logger.getLogger("io.redback");
	protected DomainManager domainManager;
	protected Session session;
	protected String domain;
	protected String[] members = {"putVariable", "getVariable", "executeFunction"};

	public DomainManagerJSWrapper(DomainManager dm, Session s, String d)
	{
		domainManager = dm;
		session = s;
		domain = d;
	}
	
	public Object getMember(String key) {
		if(key.equals("putVariable")) {
			return new ProxyExecutable() {
				public Object execute(Value... arguments) {
					String name = arguments[0].asString();
					Object val = JSConverter.toJava(arguments[1]);
					if(!(val instanceof DataEntity))
						val = new DataLiteral(val);
					try
					{
						domainManager.putVariable(session, domain, name, (DataEntity)val);
					}
					catch(Exception e)
					{
						logger.severe("Error putting report :" + e);
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
						Object o = domainManager.getVariable(session, domain, name);
						return JSConverter.toJS(o);
					}
					catch(Exception e)
					{
						logger.severe("Error putting report :" + e);
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
						domainManager.executeFunction(session, domain, name, param);
					}
					catch(Exception e)
					{
						logger.severe("Error putting report :" + e);
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
