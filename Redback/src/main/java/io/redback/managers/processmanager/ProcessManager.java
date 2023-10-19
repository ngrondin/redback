package io.redback.managers.processmanager;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import io.firebus.Firebus;
import io.firebus.Payload;
import io.firebus.data.DataFilter;
import io.firebus.data.DataList;
import io.firebus.data.DataMap;
import io.firebus.logging.Logger;
import io.firebus.script.ScriptFactory;
import io.redback.client.AccessManagementClient;
import io.redback.client.ConfigClient;
import io.redback.client.DataClient;
import io.redback.client.DataClient.DataTransaction;
import io.redback.client.ObjectClient;
import io.redback.exceptions.RedbackException;
import io.redback.exceptions.RedbackInvalidRequestException;
import io.redback.managers.processmanager.units.InteractionUnit;
import io.redback.security.Session;
import io.redback.security.SysUserManager;
import io.redback.utils.CollectionConfig;
import io.redback.utils.TxStore;



public class ProcessManager
{
	protected Firebus firebus;
	protected ScriptFactory scriptFactory;
	protected boolean loadAllOnInit;
	protected int preCompile;
	protected String configServiceName;
	protected String dataServiceName;
	protected String accessManagerServiceName;
	protected String objectServiceName;
	protected String domainServiceName;
	protected String processNotificationChannel;
	protected ObjectClient objectClient;
	protected DataClient dataClient;
	protected ConfigClient configClient;
	protected AccessManagementClient accessManagementClient;
	protected CollectionConfig piCollectionConfig;
	protected CollectionConfig gmCollectionConfig;
	protected CollectionConfig traceCollection;
	protected HashMap<String, HashMap<Integer, Process>> processes;
	protected DataMap globalVariables;
	protected List<String> scriptVars;
	protected SysUserManager sysUserManager;
	protected boolean useMultiDBTransactions;

	
	public ProcessManager(Firebus fb, DataMap config) throws RedbackException
	{
		try {
			firebus = fb;
			scriptFactory = new ScriptFactory();
			loadAllOnInit = config.containsKey("loadalloninit") ? config.getBoolean("loadalloninit") : true;
			preCompile = config.containsKey("precompile") ? config.getNumber("precompile").intValue() : 0;
			configServiceName = config.getString("configservice");
			configClient = new ConfigClient(firebus, configServiceName);
			dataServiceName = config.getString("dataservice");
			dataClient = new DataClient(firebus, dataServiceName);
			accessManagerServiceName = config.getString("accessmanagementservice");
			accessManagementClient = new AccessManagementClient(firebus, accessManagerServiceName);
			objectServiceName = config.getString("objectservice");
			objectClient = new ObjectClient(firebus, objectServiceName);
			domainServiceName = config.getString("domainservice");
			processNotificationChannel = config.getString("processnotificationchannel");
			useMultiDBTransactions = config.containsKey("multidbtransactions") ? config.getBoolean("multidbtransactions") : false;
			sysUserManager = new SysUserManager(accessManagementClient, config);
			processes = new HashMap<String, HashMap<Integer, Process>>();
			globalVariables = config.getObject("globalvariables");
			piCollectionConfig = config.containsKey("processinstancecollection") ? new CollectionConfig(config.getObject("processinstancecollection")) : new CollectionConfig("rbpm_instance");
			gmCollectionConfig = config.containsKey("groupmembercollection") ? new CollectionConfig(config.getObject("groupmembercollection")) : new CollectionConfig("rbam_groupmember");
			traceCollection = config.containsKey("tracecollection") ? new CollectionConfig(config.getObject("tracecollection")) : null;
			scriptFactory.setGlobals(globalVariables);
			scriptVars = new ArrayList<String>();
			scriptVars.add("pid");
			scriptVars.add("pm");
			scriptVars.add("firebus");
			scriptVars.add("oc");
			scriptVars.add("data");
			scriptVars.add("processuser");
			scriptVars.add("lastactioned");
		} catch(Exception e) {
			throw new RedbackException("Error initialising process manager", e);
		}
	}

	
	public Firebus getFirebus()
	{
		return firebus;
	}
	
	public ScriptFactory getScriptFactory()
	{
		return scriptFactory;
	}
	
	public ObjectClient getObjectClient()
	{
		return objectClient;
	}

	public List<String> getScriptVariableNames()
	{
		return scriptVars;
	}
	
	public SysUserManager getSysUserManager()
	{
		return sysUserManager;
	}
	
	public void refreshAllConfigs()
	{
		processes.clear();
		if(loadAllOnInit) {
			Session session = new Session();
			try {
				loadAppProcesses(session);
			} catch(Exception e) {
				Logger.severe("rb.process.refreshconfig", null, e);
				//logger.severe(StringUtils.rollUpExceptions(e));
			}
		}
	}
	
