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
import io.firebus.utils.DataFilter;
import io.firebus.utils.DataList;
import io.firebus.utils.DataMap;
import io.redback.RedbackException;
import io.redback.client.AccessManagementClient;
import io.redback.client.ConfigurationClient;
import io.redback.client.DataClient;
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
	protected String processNotificationChannel;
	protected ObjectClient objectClient;
	protected DataClient dataClient;
	protected ConfigurationClient configClient;
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
		configClient = new ConfigurationClient(firebus, configServiceName);
		dataServiceName = config.getString("dataservice");
		dataClient = new DataClient(firebus, dataServiceName);
		accessManagerServiceName = config.getString("accessmanagementservice");
		accessManagementClient = new AccessManagementClient(firebus, accessManagerServiceName);
		objectServiceName = config.getString("objectservice");
		objectClient = new ObjectClient(firebus, objectServiceName);
		domainServiceName = config.getString("domainservice");
		processNotificationChannel = config.getString("processnotificationchannel");
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
	

	protected void loadProcess(Session session, String name) throws RedbackException
	{
		try
		{
			DataMap configList = configClient.listConfigs(session, "rbpm", "process", new DataMap("name", name));
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

	protected Process getProcess(Session session, String name) throws RedbackException
	{
		Process process = null;
		if(!processes.containsKey(name))
			loadProcess(session, name);
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
	
	protected Process getProcess(Session session, String name, int v) throws RedbackException
	{
		Process process = null;
		if(!processes.containsKey(name))
			loadProcess(session, name);
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
				DataMap response = dataClient.getData(piCollectionConfig.getName(), new DataMap(piCollectionConfig.getField("_id"), pid));
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
				throw new RedbackException("Error authenticating sys user", e);
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
		Process process = getProcess(actionner.getSession(), processName);
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
		return getNotifications(actionner, filter, viewdata, 0, 50);
	}
	
	public List<Notification> getNotifications(Actionner actionner, DataMap filter, DataList viewdata, int page, int pageSize) throws RedbackException
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
		List<ProcessInstance> instances = findProcesses(actionner, fullFilter, page, pageSize);
		for(int i = 0; i < instances.size(); i++)
		{
			ProcessInstance pi = instances.get(i);
			Process process = getProcess(actionner.getSession(), pi.getProcessName());
			ProcessUnit pu = process.getNode(pi.getCurrentNode());
			Notification notification = null;
			if(pu instanceof InteractionUnit)
				notification = ((InteractionUnit)pu).getNotificationForActionner(actionner, pi);

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
		List<Notification> assignments = getNotifications(actionner, filter, null, 0, 50);
		int count = assignments.size();
		return count;
	}
	
	public void actionProcess(Actionner actionner, String pid, String action, DataMap data) throws RedbackException
	{
		ProcessInstance pi = getProcessInstance(actionner, pid);
		logger.finer("Processing action " + action + " on process " + pi.getProcessName() + ":" + pid);
		Process process = getProcess(actionner.getSession(), pi.getProcessName(), pi.getProcessVersion());
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
		Process process = getProcess(actionner.getSession(), pi.getProcessName(), pi.getProcessVersion());
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
	
	public ArrayList<ProcessInstance> findProcesses(Actionner actionner, DataMap filter, int page, int pageSize) throws RedbackException
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
			DataMap result = dataClient.getData("rbpm_instance", fullFilterMap, null, page, pageSize); 
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
			DataMap filter = new DataMap();
			filter.put(gmCollectionConfig.getField("domain"), actionner.isUser() ? actionner.getUserProfile().getDBFilterDomainClause() : actionner.getProcessInstance().getDomain());
			filter.put(gmCollectionConfig.getField("username"), actionner.getId());
			DataMap result = dataClient.getData(gmCollectionConfig.getName(), filter);
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
			DataMap filter = new DataMap();
			filter.put(gmCollectionConfig.getField("domain"), domain);
			filter.put(gmCollectionConfig.getField("group"), groupid);
			DataMap result = dataClient.getData(gmCollectionConfig.getName(), filter);
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
		if(processNotificationChannel != null) {
			try 
			{
				Payload payload = new Payload(notification.getDataMap().toString());
				logger.finest("Publishing process notification");
				firebus.publish(processNotificationChannel, payload);
				logger.finest("Published process notification");
			}
			catch(Exception e) 
			{
				logger.severe("Cannot send out signal : " + e.getMessage());
			}	
		}
	}
	
	public void sendInteractionCompletion(String processName, String pid, String code, List<String> to) 
	{
		if(processNotificationChannel != null) {
			try 
			{
				DataMap intcomp = new DataMap();
				intcomp.put("process", processName);
				intcomp.put("pid", pid);
				intcomp.put("code", code);
				DataList toList = new DataList();
				for(String username: to)
					toList.add(username);
				intcomp.put("to", toList);
				intcomp.put("completed", true);
				Payload payload = new Payload(intcomp.toString());
				logger.finest("Publishing process notification completion");
				firebus.publish(processNotificationChannel, payload);
				logger.finest("Published process notification completion");

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
				if(pi.isUpdated()) 
					commitInstance(pi);
			}
			synchronized(transactions)
			{
				transactions.remove(txId);
			}
		}		
	}
	
	protected void commitInstance(ProcessInstance pi) throws RedbackException
	{
		logger.finest("Publishing to firebus service : " + dataServiceName + "  ");
		DataMap key = new DataMap(piCollectionConfig.getField("_id"), pi.getId());
		dataClient.putData(piCollectionConfig.getName(), key, pi.getJSON(), true);
	}

}
