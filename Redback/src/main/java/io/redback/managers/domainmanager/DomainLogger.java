package io.redback.managers.domainmanager;

import io.redback.security.Session;

public class DomainLogger {
	protected StringBuilder sb;
	protected Session session;
	protected DomainFunction function;
	protected DomainManager domainManager;
	protected long start;
	
	public DomainLogger(Session s, DomainManager dm, DomainFunction df) {
		session = s;
		domainManager = dm;
		function = df;
		sb = new StringBuilder();
		start = System.currentTimeMillis();
	}
	
	public void log(String s) {
		sb.append(s);
		sb.append("\r\n");
	}
	
	public String getLog() {
		return sb.toString();
	}
	
	public void commit() {
		log("Execution completed in " + (System.currentTimeMillis() - start) + "ms");
		domainManager.addFunctionLog(session, function, getLog());
	}
}
