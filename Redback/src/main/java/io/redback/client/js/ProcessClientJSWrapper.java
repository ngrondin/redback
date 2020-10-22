package io.redback.client.js;


import java.util.Arrays;

import org.graalvm.polyglot.Value;
import org.graalvm.polyglot.proxy.ProxyArray;
import org.graalvm.polyglot.proxy.ProxyExecutable;
import org.graalvm.polyglot.proxy.ProxyObject;

import io.firebus.utils.DataMap;
import io.redback.client.ProcessAssignmentRemote;
import io.redback.client.ProcessClient;
import io.redback.security.Session;
import io.redback.utils.js.JSConverter;

public class ProcessClientJSWrapper implements ProxyObject
{
	protected ProcessClient processClient;
	protected Session session;
	protected String domainLock;
	protected String[] members = {"initiate", "getAssignment", "actionProcess", "interruptProcess", "interruptProcesses"};
	
	public ProcessClientJSWrapper(ProcessClient pc, Session s)
	{
		processClient = pc;
		session = s;
	}

	public ProcessClientJSWrapper(ProcessClient pc, Session s, String dl)
	{
		processClient = pc;
		session = s;
		domainLock = dl;
	}

	public Object getMember(String key) {
		if(key.equals("initiate")) {
			return new ProxyExecutable() {
				public Object execute(Value... arguments) {
					String process = arguments[0].asString();
					String domain = arguments[1].asString();
					DataMap data = (DataMap)JSConverter.toJava(arguments[2]);
					if(domainLock != null)
						domain = domainLock;
					try
					{
						processClient.initiate(session, process, domain, data);
					}
					catch(Exception e) 
					{
						throw new RuntimeException("Error initiating process", e);						
					}
					return null;
				}
			};			
		} else if(key.equals("getAssignment")) {
			return new ProxyExecutable() {
				public Object execute(Value... arguments) {
					DataMap filter = (DataMap)JSConverter.toJava(arguments[0]);
					if(domainLock != null)
						filter.put("domain", domainLock);
					try
					{
						ProcessAssignmentRemote par = processClient.getAssignment(session, filter);
						if(par != null) {
							return new ProcessAssignmentRemoteJSWrapper(par);
						} 					}
					catch(Exception e) 
					{
						throw new RuntimeException("Error getting process assignment", e);						
					}
					return null;				
				}
			};				
		} else if(key.equals("actionProcess")) {
			return new ProxyExecutable() {
				public Object execute(Value... arguments) {
					String pid = arguments[0].asString();
					String action = arguments[1].asString();
					try
					{
						processClient.actionProcess(session, pid, action);
					}
					catch(Exception e) 
					{
						throw new RuntimeException("Error actionning process", e);						
					}	
					return null;
				}
			};				
		} else if(key.equals("interruptProcess")) {
			return new ProxyExecutable() {
				public Object execute(Value... arguments) {
					String pid = arguments[0].asString();
					try
					{
						processClient.interruptProcess(session, pid);
					}
					catch(Exception e) 
					{
						throw new RuntimeException("Error interripting process", e);						
					}	
					return null;
				}
			};	
		} else if(key.equals("interruptProcesses")) {
			return new ProxyExecutable() {
				public Object execute(Value... arguments) {
					DataMap filter = (DataMap)JSConverter.toJava(arguments[0]);
					if(domainLock != null)
						filter.put("domain", domainLock);
					try
					{
						processClient.interruptProcesses(session, filter);
					}
					catch(Exception e) 
					{
						throw new RuntimeException("Error interrupting processes", e);						
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
