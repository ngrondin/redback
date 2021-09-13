package io.redback.managers.domainmanager.js;


import io.firebus.data.DataEntity;
import io.firebus.data.DataLiteral;
import io.firebus.data.DataMap;
import io.redback.exceptions.RedbackException;
import io.redback.managers.domainmanager.DomainManager;
import io.redback.security.Session;
import io.redback.utils.js.CallableJSWrapper;
import io.redback.utils.js.ObjectJSWrapper;

public class DomainManagerJSWrapper extends ObjectJSWrapper {
	
	protected DomainManager domainManager;
	protected Session session;
	protected String domain;

	public DomainManagerJSWrapper(DomainManager dm, Session s, String d)
	{
		super(new String[] {"putVariable", "getVariable", "executeFunction"});
		domainManager = dm;
		session = s;
		domain = d;
	}
	
	public Object get(String key) {
		if(key.equals("putVariable")) {
			return new CallableJSWrapper() {
				public Object call(Object... arguments) throws RedbackException {
					String name = (String)arguments[0];
					Object val = arguments[1];
					if(!(val instanceof DataEntity))
						val = new DataLiteral(val);
					domainManager.putVariable(session, domain, name, (DataEntity)val);
					return null;
				}
			};
		} else if(key.equals("getVariable")) {
			return new CallableJSWrapper() {
				public Object call(Object... arguments) throws RedbackException {
					String name = (String)arguments[0];
					return domainManager.getVariable(session, domain, name);
				}
			};
		} else if(key.equals("executeFunction")) {
			return new CallableJSWrapper() {
				public Object call(Object... arguments) throws RedbackException {
					String name = (String)arguments[0];
					DataMap param = (DataMap)(arguments[1]);
					domainManager.executeFunction(session, domain, name, param, false);
					return null;
				}
			};
		} else {
			return null;
		}
	}
}
