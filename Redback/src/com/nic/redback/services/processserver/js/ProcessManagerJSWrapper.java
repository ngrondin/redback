package com.nic.redback.services.processserver.js;

import java.util.ArrayList;

import jdk.nashorn.api.scripting.JSObject;

import com.nic.firebus.utils.FirebusDataUtil;
import com.nic.firebus.utils.JSONObject;
import com.nic.redback.RedbackException;
import com.nic.redback.services.objectserver.js.RedbackJSArray;
import com.nic.redback.services.processserver.ProcessInstance;
import com.nic.redback.services.processserver.ProcessManager;

public class ProcessManagerJSWrapper
{
	protected ProcessManager processManager;
	
	public ProcessManagerJSWrapper(ProcessManager pm)
	{
		processManager = pm;
	}
	
	public ProcessInstance initiateProcess(String name, JSONObject data) throws RedbackException
	{
		return processManager.initiateProcess(processManager.getSystemUserSession(), name, data);
	}

	public JSObject getActions(String pid) throws RedbackException
	{
		return FirebusDataUtil.convertDataListToJSArray(processManager.getActions(processManager.getSystemUserSession(), pid));
	}
	
	public ProcessInstance processAction(String extpid, String pid, String event, JSObject data) throws RedbackException
	{
		return processManager.processAction(processManager.getSystemUserSession(), extpid, pid, event, FirebusDataUtil.convertJSObjectToDataObject(data));
	}
	
	public JSObject findProcesses(JSObject filter) throws RedbackException
	{
		ArrayList<ProcessInstance> list = processManager.findProcesses(processManager.getSystemUserSession(), FirebusDataUtil.convertJSObjectToDataObject(filter));
		RedbackJSArray array = new RedbackJSArray();
		for(int i = 0; i < list.size(); i++)
			array.setSlot(i,  list.get(i));
		return array;
	}
	
}
