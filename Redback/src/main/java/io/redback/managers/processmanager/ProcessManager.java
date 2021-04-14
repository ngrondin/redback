package io.redback.managers.processmanager;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;

import io.firebus.Firebus;
import io.firebus.Payload;
import io.firebus.exceptions.FunctionErrorException;
import io.firebus.exceptions.FunctionTimeoutException;
import io.firebus.utils.DataException;
import io.firebus.utils.DataFilter;
import io.firebus.utils.DataList;
import io.firebus.utils.DataMap;
import io.redback.RedbackException;
import io.redback.client.AccessManagementClient;
import io.redback.client.ObjectClient;
import io.redback.managers.jsmanager.JSManager;
import io.redback.managers.processmanager.units.InteractionUnit;
import io.redback.security.Session;
import io.redback.security.UserProfile;
import io.redback.utils.CollectionConfig;
import io.redback.utils.Notification;



public class ProcessManager
{
	private Logger logger = Logger.getLogger("io.redback.managers.processmanager");
	protected Firebus firebus;
	protected JSManager jsManager;
	protected String configServiceName;
	protected String dataServiceName;
	protected String accessManagerServiceName;
	protected String objectServiceName;
	protected String domainServiceName;
	protected String signalConsumerName;
	protected ObjectClient objectClient;
	protected AccessManagementClient accessManagementClient;
	protected CollectionConfig piCollectionConfig;
	protected CollectionConfig gmCollectionConfig;
	protected HashMap<String, HashMap<Integer, Process>> processes;
	protected HashMap<Long, HashMap<String, ProcessInstance>> transactions;
	protected String processUserName;
	protected String jwtSecret;
	protected String jwtIssuer;
	protected String sysUserToken;
	protected UserProfile sysUserProfile;
	protected DataMap globalVariables;
	protected List<String> scriptVars;
	
	public ProcessManager(Firebus fb, DataMap config)
	{
		firebus = fb;
		jsManager = new JSManager("process");
		configServiceName = config.getString("configservice");
		dataServiceName = config.getString("dataservice");
		accessManagerServiceName = config.getString("accessmanagementservice");
		accessManagementClient = new AccessManagementClient(firebus, accessManagerServiceName);
		objectServiceName = config.getString("objectservice");
		objectClient = new ObjectClient(firebus, objectServiceName);
		domainServiceName = config.getString("domainservice");
		signalConsumerName = config.getString("signalconsumer");
		processUserName = config.getString("processuser");
		jwtSecret = config.getString("jwtsecret");
		jwtIssuer = config.getString("jwtissuer");
		processes = new HashMap<String, HashMap<Integer, Process>>();
		transactions = new HashMap<Long, HashMap<String, ProcessInstance>>();
		globalVariables = config.getObject("globalvariables");
		piCollectionConfig = config.containsKey("processinstancecollection") ? new CollectionConfig(config.getObject("processinstancecollection")) : new CollectionConfig("rbpm_instance");
		gmCollectionConfig = config.containsKey("groupmembercollection") ? new CollectionConfig(config.getObject("groupmembercollection")) : new CollectionConfig("rbam_groupmember");
		jsManager.setGlobalVariables(globalVariables);
		scriptVars = new ArrayList<String>();
		scriptVars.add("pid");
		scriptVars.add("pm");
		scriptVars.add("firebus");
		scriptVars.add("oc");
		scriptVars.add("data");
		scriptVars.add("processuser");
	}

	
	public Firebus getFirebus()
	{
		return firebus;
	}
	
	public JSManager getJSManager()
	{
		return jsManager;
	}
	
	public ObjectClient getObjectClient()
	{
		return objectClient;
	}

	public List<String> getScriptVariableNames()
	{
		return scriptVars;
	}
	
	public void refreshAllConfigs()
	{
		processes.clear();
	}
	

