package io.redback.client.js;

import java.util.Date;

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
					Date date = arguments.length > 1 ? (Date)arguments[1] : null;
					processAssignmentRemote.action(action, date);
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
