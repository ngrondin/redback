package io.redback.managers.processmanager.js;

import java.util.ArrayList;
import java.util.List;

import io.firebus.data.DataList;
import io.firebus.data.DataMap;
import io.redback.exceptions.RedbackException;
import io.redback.managers.processmanager.Actionner;
import io.redback.managers.processmanager.Notification;
import io.redback.managers.processmanager.ProcessInstance;
import io.redback.managers.processmanager.ProcessManager;
import io.redback.utils.js.CallableJSWrapper;
import io.redback.utils.js.ObjectJSWrapper;

public class ProcessManagerJSWrapper extends ObjectJSWrapper
{
	protected ProcessManager processManager;
	protected Actionner actionner;
	
	public ProcessManagerJSWrapper(Actionner a, ProcessManager pm)
	{
		super(new String[] {"initiateProcess", "getNotifications", "processAction", "actionProcess", "interruptProcess", "findProcesses"});
		actionner = a;
		processManager = pm;
	}
	
	public Object get(String name) throws RedbackException {
		if(name.equals("initiateProcess"))
		{
			return new CallableJSWrapper() {
				public Object call(Object... arguments) throws RedbackException {
					String name = (String)arguments[0];
					String domain = (String)arguments[1];
					DataMap data = (DataMap)(arguments[2]);
					ProcessInstance pi = processManager.initiateProcess(actionner, name, domain, data);
					return new ProcessInstanceJSWrapper(pi);
				}
			};
		}
		else if(name.equals("getNotifications"))
		{
			return new CallableJSWrapper() {
				public Object call(Object... arguments) throws RedbackException {
					DataMap filter = (DataMap)(arguments[0]);
					DataList viewData = (DataList)arguments[1];
					List<Notification> list = processManager.getNotifications(actionner, filter, viewData);
					return list;
				}
			};
		}
		else if(name.equals("processAction") || name.equals("actionProcess"))
		{
			return new CallableJSWrapper() {
				public Object call(Object... arguments) throws RedbackException {
					String pid = (String)arguments[0];
					String event = (String)arguments[1];
					DataMap data = (DataMap)(arguments[2]);
					processManager.actionProcess(actionner, pid, event, null, data);
					return null;
				}
			};
		}
		else if(name.equals("interruptProcess"))
		{
			return new CallableJSWrapper() {
				public Object call(Object... arguments) throws RedbackException {
					String pid = (String)arguments[0];
					processManager.interruptProcess(actionner, pid);
					return null;
				}
			};
		}
		else if(name.equals("findProcesses"))
		{
			return new CallableJSWrapper() {
				public Object call(Object... arguments) throws RedbackException {
					DataMap filter = (DataMap)(arguments[0]);
					List<ProcessInstance> list = processManager.findProcesses(actionner, filter, 0, 50);
					List<ProcessInstanceJSWrapper> jsList = new ArrayList<ProcessInstanceJSWrapper>();
					for(ProcessInstance pi: list)
						jsList.add(new ProcessInstanceJSWrapper(pi));
					return jsList;
				}
			};
		}
		else
		{
			return null;
		}
	}
}
