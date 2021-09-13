package io.redback.client.js;

import io.firebus.data.DataMap;
import io.redback.client.ProcessAssignmentRemote;
import io.redback.client.ProcessClient;
import io.redback.exceptions.RedbackException;
import io.redback.security.Session;
import io.redback.utils.js.CallableJSWrapper;
import io.redback.utils.js.ObjectJSWrapper;

public class ProcessClientJSWrapper extends ObjectJSWrapper {
	protected ProcessClient processClient;
	protected Session session;
	protected String domainLock;
	
	public ProcessClientJSWrapper(ProcessClient pc, Session s)
	{
		super(new String[] {"initiate", "getAssignment", "actionProcess", "continueProcess", "interruptProcess", "interruptProcesses"});
		processClient = pc;
		session = s;
	}

	public ProcessClientJSWrapper(ProcessClient pc, Session s, String dl)
	{
		super(new String[] {"initiate", "getAssignment", "actionProcess", "continueProcess", "interruptProcess", "interruptProcesses"});
		processClient = pc;
		session = s;
		domainLock = dl;
	}

	public Object get(String key) {
		if(key.equals("initiate")) {
			return new CallableJSWrapper() {
				public Object call(Object... arguments) throws RedbackException {
					String process = (String)arguments[0];
					String domain = (String)arguments[1];
					DataMap data = (DataMap)(arguments[2]);
					if(domainLock != null)
						domain = domainLock;
					processClient.initiate(session, process, domain, data);
					return null;
				}
			};	
		} else if(key.equals("continueProcess")) {
			return new CallableJSWrapper() {
				public Object call(Object... arguments) throws RedbackException {
					String pid = (String)arguments[0];
					processClient.continueProcess(session, pid);
					return null;
				}
			};				
		} else if(key.equals("getAssignment")) {
			return new CallableJSWrapper() {
				public Object call(Object... arguments) throws RedbackException {
					DataMap filter = (DataMap)(arguments[0]);
					if(domainLock != null)
						filter.put("domain", domainLock);
					ProcessAssignmentRemote par = processClient.getAssignment(session, filter);
					if(par != null) {
						return new ProcessAssignmentRemoteJSWrapper(par);
					} 					
					return null;				
				}
			};				
		} else if(key.equals("actionProcess")) {
			return new CallableJSWrapper() {
				public Object call(Object... arguments) throws RedbackException {
					String pid = (String)arguments[0];
					String action = (String)arguments[1];
					processClient.actionProcess(session, pid, action);
					return null;
				}
			};				
		} else if(key.equals("interruptProcess")) {
			return new CallableJSWrapper() {
				public Object call(Object... arguments) throws RedbackException {
					String pid = (String)arguments[0];
					processClient.interruptProcess(session, pid);
					return null;
				}
			};	
		} else if(key.equals("interruptProcesses")) {
			return new CallableJSWrapper() {
				public Object call(Object... arguments) throws RedbackException {
					DataMap filter = (DataMap)(arguments[0]);
					if(domainLock != null)
						filter.put("domain", domainLock);
					processClient.interruptProcesses(session, filter);
					return null;
				}
			};				
		} else {
			return null;
		}
	}
}
