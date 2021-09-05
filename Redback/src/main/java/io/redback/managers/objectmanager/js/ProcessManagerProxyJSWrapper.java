package io.redback.managers.objectmanager.js;


import java.util.Arrays;
import java.util.logging.Logger;

import org.graalvm.polyglot.Value;
import org.graalvm.polyglot.proxy.ProxyArray;
import org.graalvm.polyglot.proxy.ProxyExecutable;
import org.graalvm.polyglot.proxy.ProxyObject;

import io.firebus.Firebus;
import io.firebus.Payload;
import io.firebus.data.DataMap;
import io.redback.security.Session;
import io.redback.utils.js.JSConverter;

@Deprecated
public class ProcessManagerProxyJSWrapper implements ProxyObject
{
	private Logger logger = Logger.getLogger("io.redback");
	protected Firebus firebus;
	protected String processServiceName;
	protected Session session;
	protected String[] members = {"initiate", "getAssignment", "action", "actionProcess", "interruptProcess"};
	
	public ProcessManagerProxyJSWrapper(Firebus f, String ps, Session s)
	{
		firebus = f;
		processServiceName = ps;
		session = s;
	}


	public Object getMember(String key) {
		if(key.equals("initiate")) {
			return new ProxyExecutable() {
				public Object execute(Value... arguments) {
					String process = arguments[0].asString();
					RedbackObjectJSWrapper object = arguments[1].asProxyObject();
					DataMap data = (DataMap)JSConverter.toJava(arguments[2]);
					data.put("objectname", object.getMember("objectname"));
					data.put("uid", object.getMember("uid"));
					DataMap request = new DataMap();
					request.put("action", "initiate");
					request.put("process", process);
					request.put("objectname", object.getMember("objectname"));
					request.put("uid", object.getMember("uid"));
					request.put("domain", object.getMember("domain"));
					request.put("data", data);
					try
					{
						Payload requestPayload = new Payload(request.toString());
						requestPayload.metadata.put("session", session.getId());
						requestPayload.metadata.put("token", session.getToken());						
						firebus.requestService(processServiceName, requestPayload);
					}
					catch(Exception e) 
					{
						logger.severe("Error initiating process: " + e);
						throw new RuntimeException("Error initiating process", e);						
					}
					return null;
				}
			};			
		} else if(key.equals("getAssignment")) {
			return new ProxyExecutable() {
				public Object execute(Value... arguments) {
					RedbackObjectJSWrapper object = arguments[0].asProxyObject();
					DataMap request = new DataMap();
					request.put("action", "getassignments");
					DataMap filter = new DataMap();
					filter.put("data.objectname", object.getMember("objectname"));
					filter.put("data.uid", object.getMember("uid"));
					request.put("filter", filter);
					try
					{
						Payload requestPayload = new Payload(request.toString());
						requestPayload.metadata.put("session", session.getId());
						requestPayload.metadata.put("token", session.getToken());
						Payload resp = firebus.requestService(processServiceName, requestPayload);
						DataMap response = new DataMap(resp.getString());
						if(response != null && response.getList("result").size() > 0) 
						{
							return JSConverter.toJS(response.getList("result").getObject(0));	
						}
					}
					catch(Exception e) 
					{
						logger.severe("Error getting process assignment: " + e);
						throw new RuntimeException("Error getting process assignment", e);						
					}
					return null;				
				}
			};				
		} else if(key.equals("action") || key.equals("actionProcess")) {
			return new ProxyExecutable() {
				public Object execute(Value... arguments) {
					String pid = arguments[0].asString();
					String action = arguments[1].asString();
					DataMap request = new DataMap();
					request.put("action", "actionprocess");
					request.put("pid", pid);
					request.put("processaction", action);
					try
					{
						Payload requestPayload = new Payload(request.toString());
						requestPayload.metadata.put("session", session.getId());
						requestPayload.metadata.put("token", session.getToken());
						firebus.requestService(processServiceName, requestPayload);
					}
					catch(Exception e) 
					{
						logger.severe("Error actionning process : " + e);
						throw new RuntimeException("Error actionning process", e);						
					}	
					return null;
				}
			};				
		} else if(key.equals("interruptProcess")) {
			return new ProxyExecutable() {
				public Object execute(Value... arguments) {
					RedbackObjectJSWrapper object = arguments[0].asProxyObject();
					DataMap request = new DataMap();
					request.put("action", "interruptprocesses");
					DataMap filter = new DataMap();
					filter.put("data.objectname", object.getMember("objectname"));
					filter.put("data.uid", object.getMember("uid"));
					request.put("filter", filter);
					try
					{
						Payload requestPayload = new Payload(request.toString());
						requestPayload.metadata.put("session", session.getId());						
						requestPayload.metadata.put("token", session.getToken());
						firebus.requestService(processServiceName, requestPayload);
					}
					catch(Exception e) 
					{
						logger.severe("Error actionning process : " + e);
						throw new RuntimeException("Error actionning process", e);						
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
