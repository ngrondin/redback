package com.nic.redback.managers.processmanager;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.logging.Logger;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.nic.firebus.Firebus;
import com.nic.firebus.Payload;
import com.nic.firebus.exceptions.FunctionErrorException;
import com.nic.firebus.exceptions.FunctionTimeoutException;
import com.nic.firebus.utils.DataException;
import com.nic.firebus.utils.DataList;
import com.nic.firebus.utils.DataMap;
import com.nic.redback.RedbackException;
import com.nic.redback.managers.processmanager.units.InteractionUnit;
import com.nic.redback.security.Session;


public class ProcessManager
{
	private Logger logger = Logger.getLogger("com.nic.redback.managers.processmanager");
	protected Firebus firebus;
	protected String configServiceName;
	protected String dataServiceName;
	protected String accessManagerServiceName;
	protected HashMap<String, HashMap<Integer, Process>> processes;
	protected HashMap<Long, HashMap<String, ProcessInstance>> transactions;
	protected String sysUserName;
	protected String sysUserPassword;
	protected Session sysUserSession;
	protected DataMap globalVariables;

	public ProcessManager(Firebus fb, DataMap config)
	{
		firebus = fb;
		configServiceName = config.getString("configservice");
		dataServiceName = config.getString("dataservice");
		accessManagerServiceName = config.getString("accessmanagementservice");
		sysUserName = config.getString("sysusername");
		sysUserPassword = config.getString("sysuserpassword");
		processes = new HashMap<String, HashMap<Integer, Process>>();
		transactions = new HashMap<Long, HashMap<String, ProcessInstance>>();
		globalVariables = config.getObject("globalvariables");
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
			DataMap configList = request(configServiceName, new DataMap("{action:list, service:rbpm, category:process, filter:{name:" + name + "}}"));
			DataList list = configList.getList("result");
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
		{
			int max = -1;
			Iterator<Integer> it = processes.get(name).keySet().iterator();
			while(it.hasNext())
			{
				int n = it.next();
				if(n > max)
					max = n;
			}
			process = processes.get(name).get(max);
		}
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
				DataMap piJSON = request(dataServiceName, new DataMap("{object:rbpm_instance,filter:{_id:\"" + pid + "\"}}"));
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
				Algorithm algorithm = Algorithm.HMAC256("secret");
				String token = JWT.create()
						.withIssuer("rbpm")
						.withSubject("processuser")
						.withClaim("email", "processuser")
						.withExpiresAt(new Date(System.currentTimeMillis() + 3600000))
						.sign(algorithm);
				DataMap result = request(accessManagerServiceName, new DataMap("{action:validate, token:\"" + token + "\"}"));
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
	
	public DataMap getGlobalVariables()
	{
		return globalVariables;
	}
	
	public DataMap initiateProcess(Session session, String processName, DataMap data) throws RedbackException
	{
		logger.finer("Initiating process '" + processName + "'");
		Process process = getProcess(processName);
		ProcessInstance pi = process.createInstance(session, data);
		putInCurrentTransaction(pi);
		DataMap result = process.startInstance(session, pi);
		result.put("pid", pi.getId());
		logger.finer("Initiated instance '" + pi.getId() + "' for process '" + processName + "'");
		return result;
	}

	public ArrayList<DataMap> getAssignments(Session session, String extpid, DataMap filter, DataList viewdata) throws RedbackException
	{
		ArrayList<DataMap> retList = new ArrayList<DataMap>();
		DataMap fullFilter = new DataMap();
		if(filter != null)
			fullFilter.merge(filter);
		String assigneeId = (extpid != null ? extpid : session.getUserProfile().getUsername());
		DataList assigneeOrList = new DataList();
		DataMap assigneeOrTerm1 = new DataMap();
		assigneeOrTerm1.put("assignees.id", assigneeId);
		assigneeOrList.add(assigneeOrTerm1);
		DataMap assigneeOrTerm2 = new DataMap();
		assigneeOrTerm2.put("lastactioner.id", assigneeId);
		assigneeOrList.add(assigneeOrTerm2);		
		fullFilter.put("$or", assigneeOrList);
		ArrayList<ProcessInstance> instances = findProcesses(session, fullFilter);
		for(int i = 0; i < instances.size(); i++)
		{
			ProcessInstance pi = instances.get(i);
			Process process = getProcess(pi.getProcessName());
			ProcessUnit pu = process.getNode(pi.getCurrentNode());
			DataMap notification = null;
			if(pu instanceof InteractionUnit)
			{
				notification = ((InteractionUnit)pu).getNotification(session, extpid, pi);
			}
			else
			{
				notification = new DataMap();
				notification.put("process", pi.getProcessName());
				notification.put("pid", pi.getId());
				notification.put("interaction", "processexception");
				notification.put("message", "The process has stopped due to an exception and requires restart");
				DataList actionList = new DataList();
				DataMap action = new DataMap();
				action.put("action", "restart");
				action.put("description", "Restart");
				actionList.add(action);
				notification.put("actions", actionList);
			}
			if(notification != null)
			{
				if(viewdata != null  &&  viewdata.size() > 0)
				{
					DataMap data = new DataMap();
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
	
	public DataMap processAction(Session session, String extpid, String pid, String event, DataMap data) throws RedbackException
	{
		ProcessInstance pi = getProcessInstance(pid);
		logger.finer("Processing action " + event + " on process " + pi.getProcessName() + ":" + pid);
		Process process = getProcess(pi.getProcessName(), pi.getProcessVersion());
		ProcessUnit pu = process.getNode(pi.getCurrentNode());
		DataMap result = null;
		if(pu instanceof InteractionUnit)
		{
			result = process.processAction(session, extpid, pi, event, data);
		}
		else
		{
			result = process.continueInstance(pi);
		}
		logger.finer("Finished processing action");
		return result;
	}
	
	public ArrayList<ProcessInstance> findProcesses(Session session, DataMap filter) throws RedbackException
	{
		logger.finer("Finding processes for " + filter.toString());
		ArrayList<ProcessInstance> list = new ArrayList<ProcessInstance>();
		try 
		{
			DataMap result = request(dataServiceName, new DataMap("{object:rbpm_instance,filter:" + filter + "}"));
			DataList resultList = result.getList("result");
			for(int i = 0; i < resultList.size(); i++)
			{
				ProcessInstance pi = getFromCurrentTransaction(resultList.getObject(i).getString("_id"));
				if(pi == null)
				{
					pi = new ProcessInstance(resultList.getObject(i));
					putInCurrentTransaction(pi);
				}
				list.add(pi);
				logger.finer("Found process " + pi.getProcessName() + ":" + pi.getId());
			}
		} 
		catch (Exception e) 
		{
			throw new RedbackException("Error retreiving process instance", e);
		} 	
		logger.finer("Finished finding processes");
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
	
	
	protected DataMap request(String service, DataMap request) throws DataException, FunctionErrorException, FunctionTimeoutException, RedbackException
	{
		if(service != null  &&  request != null)
		{
			Payload reqPayload = new Payload(request.toString());
			logger.finest("Requesting firebus service : " + service + "  " + request.toString().replace("\r\n", "").replace("\t", ""));
			Payload respPayload = firebus.requestService(service, reqPayload);
			logger.finest("Receiving firebus config service respnse");
			String respStr = respPayload.getString();
			DataMap result = new DataMap(respStr);
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
		logger.finest("Publishing to firebus service : " + dataServiceName + "  ");
		firebus.publish(dataServiceName, new Payload("{object:rbpm_instance, key: {_id:" + pi.getId() + "}, data:" + pi.getJSON() + ", operation:replace}"));
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
