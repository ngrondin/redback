package io.redback.managers.processmanager;

import java.util.HashMap;
import java.util.logging.Logger;

import io.firebus.utils.DataList;
import io.firebus.utils.DataMap;
import io.redback.RedbackException;
import io.redback.managers.processmanager.units.ActionUnit;
import io.redback.managers.processmanager.units.ConditionalUnit;
import io.redback.managers.processmanager.units.DomainServiceUnit;
import io.redback.managers.processmanager.units.FirebusRequestUnit;
import io.redback.managers.processmanager.units.InteractionUnit;
import io.redback.managers.processmanager.units.RedbackObjectExecuteUnit;
import io.redback.managers.processmanager.units.RedbackObjectGetUnit;
import io.redback.managers.processmanager.units.RedbackObjectUpdateUnit;
import io.redback.managers.processmanager.units.ScriptUnit;

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
			else if(unitType.equals("rbobjectget"))
				unit = new RedbackObjectGetUnit(processManager, nodeConfig);
			else if(unitType.equals("rbobjectupdate"))
				unit = new RedbackObjectUpdateUnit(processManager, nodeConfig);
			else if(unitType.equals("rbobjectexecute"))
				unit = new RedbackObjectExecuteUnit(processManager, nodeConfig);
			else if(unitType.equals("firebusrequest"))
				unit = new FirebusRequestUnit(processManager, nodeConfig);
			else if(unitType.equals("domainservice"))
				unit = new DomainServiceUnit(processManager, nodeConfig);
			else
				error("Unit type '" + unitType + "' is not recognised");
			nodes.put(unit.getId(), unit);
		}
	}
	
	public ProcessUnit getNode(String n)
	{
		return nodes.get(n);
	}

	public ProcessInstance createInstance(Actionner actionner, DataMap data) throws RedbackException
	{
		String domain = null;
		if(actionner.isUser())
			domain = actionner.getUserProfile().getAttribute("rb.defaultdomain");
		else
			domain = processManager.getProcessInstance(actionner.getId()).getDomain();
		ProcessInstance pi = new ProcessInstance(name, version, domain, data);
		return pi;
	}
	
	public void startInstance(Actionner actionner, ProcessInstance pi) throws RedbackException
	{
		logger.info("Starting process '" + name + "' instance");
		pi.setCurrentNode(startNode);
		pi.getData().put("originator", actionner.getId());
		execute(pi);
		logger.info("Process '" + name + "' started instance '" + pi.getId() + "'");
	}
	
	public void continueInstance(ProcessInstance pi) throws RedbackException
	{
		logger.info("Continuing process '" + name + "' instance");
		execute(pi);
		logger.info("Process '" + name + "' has continued '" + pi.getId() + "'");
	}
	
	public void processAction(Actionner actionner, ProcessInstance pi, String action, DataMap data) throws RedbackException
	{
		logger.info("Processing action '" + action + "' of  process " + name + ":" + pi.getId() + "");
		String currentNodeId = pi.getCurrentNode();
		if(currentNodeId != null)
		{
			ProcessUnit currentNode = nodes.get(currentNodeId);
			if(currentNode instanceof InteractionUnit)
			{
				((InteractionUnit)currentNode).processAction(actionner,  pi, action, data);
				if(pi.getCurrentNode() != null)
					execute(pi);
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
	}
	
	protected void execute(ProcessInstance pi) throws RedbackException
	{
		String currentNodeId = pi.getCurrentNode();
		if(currentNodeId != null)
		{
			while(currentNodeId != null  &&  nodes.get(currentNodeId) != null  &&   !pi.isComplete()  &&  !(nodes.get(currentNodeId) instanceof InteractionUnit))
			{
				logger.info("Executing node '" + currentNodeId + "'");
				nodes.get(currentNodeId).execute(pi);
				currentNodeId = pi.getCurrentNode();
			}
			
			if(currentNodeId != null  &&  !pi.isComplete())
			{
				if(nodes.get(currentNodeId) != null)
				{
					if(nodes.get(currentNodeId) instanceof InteractionUnit)
					{
						logger.info("Executing node '" + currentNodeId + "'");
						((InteractionUnit)nodes.get(currentNodeId)).execute(pi);			
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
