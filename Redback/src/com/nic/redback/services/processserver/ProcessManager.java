package com.nic.redback.services.processserver;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.logging.Logger;

import javax.script.ScriptException;

import com.nic.firebus.Firebus;
import com.nic.firebus.Payload;
import com.nic.firebus.exceptions.FunctionErrorException;
import com.nic.firebus.exceptions.FunctionTimeoutException;
import com.nic.firebus.utils.JSONException;
import com.nic.firebus.utils.JSONList;
import com.nic.firebus.utils.JSONObject;
import com.nic.redback.RedbackException;
import com.nic.redback.security.Session;
import com.nic.redback.services.processserver.units.InteractionUnit;


public class ProcessManager 
{
	private Logger logger = Logger.getLogger("com.nic.redback");
	protected Firebus firebus;
	protected String configServiceName;
	protected HashMap<String, HashMap<Integer, Process>> processes;
	protected HashMap<Long, HashMap<String, ProcessInstance>> transactions;
	protected String sysUserName;
	protected String sysUserPassword;
	protected JSONObject globalData;

	public ProcessManager(JSONObject config)
	{
		configServiceName = config.getString("configservice");
		sysUserName = config.getString("sysusername");
		sysUserPassword = config.getString("sysuserpassword");
		processes = new HashMap<String, HashMap<Integer, Process>>();
		transactions = new HashMap<Long, HashMap<String, ProcessInstance>>();
		globalData = config.getObject("globaldata");
	}
	
	public void setFirebus(Firebus fb)
	{
		firebus = fb;
	}
	
	public Firebus getFirebus()
	{
		return firebus;
	}
	
	protected void loadProcess(String name) throws RedbackException
	{
		try
		{
			JSONObject configList = request(new JSONObject("{object:rbpm_config,filter:{name:" + name + "}}"));
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
		ProcessInstance pi = getFromCurrentTransaction(pid);
		if(pi == null)
		{
			try 
			{
				JSONObject piJSON = request(new JSONObject("{object:rbpm_instance,filter:{_id:\"" + pid + "\"}}"));
				pi = new ProcessInstance(piJSON.getObject("result.0"));
				putInCurrentTransaction(pi);
			} 
			catch (Exception e) 
			{
				throw new RedbackException("Error retreiving process instance", e);
			} 	
		}
		return pi;
	}
	
	public Session getSystemUserSession()
	{
		JSONObject sessionObject = new JSONObject();
		sessionObject.put("userprofile", new JSONObject());
		sessionObject.getObject("userprofile").put("username", sysUserName);
		sessionObject.getObject("userprofile").put("attributes", new JSONObject());
		sessionObject.getObject("userprofile.attributes").put("rb", new JSONObject());
		sessionObject.getObject("userprofile.attributes.rb").put("process", new JSONObject());
		sessionObject.getObject("userprofile.attributes.rb.process").put("sysuser", true);
		return new Session(sessionObject);
	}
	
	public JSONObject getGlobalData()
	{
		return globalData;
	}
	
	public ProcessInstance initiateProcess(Session session, String name, JSONObject data) throws RedbackException
	{
		Process process = getProcess(name);
		ProcessInstance pi = process.createInstance(session, data);
		putInCurrentTransaction(pi);
		process.startInstance(session, pi);
		return pi;
	}

	public JSONList getActions(Session session, String pid) throws RedbackException
	{
		ProcessInstance pi = getProcessInstance(pid);
		Process process = getProcess(pi.getProcessName());
		ProcessUnit node = process.getNode(pi.getCurrentNode());
		if(node instanceof InteractionUnit)
			return ((InteractionUnit)node).getActions(session, pid, pi);
		else
			return null;
	}
	
	public ProcessInstance processAction(Session session, String extpid, String pid, String event, JSONObject data) throws RedbackException
	{
		ProcessInstance pi = getProcessInstance(pid);
		Process process = getProcess(pi.getProcessName(), pi.getProcessVersion());
		process.processAction(session, extpid, pi, event, data);
		return pi;
	}
	
	public ArrayList<ProcessInstance> findProcesses(Session session, JSONObject filter) throws RedbackException
	{
		ArrayList<ProcessInstance> list = new ArrayList<ProcessInstance>();
		try 
		{
			JSONObject result = request(new JSONObject("{object:rbpm_instance,filter:" + filter + "}"));
			JSONList resultList = result.getList("result");
			for(int i = 0; i < resultList.size(); i++)
			{
				ProcessInstance pi = getFromCurrentTransaction(resultList.getObject(i).getString("_id"));
				if(pi == null)
				{
					pi = new ProcessInstance(resultList.getObject(i));
					putInCurrentTransaction(pi);
				}
				list.add(pi);
			}
		} 
		catch (Exception e) 
		{
			throw new RedbackException("Error retreiving process instance", e);
		} 	
		return list;
	}
	
	public void notifyProcess(Session session, String extpid, String pid, JSONObject notification) throws RedbackException
	{
		ProcessInstance pi = getProcessInstance(pid);
		notification.put("user", session.getUserProfile().getUsername());
		if(extpid != null)
			notification.put("extpid", extpid);
		pi.addNotification(notification);
	}
	
	
	protected ProcessInstance getFromCurrentTransaction(String pid)
	{
		long txId = Thread.currentThread().getId();
		if(transactions.containsKey(txId))
			return transactions.get(txId).get(pid);
		else
			return null;
	}

	protected void putInCurrentTransaction(ProcessInstance pi)
	{
		long txId = Thread.currentThread().getId();
		synchronized(transactions)
		{
			if(!transactions.containsKey(txId))
				transactions.put(txId, new HashMap<String, ProcessInstance>());
		}
		transactions.get(txId).put(pi.getId(), pi);
	}
	
	public void commitCurrentTransaction() throws ScriptException, RedbackException
	{
		long txId = Thread.currentThread().getId();
		if(transactions.containsKey(txId))
		{
			HashMap<String, ProcessInstance> objects = transactions.get(txId);
			Iterator<String> it = objects.keySet().iterator();
			while(it.hasNext())
			{
				String key = it.next();
				ProcessInstance pi = objects.get(key);
				publishInstance(pi);
			}
			synchronized(transactions)
			{
				transactions.remove(txId);
			}
		}		
	}
	
	
	protected JSONObject request(JSONObject request) throws JSONException, FunctionErrorException, FunctionTimeoutException
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
		firebus.publish(configServiceName, new Payload("{object:rbpm_instance,data:" + pi.getJSON() + ", operation:replace}"));
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