	protected void loadAppProcesses(Session session) throws RedbackException
	{
		DataMap result = configClient.listConfigs(session, "rbpm", "process");
		DataList resultList = result.getList("result");
		for(int i = 0; i < resultList.size(); i++)
		{
			DataMap cfg = resultList.getObject(i);
			Process process = new Process(this, cfg);
			String name = cfg.getString("name");
			int version = cfg.getNumber("version").intValue();
			HashMap<Integer, Process> versions = processes.get(name);
			if(versions == null) {
				versions = new HashMap<Integer, Process>(); 
				processes.put(name, versions);
			}
			versions.put(version, process);
		}	
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
			String msg = "Exception getting process config '" + name + "'";
			Logger.severe("rb.process.load", msg, e);
			throw new RedbackException(msg, e);
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
		ProcessInstance pi = actionner.getSession().hasTxStore() ? (ProcessInstance)actionner.getSession().getTxStore().get(pid) : null;
		if(pi == null)
		{
			try 
			{
				DataMap response = dataClient.getData(piCollectionConfig.getName(), new DataMap(piCollectionConfig.getField("_id"), pid));
				DataList result = response.getList("result");
				if(result.size() > 0) {
					pi = new ProcessInstance(actionner, this, piCollectionConfig.convertObjectToCanonical(result.getObject(0)));
					//putInCurrentTransaction(pi);
				} else {
					throw new RedbackInvalidRequestException("Process instance does not exist");
				}
			} 
			catch (Exception e) 
			{
				throw new RedbackException("Error retreiving process instance", e);
			} 	
		}
		return pi;
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
		Logger.finer("rb.process.init.start", new DataMap("name", processName));
		ProcessInstance pi = null;
		Process process = getProcess(actionner.getSession(), processName);
		if(process != null)
		{
			if(domain == null && actionner.isUser())
				domain = actionner.getUserProfile().getAttribute("rb.defaultdomain");
			pi = process.createInstance(actionner, domain, data);
			//This is necessary to allow dependent processes created afterward to interact with this instance before its first segment is committed.
			dataClient.putData(piCollectionConfig.getName(), new DataMap(piCollectionConfig.getField("_id"), pi.getId()), pi.getJSON(), true);
			process.startInstance(actionner, pi);
			Logger.finer("rb.process.init.end", new DataMap("pid", pi.getId(), "name", processName));
		}
		else
		{
			throw new RedbackException("No process found for name '" + processName + "'");
		}
		return pi;
	}
	
