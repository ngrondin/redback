package com.nic.redback.services.processserver;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Logger;

import com.nic.firebus.Firebus;
import com.nic.firebus.Payload;
import com.nic.firebus.exceptions.FunctionErrorException;
import com.nic.firebus.exceptions.FunctionTimeoutException;
import com.nic.firebus.utils.JSONException;
import com.nic.firebus.utils.JSONList;
import com.nic.firebus.utils.JSONObject;
import com.nic.redback.RedbackException;
import com.nic.redback.security.UserProfile;
import com.nic.redback.services.processserver.units.InteractionUnit;


public class ProcessManager 
{
	private Logger logger = Logger.getLogger("com.nic.redback");
	protected Firebus firebus;
	protected String configServiceName;
	protected HashMap<String, HashMap<Integer, Process>> processes;

	public ProcessManager(JSONObject config)
	{
		configServiceName = config.getString("configservice");
		processes = new HashMap<String, HashMap<Integer, Process>>();
	}
	
	public void setFirebus(Firebus fb)
	{
		firebus = fb;
	}
	
	protected void loadProcess(String name) throws RedbackException
	{
		try
		{
			JSONObject configList = requestConfig(new JSONObject("{object:rbpm_config,filter:{name:" + name + "}}"));
			JSONList list = configList.getList("result");
			if(list.size() > 0)
			{
				HashMap<Integer, Process> versions = new HashMap<Integer, Process>();
				for(int i = 0; i < list.size(); i++)
					versions.put(list.getObject(i).getNumber("version").intValue(), new Process(this, list.getObject(i)));
				processes.put(name, versions);				
			}
		}
		catch(Exception e)
		{
			logger.severe(e.getMessage());
			throw new RedbackException("Exception getting object config", e);
		}
	
	}

	protected Process getProcess(String name) throws RedbackException
	{
		Process process = null;
		if(!processes.containsKey(name))
			loadProcess(name);
		if(processes.containsKey(name))
			process = processes.get(name).get(processes.get(name).size());
		return process;
	}
	
	protected Process getProcess(String name, int v) throws RedbackException
	{
		Process process = null;
		if(!processes.containsKey(name))
			loadProcess(name);
		if(processes.containsKey(name))
			process = processes.get(name).get(v);
		return process;
	}
	
	protected ProcessInstance getProcessInstance(String pid) throws RedbackException
	{
		ProcessInstance pi = null;
		try 
		{
			JSONObject piJSON = requestConfig(new JSONObject("{object:rbpm_instance,filter:{_id:\"" + pid + "\"}}"));
			pi = new ProcessInstance(piJSON.getObject("result.0"));
		} 
		catch (Exception e) 
		{
			throw new RedbackException("Error retreiving process instance", e);
		} 	
		return pi;
	}
	
	public ProcessInstance initiateProcess(UserProfile up, String name, JSONObject data) throws RedbackException
	{
		Process process = getProcess(name);
		ProcessInstance pi = process.createInstance(up, data);
		process.startInstance(up, pi);
		publishInstance(pi);
		return pi;
	}

	public ArrayList<String[]> getActions(UserProfile up, String pid) throws RedbackException
	{
		ProcessInstance pi = getProcessInstance(pid);
		Process process = getProcess(pi.getProcessName());
		ProcessUnit node = process.getNode(pi.getCurrentNode());
		if(node instanceof InteractionUnit)
			return ((InteractionUnit)node).getActions();
		else
			return null;
	}
	
	public ProcessInstance processEvent(UserProfile up, String pid, String event, JSONObject data) throws RedbackException
	{
		ProcessInstance pi = getProcessInstance(pid);
		Process process = getProcess(pi.getProcessName(), pi.getProcessVersion());
		process.processEvent(up, pi, event, data);
		publishInstance(pi);
		return pi;
	}
	
	protected JSONObject requestConfig(JSONObject request) throws JSONException, FunctionErrorException, FunctionTimeoutException
	{
		Payload reqPayload = new Payload(request.toString());
		logger.info("Requesting firebus config service : " + "  " + request.toString().replace("\r\n", "").replace("\t", ""));
		Payload respPayload = firebus.requestService(configServiceName, reqPayload);
		logger.info("Receiving firebus config service respnse");
		String respStr = respPayload.getString();
		JSONObject result = new JSONObject(respStr);
		return result;
	}
	
	protected void publishInstance(ProcessInstance pi)
	{
		logger.info("Publishing to firebus service : " + configServiceName + "  ");
		firebus.publish(configServiceName, new Payload("{object:rbpm_instance,data:" + pi.getJSON() + "}"));
	}

	protected void error(String msg) throws RedbackException
	{
		error(msg, null);
	}
	
	protected void error(String msg, Exception cause) throws RedbackException
	{
		logger.severe(msg);
		if(cause != null)
			throw new RedbackException(msg, cause);
		else
			throw new RedbackException(msg);
	}
}
