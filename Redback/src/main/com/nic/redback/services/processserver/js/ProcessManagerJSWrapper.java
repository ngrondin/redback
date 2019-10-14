package com.nic.redback.services.processserver.js;

import java.util.ArrayList;

import jdk.nashorn.api.scripting.JSObject;

import com.nic.firebus.utils.FirebusDataUtil;
import com.nic.firebus.utils.FirebusJSArray;
import com.nic.firebus.utils.JSONObject;
import com.nic.redback.RedbackException;
import com.nic.redback.security.Session;
import com.nic.redback.services.processserver.ProcessInstance;
import com.nic.redback.services.processserver.ProcessManager;

public class ProcessManagerJSWrapper
{
	protected ProcessManager processManager;
	protected Session session;
	
	public ProcessManagerJSWrapper(ProcessManager pm, Session s)
	{
		processManager = pm;
		session = s;
	}
	
	public JSObject initiateProcess(String name, String domain, JSONObject data) throws RedbackException
	{
		return FirebusDataUtil.convertDataObjectToJSObject(processManager.initiateProcess(session, name, data));
	}

	public JSObject getNotifications(String extpid, JSObject filter, JSObject viewdata) throws RedbackException
	{
		ArrayList<JSONObject> list = processManager.getAssignments(session, extpid, FirebusDataUtil.convertJSObjectToDataObject(filter), FirebusDataUtil.convertJSArrayToDataList(viewdata));
		FirebusJSArray array = new FirebusJSArray();
		for(int i = 0; i < list.size(); i++)
			array.setSlot(i,  FirebusDataUtil.convertDataObjectToJSObject(list.get(i)));
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
