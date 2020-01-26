package io.redback.managers.processmanager.js;

import java.util.ArrayList;

import jdk.nashorn.api.scripting.JSObject;
import io.firebus.utils.DataMap;
import io.firebus.utils.FirebusDataUtil;
import io.firebus.utils.FirebusJSArray;
import io.redback.RedbackException;
import io.redback.managers.processmanager.Assignment;
import io.redback.managers.processmanager.ProcessInstance;
import io.redback.managers.processmanager.ProcessManager;
import io.redback.security.Session;

public class ProcessManagerJSWrapper
{
	protected ProcessManager processManager;
	protected Session session;
	
	public ProcessManagerJSWrapper(ProcessManager pm, Session s)
	{
		processManager = pm;
		session = s;
	}
	
	public JSObject initiateProcess(String name, String domain, DataMap data) throws RedbackException
	{
		return FirebusDataUtil.convertDataObjectToJSObject(processManager.initiateProcess(session, name, data));
	}

	public JSObject getNotifications(String extpid, JSObject filter, JSObject viewdata) throws RedbackException
	{
		ArrayList<Assignment> list = processManager.getAssignments(session, extpid, FirebusDataUtil.convertJSObjectToDataObject(filter), FirebusDataUtil.convertJSArrayToDataList(viewdata));
		FirebusJSArray array = new FirebusJSArray();
		for(int i = 0; i < list.size(); i++)
			array.setSlot(i,  FirebusDataUtil.convertDataObjectToJSObject(list.get(i).getDataMap()));
		return array;
		
	}
	
	public JSObject processAction(String extpid, String pid, String event, JSObject data) throws RedbackException
	{
		return FirebusDataUtil.convertDataObjectToJSObject(processManager.processAction(session, extpid, pid, event, FirebusDataUtil.convertJSObjectToDataObject(data)));
	}
	
	public JSObject findProcesses(JSObject filter) throws RedbackException
	{
		ArrayList<ProcessInstance> list = processManager.findProcesses(session, FirebusDataUtil.convertJSObjectToDataObject(filter));
		FirebusJSArray array = new FirebusJSArray();
		for(int i = 0; i < list.size(); i++)
			array.setSlot(i,  new ProcessInstanceJSWrapper(list.get(i)));
		return array;
	}
	
}