	protected void loadProcess(String name) throws RedbackException
	{
		try
		{
			DataMap request = new DataMap();
			request.put("action", "list");
			request.put("service", "rbpm");
			request.put("category", "process");
			request.put("filter", new DataMap("name", name));
			DataMap configList = request(configServiceName, request);
			DataList list = configList.getList("result");
			if(list.size() > 0)
			{
				HashMap<Integer, Process> versions = new HashMap<Integer, Process>();
				for(int i = 0; i < list.size(); i++)
				{
					Process process = new Process(this, list.getObject(i));
					versions.put(list.getObject(i).getNumber("version").intValue(), process);
				}
				processes.put(name, versions);				
			}
		}
		catch(Exception e)
		{
			logger.severe(e.getMessage());
			throw new RedbackException("Exception getting process config '" + name + "'", e);
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
	
	protected ProcessInstance getProcessInstance(Actionner actionner, String pid) throws RedbackException
	{
		ProcessInstance pi = getFromCurrentTransaction(pid);
		if(pi == null)
		{
			try 
			{
				DataMap request = new DataMap();
				request.put("object", piCollectionConfig.getName());
				request.put("filter", new DataMap(piCollectionConfig.getField("_id"), pid));
				DataMap response = request(dataServiceName, request);
				pi = new ProcessInstance(actionner, this, piCollectionConfig.convertObjectToCanonical(response.getObject("result.0")));
				putInCurrentTransaction(pi);
			} 
			catch (Exception e) 
			{
				throw new RedbackException("Error retreiving process instance", e);
			} 	
		}
		return pi;
	}
	
	public String getProcessUsername() 
	{
		return processUserName;
	}
	
	public Session getProcessUserSession(String sessionId) throws RedbackException 
	{
		Session session = new Session(sessionId);
		if(sysUserProfile != null  &&  sysUserProfile.getExpiry() < System.currentTimeMillis())
			sysUserProfile = null;

		if(sysUserProfile == null)
		{
			try
			{
				Algorithm algorithm = Algorithm.HMAC256(jwtSecret);
				sysUserToken = JWT.create()
						.withIssuer(jwtIssuer)
						.withClaim("email", processUserName)
						.withExpiresAt(new Date(System.currentTimeMillis() + 3600000))
						.sign(algorithm);
				sysUserProfile = accessManagementClient.validate(session, sysUserToken);
			}
			catch(Exception e)
			{
				error("Error authenticating sys user", e);
			}
		}
		session.setUserProfile(sysUserProfile);
		session.setToken(sysUserToken);
		return session;
	}
	
	public DataMap getGlobalVariables()
	{
		return globalVariables;
	}
	
	public String getObjectServiceName()
	{
		return objectServiceName;
	}
	
	public String getDomainServiceName()
	{
		return domainServiceName;
	}
	
	public ProcessInstance initiateProcess(Actionner actionner, String processName, String domain, DataMap data) throws RedbackException
	{
		logger.finer("Initiating process '" + processName + "'");
		ProcessInstance pi = null;
		Process process = getProcess(processName);
		if(process != null)
		{
			if(domain == null && actionner.isUser())
				domain = actionner.getUserProfile().getAttribute("rb.defaultdomain");
			pi = process.createInstance(actionner, domain, data);
			putInCurrentTransaction(pi);
			commitInstance(pi);
			process.startInstance(actionner, pi);
			logger.finer("Initiated instance '" + pi.getId() + "' for process '" + processName + "'");
		}
		else
		{
			throw new RedbackException("No process found for name '" + processName + "'");
		}
		return pi;
	}

	public List<Notification> getNotifications(Actionner actionner, DataMap filter, DataList viewdata) throws RedbackException
	{
		List<Notification> list = new ArrayList<Notification>();
		if(actionner.isUser())
			loadGroupsOf(actionner);
		DataMap fullFilter = new DataMap();
		if(filter != null)
			fullFilter.merge(filter);
		if(!actionner.getId().equals(getProcessUsername())) 
		{
			DataList assigneeInList = new DataList();
			assigneeInList.add(actionner.getId());
			List<String> groups = actionner.getGroups();
			for(int i = 0; i < groups.size(); i++)
				assigneeInList.add(groups.get(i));
			fullFilter.put("assignees.id", new DataMap("$in", assigneeInList));
		}
		List<ProcessInstance> instances = findProcesses(actionner, fullFilter);
		for(int i = 0; i < instances.size(); i++)
		{
			ProcessInstance pi = instances.get(i);
			Process process = getProcess(pi.getProcessName());
			ProcessUnit pu = process.getNode(pi.getCurrentNode());
			Notification notification = null;
			if(pu instanceof InteractionUnit)
				notification = ((InteractionUnit)pu).getAssignment(actionner, pi);

			if(notification != null)
			{
				if(viewdata != null  &&  viewdata.size() > 0)
				{
					for(int j = 0; j < viewdata.size(); j++)
					{
						String key = viewdata.getString(j); 
						notification.addData(key, pi.getData().getString(key));
					}
				}
				list.add(notification);
			}
		}
		return list;
	}
	
	public int getNotificationCount(Actionner actionner, DataMap filter) throws RedbackException
	{
		List<Notification> assignments = getNotifications(actionner, filter, null);
		int count = assignments.size();
		return count;
	}
	
	public void actionProcess(Actionner actionner, String pid, String action, DataMap data) throws RedbackException
	{
		ProcessInstance pi = getProcessInstance(actionner, pid);
		logger.finer("Processing action " + action + " on process " + pi.getProcessName() + ":" + pid);
		Process process = getProcess(pi.getProcessName(), pi.getProcessVersion());
		ProcessUnit pu = process.getNode(pi.getCurrentNode());
		if(pu instanceof InteractionUnit)
		{
			loadGroupsOf(actionner);
			process.action(actionner, pi, action, data);
		}
		else
		{
			throw new RedbackException("The process in not on an interaction node");
		}
		logger.finer("Finished processing action");
	}
	
	public void interruptProcess(Actionner actionner, String pid) throws RedbackException
	{
		ProcessInstance pi = getProcessInstance(actionner, pid);
		logger.finer("Interrupting interaction on process " + pi.getProcessName() + ":" + pid);
		Process process = getProcess(pi.getProcessName(), pi.getProcessVersion());
		ProcessUnit pu = process.getNode(pi.getCurrentNode());
		if(pu instanceof InteractionUnit)
		{
			process.interrupt(actionner, pi);
		}
		else
		{
			throw new RedbackException("The process in not on an interaction node");
		}
		logger.finer("Finished processing action");
	}
	
	public ArrayList<ProcessInstance> findProcesses(Actionner actionner, DataMap filter) throws RedbackException
	{
		logger.finer("Finding processes for " + filter.toString());
		ArrayList<ProcessInstance> list = new ArrayList<ProcessInstance>();
		try 
		{
			DataMap fullFilterMap = new DataMap();
			fullFilterMap.merge(filter);
			if(!actionner.getId().equals(getProcessUsername()))
				fullFilterMap.put("domain", actionner.isUser() ? actionner.getUserProfile().getDBFilterDomainClause() : actionner.getProcessInstance().getDomain());
			DataFilter fullFilter = new DataFilter(fullFilterMap);
			long txId = Thread.currentThread().getId();
			HashMap<String, ProcessInstance> txProcesses = transactions.get(txId);
			Iterator<String> it = txProcesses.keySet().iterator();
			while(it.hasNext()) {
				ProcessInstance pi = txProcesses.get(it.next());
				if(fullFilter.apply(pi.getJSON())) {
					logger.finer("Found process " + pi.getProcessName() + ":" + pi.getId() + " in transaction with data " + pi.getData());
					list.add(pi);
				}
			}
			DataMap result = request(dataServiceName, new DataMap("{object:rbpm_instance, filter:" + fullFilterMap + "}"));
			DataList resultList = result.getList("result");
			for(int i = 0; i < resultList.size(); i++)
			{
				String pid = resultList.getObject(i).getString("_id");
				ProcessInstance pi = getFromCurrentTransaction(pid);
				if(pi == null)
				{
					pi = new ProcessInstance(actionner, this, resultList.getObject(i));
					putInCurrentTransaction(pi);
					logger.finer("Found process " + pi.getProcessName() + ":" + pi.getId() + " in database with data " + pi.getData());
					list.add(pi);
				} 
			}
		} 
		catch (Exception e) 
		{
			throw new RedbackException("Error retreiving process instance", e);
		} 	
		logger.finer("Finished finding processes");
		return list;
	}
	
	protected void loadGroupsOf(Actionner actionner) throws RedbackException
	{
		logger.finer("Finding groups for " + actionner.getId());
		try 
		{
			DataMap request = new DataMap();
			request.put("object", gmCollectionConfig.getName());
			DataMap filter = new DataMap();
			filter.put(gmCollectionConfig.getField("domain"), actionner.isUser() ? actionner.getUserProfile().getDBFilterDomainClause() : actionner.getProcessInstance().getDomain());
			filter.put(gmCollectionConfig.getField("username"), actionner.getId());
			request.put("filter", filter);
			DataMap result = request(dataServiceName, request);		
			DataList resultList = result.getList("result");
			for(int i = 0; i < resultList.size(); i++)
				actionner.addGroup(resultList.getObject(i).getString(gmCollectionConfig.getField("group")));
		} 
		catch (Exception e) 
		{
			throw new RedbackException("Error retreiving groups", e);
		} 	
		logger.finer("Finished finding groups");
	}
	
	public List<String> getUsersOfGroup(String domain, String groupid) throws RedbackException
	{
		logger.finer("Finding users for group " + groupid);
		List<String> users = new ArrayList<String>();
		try 
		{
			DataMap request = new DataMap();
			request.put("object", gmCollectionConfig.getName());
			DataMap filter = new DataMap();
			filter.put(gmCollectionConfig.getField("domain"), domain);
			filter.put(gmCollectionConfig.getField("group"), groupid);
			request.put("filter", filter);
			DataMap result = request(dataServiceName, request);		
			DataList resultList = result.getList("result");
			for(int i = 0; i < resultList.size(); i++)
				users.add(resultList.getObject(i).getString(gmCollectionConfig.getField("username")));
		} 
		catch (Exception e) 
		{
			throw new RedbackException("Error retreiving user for group", e);
		} 	
		logger.finer("Finished finding users for group");	
		return users;
	}
	
	public void sendNotification(Notification notification)
	{
		if(signalConsumerName != null) 
		{
			try 
			{
				DataMap signal = new DataMap();
				signal.put("type", "processnotification");
				signal.put("notification", notification.getDataMap());
				Payload payload = new Payload(signal.toString());
				logger.finest("Publishing signal : " + signal);
				firebus.publish(signalConsumerName, payload);
				logger.finest("Published signal : " + signal);
			}
			catch(Exception e) 
			{
				logger.severe("Cannot send out signal : " + e.getMessage());
			}
		}
	}
	
	public void initiateCurrentTransaction() 
	{
		long txId = Thread.currentThread().getId();
		synchronized(transactions)
		{
			transactions.put(txId, new HashMap<String, ProcessInstance>());
		}
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
	
	public void commitCurrentTransaction() throws RedbackException 
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
				commitInstance(pi);
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
			Payload respPayload = firebus.requestService(service, reqPayload, 10000);
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
	
	protected void commitInstance(ProcessInstance pi) throws RedbackException
	{
		try
		{
			logger.finest("Publishing to firebus service : " + dataServiceName + "  ");
			firebus.requestService(dataServiceName, new Payload("{object:rbpm_instance, key: {_id:" + pi.getId() + "}, data:" + pi.getJSON() + ", operation:replace}"));
		}
		catch(FunctionErrorException | FunctionTimeoutException e)
		{
			throw new RedbackException("Error publishing data", e);
		}		
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
