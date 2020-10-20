package io.redback.client.js;

import java.util.Arrays;

import org.graalvm.polyglot.Value;
import org.graalvm.polyglot.proxy.ProxyArray;
import org.graalvm.polyglot.proxy.ProxyExecutable;
import org.graalvm.polyglot.proxy.ProxyObject;

import io.redback.client.ProcessAssignmentRemote;

public class ProcessAssignmentRemoteJSWrapper implements ProxyObject {

	protected ProcessAssignmentRemote processAssignmentRemote;
	protected String[] members = {"action"};
	
	public ProcessAssignmentRemoteJSWrapper(ProcessAssignmentRemote o)
	{
		processAssignmentRemote = o;
	}
	
	public Object getMember(String name)
	{
		if(name.equals("action"))
		{
			return new ProxyExecutable() {
				public Object execute(Value... arguments) {
					String action = arguments[0].asString();
					processAssignmentRemote.action(action);
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
