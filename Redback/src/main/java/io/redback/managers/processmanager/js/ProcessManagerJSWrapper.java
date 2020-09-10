package io.redback.managers.processmanager.js;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.graalvm.polyglot.Value;
import org.graalvm.polyglot.proxy.ProxyArray;
import org.graalvm.polyglot.proxy.ProxyExecutable;
import org.graalvm.polyglot.proxy.ProxyObject;

import io.firebus.utils.DataList;
import io.firebus.utils.DataMap;
import io.redback.managers.processmanager.Actionner;
import io.redback.managers.processmanager.Assignment;
import io.redback.managers.processmanager.ProcessInstance;
import io.redback.managers.processmanager.ProcessManager;
import io.redback.utils.js.JSConverter;

public class ProcessManagerJSWrapper implements ProxyObject
{
	protected ProcessManager processManager;
	protected ProcessInstance processInstance;
	protected Actionner actionner;
	protected String[] members = {"initiateProcess", "getNotifications", "processAction", "findProcesses"};
	
	public ProcessManagerJSWrapper(ProcessManager pm, ProcessInstance pi)
	{
		processManager = pm;
		processInstance = pi;
		actionner = new Actionner(pi);
	}
	
	public Object getMember(String name)
	{
		if(name.equals("initiateProcess"))
		{
			return new ProxyExecutable() {
				public Object execute(Value... arguments) {
					try {
						String name = arguments[0].asString();
						String domain = arguments[1].asString();
						DataMap data = (DataMap)JSConverter.toJava(arguments[2]);
						ProcessInstance pi = processManager.initiateProcess(actionner, name, domain, data);
						return new ProcessInstanceJSWrapper(pi);
					} catch (Exception e) {
						throw new RuntimeException("Errror in initiateProcess", e);
					}
				}
			};
		}
		else if(name.equals("getNotifications"))
		{
			return new ProxyExecutable() {
				public Object execute(Value... arguments) {
					try {
						DataMap filter = (DataMap)JSConverter.toJava(arguments[0]);
						DataList viewData = (DataList)JSConverter.toJava(arguments[1]);
						List<Assignment> list = processManager.getAssignments(actionner, filter, viewData);
						return JSConverter.toJS(list);
					} catch (Exception e) {
						throw new RuntimeException("Errror in getNotifications", e);
					}
				}
			};
		}
		else if(name.equals("processAction"))
		{
			return new ProxyExecutable() {
				public Object execute(Value... arguments) {
					try {
						String pid = arguments[0].asString();
						String event = arguments[1].asString();
						DataMap data = (DataMap)JSConverter.toJava(arguments[2]);
						processManager.processAction(actionner, pid, event, data);
						return null;
					} catch (Exception e) {
						throw new RuntimeException("Errror in processAction", e);
					}
				}
			};
		}
		else if(name.equals("findProcesses"))
		{
			return new ProxyExecutable() {
				public Object execute(Value... arguments) {
					try {
						DataMap filter = (DataMap)JSConverter.toJava(arguments[0]);
						ArrayList<ProcessInstance> list = processManager.findProcesses(actionner, filter);
						return JSConverter.toJS(list);
					} catch (Exception e) {
						throw new RuntimeException("Errror in findProcesses", e);
					}
				}
			};
		}
		else
		{
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
