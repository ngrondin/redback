package io.redback.client.js;

import io.redback.client.ProcessAssignmentRemote;
import io.redback.exceptions.RedbackException;
import io.redback.utils.js.CallableJSWrapper;
import io.redback.utils.js.ObjectJSWrapper;

public class ProcessAssignmentRemoteJSWrapper extends ObjectJSWrapper {

	protected ProcessAssignmentRemote processAssignmentRemote;
	
	public ProcessAssignmentRemoteJSWrapper(ProcessAssignmentRemote o)
	{
		super(new String[] {"action", "hasAction"});
		processAssignmentRemote = o;
	}
	
	public Object get(String name) {
		if(name.equals("action"))
		{
			return new CallableJSWrapper() {
				public Object call(Object... arguments) throws RedbackException {
					String action = (String)arguments[0];
					processAssignmentRemote.action(action);
					return null;
				}
			};				
		} else if(name.equals("hasAction")) {
			return new CallableJSWrapper() {
				public Object call(Object... arguments) throws RedbackException {
					String action = (String)arguments[0];
					return processAssignmentRemote.hasAction(action);
				}
			};				
		} else {
			return null;
		}
	}
}
