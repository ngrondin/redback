package io.redback.client.js;

import java.util.Arrays;
//import java.util.logging.Logger;

import org.graalvm.polyglot.Value;
import org.graalvm.polyglot.proxy.ProxyArray;
import org.graalvm.polyglot.proxy.ProxyExecutable;
import org.graalvm.polyglot.proxy.ProxyObject;

import io.firebus.utils.DataMap;
import io.redback.client.ReportClient;
import io.redback.security.Session;
import io.redback.utils.js.JSConverter;

public class ReportClientJSWrapper implements ProxyObject {
	
	//private Logger logger = Logger.getLogger("io.redback");
	protected ReportClient reportClient;
	protected Session session;
	protected String domainLock;
	protected String[] members = {"produce", "produceAndStore", "clearDomainCache"};

	public ReportClientJSWrapper(ReportClient rc, Session s)
	{
		reportClient = rc;
		session = s;
	}
	
	public ReportClientJSWrapper(ReportClient rc, Session s, String dl)
	{
		reportClient = rc;
		session = s;
		domainLock = dl;
	}
	
	public Object getMember(String key) {
		if(key.equals("produce")) {
			return new ProxyExecutable() {
				public Object execute(Value... arguments) {
					String domain = arguments.length == 3 ? arguments[0].asString() : null; 
					String name = arguments.length == 3 ? arguments[1].asString() : arguments[0].asString();
					DataMap filter = (DataMap)JSConverter.toJava(arguments.length == 3 ? arguments[2] : arguments[1]);
					if(domainLock != null && domain != null)
						domain = domainLock;
					try
					{
						byte[] bytes = reportClient.produce(session, domain, name, filter);
						return bytes;
					}
					catch(Exception e)
					{
						throw new RuntimeException("Error producting report", e);
					}
				}
			};
		} else if(key.equals("produceAndStore")) {
			return new ProxyExecutable() {
				public Object execute(Value... arguments) {
					String domain = arguments.length == 3 ? arguments[0].asString() : null; 
					String name = arguments.length == 3 ? arguments[1].asString() : arguments[0].asString();
					DataMap filter = (DataMap)JSConverter.toJava(arguments.length == 3 ? arguments[2] : arguments[1]);
					if(domainLock != null && domain != null)
						domain = domainLock;
					try
					{
						String fileUid = reportClient.produceAndStore(session, domain, name, filter);
						return JSConverter.toJS(fileUid);
					}
					catch(Exception e)
					{
						throw new RuntimeException("Error producting report", e);
					}
				}
			};
		} else if(key.equals("clearDomainCache")) {
			return new ProxyExecutable() {
				public Object execute(Value... arguments) {
					String domain = arguments.length == 2 ? arguments[0].asString() : null; 
					String name = arguments.length == 2 ? arguments[1].asString() : arguments[0].asString();
					if(domainLock != null && domain != null)
						domain = domainLock;
					try
					{
						reportClient.clearDomainCache(session, domain, name);
						return null;
					}
					catch(Exception e)
					{
						throw new RuntimeException("Error clearing domain cache", e);
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
