package io.redback.client.js;

import java.util.Base64;

import io.firebus.data.DataMap;
import io.redback.client.ReportClient;
import io.redback.exceptions.RedbackException;
import io.redback.security.Session;
import io.redback.utils.js.CallableJSWrapper;
import io.redback.utils.js.ObjectJSWrapper;

public class ReportClientJSWrapper extends ObjectJSWrapper {
	protected ReportClient reportClient;
	protected Session session;
	protected String domainLock;

	public ReportClientJSWrapper(ReportClient rc, Session s)
	{
		super(new String[] {"produce", "produceAndStore", "clearDomainCache"});
		reportClient = rc;
		session = s;
	}
	
	public ReportClientJSWrapper(ReportClient rc, Session s, String dl)
	{
		super(new String[] {"produce", "produceAndStore", "clearDomainCache"});
		reportClient = rc;
		session = s;
		domainLock = dl;
	}
	
	public Object get(String key) {
		if(key.equals("produce")) {
			return new CallableJSWrapper() {
				public Object call(Object... arguments) throws RedbackException {
					String domain = arguments.length == 3 ? (String)arguments[0] : null; 
					String name = arguments.length == 3 ? (String)arguments[1] : (String)arguments[0];
					DataMap filter = (DataMap)(arguments.length == 3 ? arguments[2] : arguments[1]);
					if(domainLock != null && domain != null)
						domain = domainLock;
					byte[] bytes = reportClient.produce(session, domain, name, filter);
					String base64 = Base64.getEncoder().encodeToString(bytes);
					return base64;
				}
			};
		} else if(key.equals("produceAndStore")) {
			return new CallableJSWrapper() {
				public Object call(Object... arguments) throws RedbackException {
					String domain = arguments.length == 3 ? (String)arguments[0] : null; 
					String name = arguments.length == 3 ? (String)arguments[1] : (String)arguments[0];
					DataMap filter = (DataMap)(arguments.length == 3 ? arguments[2] : arguments[1]);
					if(domainLock != null && domain != null)
						domain = domainLock;
					String fileUid = reportClient.produceAndStore(session, domain, name, filter);
					return fileUid;
				}
			};
		} else if(key.equals("clearDomainCache")) {
			return new CallableJSWrapper() {
				public Object call(Object... arguments) throws RedbackException {
					String domain = arguments.length == 2 ? (String)arguments[0] : null; 
					String name = arguments.length == 2 ? (String)arguments[1] : (String)arguments[0];
					if(domainLock != null && domain != null)
						domain = domainLock;
					reportClient.clearDomainCache(session, domain, name);
					return null;
				}
			};
		} else {
			return null;
		}
	}
}
