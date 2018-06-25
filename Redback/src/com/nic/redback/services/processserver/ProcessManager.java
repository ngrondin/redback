package com.nic.redback.services.processserver;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.logging.Logger;

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
	private Logger logger = Logger.getLogger("com.nic.redback.services.processserver");
	protected Firebus firebus;
	protected String configServiceName;
	protected String accessManagerServiceName;
	protected HashMap<String, HashMap<Integer, Process>> processes;
	protected HashMap<Long, HashMap<String, ProcessInstance>> transactions;
	protected String sysUserName;
	protected String sysUserPassword;
	protected Session sysUserSession;
	protected JSONObject globalVariables;

	public ProcessManager(JSONObject config)
	{
		configServiceName = config.getString("configservice");
		accessManagerServiceName = config.getString("accessmanagementservice");
		sysUserName = config.getString("sysusername");
		sysUserPassword = config.getString("sysuserpassword");
		processes = new HashMap<String, HashMap<Integer, Process>>();
		transactions = new HashMap<Long, HashMap<String, ProcessInstance>>();
		globalVariables = config.getObject("globalvariables");
	}
	
	public void setFirebus(Firebus fb)
	{
		firebus = fb;
	}
	
	public Firebus getFirebus()
	{
		return firebus;
	}
	
	public void refreshAllConfigs()
	{
		processes.clear();
	}
	

	protected void loadProcess(String name) throws RedbackException
	{
		try
		{
			JSONObject configList = request(configServiceName, new JSONObject("{object:rbpm_config,filter:{name:" + name + "}}"));
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
			throw new RedbackException("Exception getting process config", e);
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
				JSONObject piJSON = request(configServiceName, new JSONObject("{object:rbpm_instance,filter:{_id:\"" + pid + "\"}}"));
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
	
	public Session getSystemUserSession(String domain) throws RedbackException 
	{
		if(sysUserSession != null  &&  sysUserSession.expiry < System.currentTimeMillis())
			sysUserSession = null;

		if(sysUserSession == null)
		{
			try
			{
				JSONObject result = request(accessManagerServiceName, new JSONObject("{action:authenticate, username:\"" + sysUserName + "\", password:\"" + sysUserPassword + "\"}"));
				if(result != null  &&  result.getString("result").equals("ok"))
					sysUserSession = new Session(result.getObject("session"));
			}
			catch(Exception e)
			{
				error("Error authenticating sys user", e);
			}
		}
		return sysUserSession;
	}
	
	public JSONObject getGlobalVariables()
	{
		return globalVariables;
	}
	
	public JSONObject initiateProcess(Session session, String processName, JSONObject data) throws RedbackException
	{
		logger.info("Initiating process '" + processName + "'");
		Process process = getProcess(processName);
		ProcessInstance pi = process.createInstance(session, data);
		putInCurrentTransaction(pi);
		JSONObject result = process.startInstance(session, pi);
		result.put("pid", pi.getId());
		logger.info("Initiated instance '" + pi.getId() + "' for process '" + processName + "'");
		return result;
	}

	public ArrayList<JSONObject> getAssignments(Session session, String extpid, JSONObject filter, JSONList viewdata) throws RedbackException
	{
		ArrayList<JSONObject> retList = new ArrayList<JSONObject>();
		JSONObject fullFilter = new JSONObject();
		if(filter != null)
			fullFilter.merge(filter);
		String assigneeId = (extpid != null ? extpid : session.getUserProfile().getUsername());
		JSONList assigneeOrList = new JSONList();
		JSONObject assigneeOrTerm1 = new JSONObject();
		assigneeOrTerm1.put("assignees.id", assigneeId);
		assigneeOrList.add(assigneeOrTerm1);
		JSONObject assigneeOrTerm2 = new JSONObject();
		assigneeOrTerm2.put("lastactioner.id", assigneeId);
		assigneeOrList.add(assigneeOrTerm2);		
		fullFilter.put("$or", assigneeOrList);
		ArrayList<ProcessInstance> instances = findProcesses(session, fullFilter);
		for(int i = 0; i < instances.size(); i++)
		{
			ProcessInstance pi = instances.get(i);
			Process process = getProcess(pi.getProcessName());
			ProcessUnit pu = process.getNode(pi.getCurrentNode());
			JSONObject notification = null;
			if(pu instanceof InteractionUnit)
			{
				notification = ((InteractionUnit)pu).getNotification(session, extpid, pi);
			}
			else
			{
				notification = new JSONObject();
				notification.put("process", pi.getProcessName());
				notification.put("pid", pi.getId());
				notification.put("interaction", "processexception");
				notification.put("message", "The process has stopped due to an exception and requires restart");
				JSONList actionList = new JSONList();
				JSONObject action = new JSONObject();
				action.put("action", "restart");
				action.put("description", "Restart");
				actionList.add(action);
				notification.put("actions", actionList);
			}
			if(notification != null)
			{
				if(viewdata != null  &&  viewdata.size() > 0)
				{
					JSONObject data = new JSONObject();
					for(int j = 0; j < viewdata.size(); j++)
					{
						String key = viewdata.getString(j); 
						data.put(key, pi.getData().getString(key));
					}
					notification.put("data", data);
				}
				retList.add(notification);
			}
		}
		return retList;
	}
	
	public JSONObject processAction(Session session, String extpid, String pid, String event, JSONObject data) throws RedbackException
	{
		ProcessInstance pi = getProcessInstance(pid);
		logger.info("Processing action " + event + " on process " + pi.getProcessName() + ":" + pid);
		Process process = getProcess(pi.getProcessName(), pi.getProcessVersion());
		ProcessUnit pu = process.getNode(pi.getCurrentNode());
		JSONObject result = null;
		if(pu instanceof InteractionUnit)
		{
			result = process.processAction(session, extpid, pi, event, data);
		}
		else
		{
			result = process.continueInstance(pi);
		}
		logger.info("Finished processing action");
		return result;
	}
	
	public ArrayList<ProcessInstance> findProcesses(Session session, JSONObject filter) throws RedbackException
	{
		logger.info("Finding processes for " + filter.toString());
		ArrayList<ProcessInstance> list = new ArrayList<ProcessInstance>();
		try 
		{
			JSONObject result = request(configServiceName, new JSONObject("{object:rbpm_instance,filter:" + filter + "}"));
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
				logger.fine("Found process " + pi.getProcessName() + ":" + pi.getId());
			}
		} 
		catch (Exception e) 
		{
			throw new RedbackException("Error retreiving process instance", e);
		} 	
		logger.info("Finished finding processes");
		return list;
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
	
	public void commitCurrentTransaction() 
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
	
	
	protected JSONObject request(String service, JSONObject request) throws JSONException, FunctionErrorException, FunctionTimeoutException, RedbackException
	{
		if(service != null  &&  request != null)
		{
			Payload reqPayload = new Payload(request.toString());
			logger.info("Requesting firebus service : " + service + "  " + request.toString().replace("\r\n", "").replace("\t", ""));
			Payload respPayload = firebus.requestService(service, reqPayload);
			logger.info("Receiving firebus config service respnse");
			String respStr = respPayload.getString();
			JSONObject result = new JSONObject(respStr);
			return result;
		}
		else
		{
			error("Service Name or Request is null in firebus request");
		}
		return null;
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