	public void continueProcess(Actionner actionner, String pid) throws RedbackException 
	{
		ProcessInstance pi = getProcessInstance(actionner, pid);
		if(pi != null)
		{
			Logger.finer("rb.process.continue.start", new DataMap("name", pi.getProcessName(), "pid", pid));
			Process process = getProcess(actionner.getSession(), pi.getProcessName(), pi.getProcessVersion());
			if(!pi.isComplete()) 
			{
				if(pi.getCurrentNode() == null) 
				{
					process.startInstance(actionner, pi);
				}
				else 
				{
					process.continueInstance(pi);
				}
			}
		}
		Logger.finer("rb.process.continue.end", new DataMap("name", pi.getProcessName(), "pid", pid));
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
		DataMap fullFilter = new DataMap("complete", false);
		if(filter != null)
			fullFilter.merge(filter);
		if(!actionner.getId().equals(sysUserManager.getUsername())) 
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
	
	public void actionProcess(Actionner actionner, String pid, String action, Date date, DataMap data) throws RedbackException
	{
		ProcessInstance pi = getProcessInstance(actionner, pid);
		Logger.finer("rb.process.action.start", new DataMap("name", pi.getProcessName(), "pid", pid, "action", action));
		Process process = getProcess(actionner.getSession(), pi.getProcessName(), pi.getProcessVersion());
		ProcessUnit pu = process.getNode(pi.getCurrentNode());
		if(pu instanceof InteractionUnit)
		{
			loadGroupsOf(actionner);
			process.action(actionner, pi, action, date, data);
		}
		else
		{
			throw new RedbackInvalidRequestException("The process " + pid + " is not on an interaction node to process action " + action);
		}
		Logger.finer("rb.process.action.end", new DataMap("name", pi.getProcessName(), "pid", pid, "action", action));
	}
	
	public void interruptProcess(Actionner actionner, String pid) throws RedbackException
	{
		ProcessInstance pi = getProcessInstance(actionner, pid);
		Logger.finer("rb.process.interrupt.start", new DataMap("name", pi.getProcessName(), "pid", pid));
		Process process = getProcess(actionner.getSession(), pi.getProcessName(), pi.getProcessVersion());
		ProcessUnit pu = process.getNode(pi.getCurrentNode());
		if(pu instanceof InteractionUnit)
		{
			process.interrupt(actionner, pi);
		}
		else
		{
			throw new RedbackInvalidRequestException("The process " + pid + " is not on an interaction node");
		}
		Logger.finer("rb.process.interrupt.end", new DataMap("name", pi.getProcessName(), "pid", pid));
	}
	
	public ArrayList<ProcessInstance> findProcesses(Actionner actionner, DataMap filter, int page, int pageSize) throws RedbackException
	{
		Logger.finer("rb.process.find.start", new DataMap("filter", filter.toString(true)));
		ArrayList<ProcessInstance> list = new ArrayList<ProcessInstance>();
		try 
		{
			DataMap fullFilterMap = new DataMap();
			fullFilterMap.merge(filter);
			if(!actionner.getId().equals(sysUserManager.getUsername()))
				fullFilterMap.put("domain", actionner.getDomainFilterClause());
			DataFilter fullFilter = new DataFilter(fullFilterMap);
			List<Object> txList = actionner.getSession().getTxStore().getAll();
			for(Object o: txList) {
				ProcessInstance pi = (ProcessInstance)o;
				if(fullFilter.apply(pi.getJSON())) {
					Logger.finer("rb.process.find.found", new DataMap("name", pi.getProcessName(), "pid", pi.getId()));
					list.add(pi);
				}
			}

			DataMap result = dataClient.getData("rbpm_instance", fullFilterMap, null, page, pageSize); 
			DataList resultList = result.getList("result");
			for(int i = 0; i < resultList.size(); i++)
			{
				String pid = resultList.getObject(i).getString("_id");
				ProcessInstance pi = actionner.getSession().hasTxStore() ? (ProcessInstance)actionner.getSession().getTxStore().get(pid) : null;
				if(pi == null)
				{
					pi = new ProcessInstance(actionner, this, resultList.getObject(i));
					Logger.finer("rb.process.find.found", new DataMap("name", pi.getProcessName(), "pid", pi.getId()));
					list.add(pi);
				} 
			}
		} 
		catch (Exception e) 
		{
			throw new RedbackException("Error retreiving process instance", e);
		} 	
		Logger.finer("rb.process.find.end", null);
		return list;
	}
	
	protected void loadGroupsOf(Actionner actionner) throws RedbackException
	{
		Logger.finer("rb.process.loadgroups.start", new DataMap("actionner", actionner.getId()));
		try 
		{
			DataMap filter = new DataMap();
			Object domainFilter = actionner.getDomainFilterClause();
			if(domainFilter != null)
				filter.put(gmCollectionConfig.getField("domain"), domainFilter);
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
		Logger.finer("rb.process.loadgroups.end", new DataMap("actionner", actionner.getId()));
	}
	
	public List<String> getUsersOfGroup(String domain, String groupid) throws RedbackException
	{
		Logger.finer("rb.process.getusersofgroup.start", new DataMap("group", groupid));
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
		Logger.finer("rb.process.getusersofgroup.end", new DataMap("group", groupid));
		return users;
	}
	
	public void sendNotification(Map<String, Notification> sendMap)
	{
		if(processNotificationChannel != null) {
			try 
			{
				DataMap data = new DataMap();
				for(String username: sendMap.keySet()) 
					data.put(username, sendMap.get(username).getDataMap());
				Payload payload = new Payload(data);
				firebus.publish(processNotificationChannel, payload);
			}
			catch(Exception e) 
			{
				Logger.severe("rb.process.sendnotif", "Cannot send out signal", e);
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
				intcomp.put("completed", true);
				DataMap sendMap = new DataMap();
				for(String username: to) {
					sendMap.put(username, intcomp);
				}
				Payload payload = new Payload(sendMap);
				firebus.publish(processNotificationChannel, payload);
			}
			catch(Exception e) 
			{
				Logger.severe("rb.process.sendcompletion", "Cannot send out signal", e);
			}	
		}
	}
	
	public void initiateCurrentTransaction(Session session, boolean store) 
	{
		session.setScriptContext(getScriptFactory().createScriptContext());
		if(store)
			session.setTxStore(new TxStore<Object>());
	}

	
	public void commitCurrentTransaction(Session session) throws RedbackException 
	{
		if(session.hasTxStore()) {
			List<ProcessInstance> list = new ArrayList<ProcessInstance>();
			for(Object pi : session.getTxStore().getAll())
				list.add((ProcessInstance)pi);

			List<DataTransaction> dbtxs = new ArrayList<DataTransaction>();
			for(ProcessInstance pi: list) {
				if(pi.isUpdated()) {
					dbtxs.add(dataClient.createPut(piCollectionConfig.getName(), new DataMap(piCollectionConfig.getField("_id"), pi.getId()), pi.getJSON(), true));
				}		
				dbtxs.addAll(pi.getDBTraceTransactions());
			}
			
			if(dbtxs.size() > 0) {
				if(this.useMultiDBTransactions) {
					dataClient.multi(dbtxs);
				} else {
					for(DataTransaction tx: dbtxs) {
						dataClient.runTransaction(tx);
					}
				}
			}

		}		
	}
}
