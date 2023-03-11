package io.redback.managers.processmanager;

import java.util.Date;
import java.util.HashMap;

import io.firebus.data.DataList;
import io.firebus.data.DataMap;
import io.firebus.logging.Logger;
import io.redback.exceptions.RedbackException;
import io.redback.exceptions.RedbackInvalidConfigException;
import io.redback.exceptions.RedbackInvalidRequestException;
import io.redback.managers.processmanager.units.ActionUnit;
import io.redback.managers.processmanager.units.ConditionalUnit;
import io.redback.managers.processmanager.units.DomainServiceUnit;
import io.redback.managers.processmanager.units.FirebusRequestUnit;
import io.redback.managers.processmanager.units.InteractionUnit;
import io.redback.managers.processmanager.units.JoinUnit;
import io.redback.managers.processmanager.units.RedbackObjectExecuteUnit;
import io.redback.managers.processmanager.units.RedbackObjectGetUnit;
import io.redback.managers.processmanager.units.RedbackObjectUpdateUnit;
import io.redback.managers.processmanager.units.ScriptUnit;

public class Process 
{
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
		version = config.containsKey("version") ? config.getNumber("version").intValue() : 0;
		domain = config.getString("domain");
		startNode = config.getString("startnode");
		DataList nodeList = config.getList("nodes");
		for(int i = 0; i < nodeList.size(); i++)
		{
			DataMap nodeConfig = nodeList.getObject(i);
			String unitType = nodeConfig.getString("type");
			ProcessUnit unit = null;
			if(unitType.equals("script"))
				unit = new ScriptUnit(processManager, this, nodeConfig);
			else if(unitType.equals("condition"))
				unit = new ConditionalUnit(processManager, this, nodeConfig);
			else if(unitType.equals("action"))
				unit = new ActionUnit(processManager, this, nodeConfig);
			else if(unitType.equals("interaction"))
				unit = new InteractionUnit(processManager, this, nodeConfig);
			else if(unitType.equals("rbobjectget"))
				unit = new RedbackObjectGetUnit(processManager, this, nodeConfig);
			else if(unitType.equals("rbobjectupdate"))
				unit = new RedbackObjectUpdateUnit(processManager, this, nodeConfig);
			else if(unitType.equals("rbobjectexecute"))
				unit = new RedbackObjectExecuteUnit(processManager, this, nodeConfig);
			else if(unitType.equals("firebusrequest"))
				unit = new FirebusRequestUnit(processManager, this, nodeConfig);
			else if(unitType.equals("domainservice"))
				unit = new DomainServiceUnit(processManager, this, nodeConfig);
			else if(unitType.equals("join"))
				unit = new JoinUnit(processManager, this, nodeConfig);
			else
				throw new RedbackInvalidConfigException("Unit type '" + unitType + "' is not recognised");
			nodes.put(unit.getId(), unit);
		}
	}
	
	public ProcessUnit getNode(String n)
	{
		return nodes.get(n);
	}
	
	public String getName()
	{
		return name;
	}

	public ProcessInstance createInstance(Actionner actionner, String domain, DataMap data) throws RedbackException
	{
		ProcessInstance pi = new ProcessInstance(actionner, processManager, name, version, domain, data);
		return pi;
	}
	
	public void startInstance(Actionner actionner, ProcessInstance pi) throws RedbackException
	{
		Logger.fine("rb.process.startinstance.start", new DataMap("process", name));
		pi.setCurrentNode(startNode);
		pi.setOriginator(actionner.getId());
		execute(pi);
		Logger.fine("rb.process.startinstance.finish", new DataMap("process", name, "pid", pi.getId()));
	}
	
	public void continueInstance(ProcessInstance pi) throws RedbackException
	{
		Logger.fine("rb.process.continue.start", new DataMap("process", name));
		execute(pi);
		Logger.fine("rb.process.continue.finish", new DataMap("process", name, "pid", pi.getId()));
	}
	
	public void action(Actionner actionner, ProcessInstance pi, String action, Date date, DataMap data) throws RedbackException
	{
		Logger.fine("rb.process.action.start", new DataMap("process", name, "action", action, "pid", pi.getId()));
		String currentNodeId = pi.getCurrentNode();
		if(currentNodeId != null)
		{
			ProcessUnit currentNode = nodes.get(currentNodeId);
			if(currentNode instanceof InteractionUnit)
			{
				((InteractionUnit)currentNode).action(actionner, pi, action, date, data);
				if(pi.getCurrentNode() != null)
					execute(pi);
			}
			else
			{
				throw new RedbackInvalidRequestException("Current node is not an interaction node");
			}
		}
		else
		{
			if(pi.isComplete())
				throw new RedbackInvalidRequestException("Process instance " + pi.getId() + " is complete");
			else
				throw new RedbackInvalidRequestException("Process instance " + pi.getId() + " has not been start yet");
		}	
		Logger.fine("rb.process.action.finish", new DataMap("process", name, "action", action, "pid", pi.getId()));
	}
	
	public void interrupt(Actionner actionner, ProcessInstance pi) throws RedbackException
	{
		Logger.fine("rb.process.interrupt.start", new DataMap("process", name, "pid", pi.getId()));
		String currentNodeId = pi.getCurrentNode();
		if(currentNodeId != null)
		{
			ProcessUnit currentNode = nodes.get(currentNodeId);
			if(currentNode instanceof InteractionUnit)
			{
				((InteractionUnit)currentNode).interrupt(actionner,  pi);
				if(pi.getCurrentNode() != null && pi.getCurrentNode() != currentNodeId)
					execute(pi);
			}
			else
			{
				throw new RedbackInvalidRequestException("Current node is not an interaction node");
			}
		}
		else
		{
			if(pi.isComplete())
				throw new RedbackInvalidRequestException("Process instance " + pi.getId() + " is complete");
			else
				throw new RedbackInvalidRequestException("Process instance " + pi.getId() + " has not been start yet");
		}	
		Logger.fine("rb.process.interrupt.finish", new DataMap("process", name, "pid", pi.getId()));
	}
	
	protected void execute(ProcessInstance pi) throws RedbackException
	{
		String currentNodeId = pi.getCurrentNode();
		if(currentNodeId != null)
		{
			while(currentNodeId != null  &&  nodes.get(currentNodeId) != null  &&   !pi.isComplete()  &&  !(nodes.get(currentNodeId) instanceof InteractionUnit))
			{
				ProcessUnit node = nodes.get(currentNodeId);
				Logger.fine("rb.process.execute", new DataMap("node", node.getName()));
				node.execute(pi);
				currentNodeId = pi.getCurrentNode();
			}
			
			if(currentNodeId != null  &&  !pi.isComplete())
			{
				if(nodes.get(currentNodeId) != null)
				{
					if(nodes.get(currentNodeId) instanceof InteractionUnit)
					{
						Logger.fine("rb.process.execute", new DataMap("node", nodes.get(currentNodeId).getName()));
						((InteractionUnit)nodes.get(currentNodeId)).execute(pi);			
					}
				}
				else
				{
					throw new RedbackInvalidRequestException("Current node '" + currentNodeId + " is unknown in process '" + name + "' version " + version);
				}
			}
		}
		else
		{
			if(pi.isComplete())
				throw new RedbackInvalidRequestException("Process instance " + pi.getId() + " is complete");
			else
				throw new RedbackInvalidRequestException("Process instance " + pi.getId() + " has not been start yet");
		}	
	}

}
