package io.redback.managers.processmanager.js;

import java.util.ArrayList;
import java.util.List;

import jdk.nashorn.api.scripting.JSObject;
import io.firebus.utils.DataMap;
import io.firebus.utils.FirebusDataUtil;
import io.firebus.utils.FirebusJSArray;
import io.redback.RedbackException;
import io.redback.managers.processmanager.Actionner;
import io.redback.managers.processmanager.Assignment;
import io.redback.managers.processmanager.ProcessInstance;
import io.redback.managers.processmanager.ProcessManager;
import io.redback.security.Session;

public class ProcessManagerJSWrapper
{
	protected ProcessManager processManager;
	protected ProcessInstance processInstance;
	protected Actionner actionner;
	//protected Session session;
	
	public ProcessManagerJSWrapper(ProcessManager pm, ProcessInstance pi)
	{
		processManager = pm;
		processInstance = pi;
		actionner = new Actionner(pi);
		//session = s;
	}
	
	public ProcessInstanceJSWrapper initiateProcess(String name, String domain, JSObject data) throws RedbackException
	{
		DataMap dataMap = FirebusDataUtil.convertJSObjectToDataObject(data);
		ProcessInstance pi = processManager.initiateProcess(actionner, name, domain, dataMap);
		return new ProcessInstanceJSWrapper(pi);
	}

	public JSObject getNotifications(JSObject filter, JSObject viewdata) throws RedbackException
	{
		List<Assignment> list = processManager.getAssignments(actionner, FirebusDataUtil.convertJSObjectToDataObject(filter), FirebusDataUtil.convertJSArrayToDataList(viewdata));
		FirebusJSArray array = new FirebusJSArray();
		for(int i = 0; i < list.size(); i++)
			array.setSlot(i,  FirebusDataUtil.convertDataObjectToJSObject(list.get(i).getDataMap()));
		return array;
		
	}
	
	public void processAction(String extpid, String pid, String event, JSObject data) throws RedbackException
	{
		processManager.processAction(actionner, pid, event, FirebusDataUtil.convertJSObjectToDataObject(data));
	}
	
	public JSObject findProcesses(JSObject filter) throws RedbackException
	{
		ArrayList<ProcessInstance> list = processManager.findProcesses(actionner, FirebusDataUtil.convertJSObjectToDataObject(filter));
		FirebusJSArray array = new FirebusJSArray();
		for(int i = 0; i < list.size(); i++)
			array.setSlot(i,  new ProcessInstanceJSWrapper(list.get(i)));
		return array;
	}
	
}
