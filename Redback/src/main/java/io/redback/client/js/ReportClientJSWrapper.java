package io.redback.client.js;

import java.util.Arrays;
import java.util.logging.Logger;

import org.graalvm.polyglot.Value;
import org.graalvm.polyglot.proxy.ProxyArray;
import org.graalvm.polyglot.proxy.ProxyExecutable;
import org.graalvm.polyglot.proxy.ProxyObject;

import io.firebus.utils.DataMap;
import io.redback.client.ReportClient;
import io.redback.security.Session;
import io.redback.utils.js.JSConverter;

public class ReportClientJSWrapper implements ProxyObject {
	
	private Logger logger = Logger.getLogger("io.redback");
	protected ReportClient reportClient;
	protected Session session;
	protected String[] members = {"produce", "produceAndStore"};

	public ReportClientJSWrapper(ReportClient rc, Session s)
	{
		reportClient = rc;
		session = s;
	}
	
	public Object getMember(String key) {
		if(key.equals("produce")) {
			return new ProxyExecutable() {
				public Object execute(Value... arguments) {
					String name = arguments[0].asString();
					DataMap filter = (DataMap)JSConverter.toJava(arguments[1]);
					try
					{
						byte[] bytes = reportClient.produce(session, name, filter);
						return bytes;
					}
					catch(Exception e)
					{
						logger.severe("Error producing report :" + e);
					}
					return null;
				}
			};
		} else if(key.equals("produceAndStore")) {
			return new ProxyExecutable() {
				public Object execute(Value... arguments) {
					String name = arguments[0].asString();
					DataMap filter = (DataMap)JSConverter.toJava(arguments[1]);
					try
					{
						String fileUid = reportClient.produceAndStore(session, name, filter);
						return JSConverter.toJS(fileUid);
					}
					catch(Exception e)
					{
						logger.severe("Error producing report :" + e);
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