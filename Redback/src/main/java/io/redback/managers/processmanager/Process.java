package io.redback.managers.processmanager;

import java.util.HashMap;
import java.util.logging.Logger;

import io.firebus.utils.DataList;
import io.firebus.utils.DataMap;
import io.redback.RedbackException;
import io.redback.managers.processmanager.units.ActionUnit;
import io.redback.managers.processmanager.units.ConditionalUnit;
import io.redback.managers.processmanager.units.InteractionUnit;
import io.redback.managers.processmanager.units.RedbackObjectExecuteUnit;
import io.redback.managers.processmanager.units.RedbackObjectUpdateUnit;
import io.redback.managers.processmanager.units.ScriptUnit;
import io.redback.security.Session;

public class Process 
{
	private Logger logger = Logger.getLogger("io.redback.managers.processmanager");
	protected HashMap<String, ProcessUnit> nodes;
	protected String startNode;
	protected String name;
	protected int version;
	protected String domain;
	protected ProcessManager processManager;
	
	protected Process(ProcessManager pm, DataMap config) throws RedbackException
	{
		processManager = pm;
		nodes = new HashMap<String, ProcessUnit>();
		name = config.getString("name");
		version = config.getNumber("version").intValue();
		domain = config.getString("domain");
		startNode = config.getString("startnode");
		DataList nodeList = config.getList("nodes");
		for(int i = 0; i < nodeList.size(); i++)
		{
			DataMap nodeConfig = nodeList.getObject(i);
			String unitType = nodeConfig.getString("type");
			ProcessUnit unit = null;
			if(unitType.equals("script"))
				unit = new ScriptUnit(processManager, nodeConfig);
			else if(unitType.equals("condition"))
				unit = new ConditionalUnit(processManager, nodeConfig);
			else if(unitType.equals("action"))
				unit = new ActionUnit(processManager, nodeConfig);
			else if(unitType.equals("interaction"))
				unit = new InteractionUnit(processManager, nodeConfig);
			else if(unitType.equals("rbobjectupdate"))
				unit = new RedbackObjectUpdateUnit(processManager, nodeConfig);
			else if(unitType.equals("rbobjectexecute"))
				unit = new RedbackObjectExecuteUnit(processManager, nodeConfig);
			else
				error("Unit type '" + unitType + "' is not recognised");
			nodes.put(unit.getId(), unit);
		}
	}
	
	public ProcessUnit getNode(String n)
	{
		return nodes.get(n);
	}

	public ProcessInstance createInstance(Session session, DataMap data)
	{
		ProcessInstance pi = new ProcessInstance(name, version, session.getUserProfile().getAttribute("rb.defaultdomain"), data);
		return pi;
	}
	
	public DataMap startInstance(Session session, ProcessInstance pi) throws RedbackException
	{
		logger.info("Starting process '" + name + "' instance");
		DataMap result = new DataMap();
		pi.setCurrentNode(startNode);
		pi.getData().put("originator", session.getUserProfile().getUsername());
		execute(pi, result);
		logger.info("Process '" + name + "' started instance '" + pi.getId() + "'");
		return result;
	}
	
	public DataMap continueInstance(ProcessInstance pi) throws RedbackException
	{
		logger.info("Continuing process '" + name + "' instance");
		DataMap result = new DataMap();
		execute(pi, result);
		logger.info("Process '" + name + "' has continued '" + pi.getId() + "'");
		return result;
	}
	
	public DataMap processAction(Session session, String extpid, ProcessInstance pi, String action, DataMap data) throws RedbackException
	{
		logger.info("Processing action '" + action + "' of  process " + name + ":" + pi.getId() + "");
		DataMap result = new DataMap();
		String currentNodeId = pi.getCurrentNode();
		if(currentNodeId != null)
		{
			ProcessUnit currentNode = nodes.get(currentNodeId);
			if(currentNode instanceof InteractionUnit)
			{
				((InteractionUnit)currentNode).processAction(session, extpid,  pi, action, data);
				execute(pi, result);
			}
			else
			{
				error("Current node is not an interaction node");
			}
		}
		else
		{
			if(pi.isComplete())
				error("Process instance " + pi.getId() + " is complete");
			else
				error("Process instance " + pi.getId() + " has not been start yet");
		}	
		logger.info("Finished processing action '" + action + "' of  " + name + ":" + pi.getId() + "");
		return result;
	}
	
	protected void execute(ProcessInstance pi, DataMap result) throws RedbackException
	{
		Session sysUserSession = processManager.getSystemUserSession(pi.getDomain());
		if(sysUserSession != null  &&  sysUserSession.getUserProfile().getAttribute("rb.process.sysuser").equals("true"))
		{
			String currentNodeId = pi.getCurrentNode();
			if(currentNodeId != null)
			{
				while(currentNodeId != null  &&  nodes.get(currentNodeId) != null  &&   !pi.isComplete()  &&  !(nodes.get(currentNodeId) instanceof InteractionUnit))
				{
					logger.info("Executing node '" + currentNodeId + "'");
					nodes.get(currentNodeId).execute(pi, result);
					currentNodeId = pi.getCurrentNode();
				}
				
				if(currentNodeId != null  &&  !pi.isComplete())
				{
					if(nodes.get(currentNodeId) != null)
					{
						if(nodes.get(currentNodeId) instanceof InteractionUnit)
						{
							logger.info("Executing node '" + currentNodeId + "'");
							((InteractionUnit)nodes.get(currentNodeId)).execute(pi, result);			
						}
					}
					else
					{
						throw new RedbackException("Current node '" + currentNodeId + " is unknown in process '" + name + "' version " + version);
					}
				}
			}
			else
			{
				if(pi.isComplete())
					error("Process instance " + pi.getId() + " is complete");
				else
					error("Process instance " + pi.getId() + " has not been start yet");
			}	
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
