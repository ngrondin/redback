package com.nic.redback.services.processserver;

import java.util.HashMap;
import java.util.logging.Logger;

import com.nic.firebus.utils.JSONList;
import com.nic.firebus.utils.JSONObject;
import com.nic.redback.RedbackException;
import com.nic.redback.security.Session;
import com.nic.redback.services.processserver.units.InteractionUnit;
import com.nic.redback.services.processserver.units.ActionUnit;
import com.nic.redback.services.processserver.units.ScriptUnit;

public class Process 
{
	private Logger logger = Logger.getLogger("com.nic.redback");
	protected HashMap<String, ProcessUnit> nodes;
	protected String startNode;
	protected String name;
	protected int version;
	protected ProcessManager processManager;
	
	protected Process(ProcessManager pm, JSONObject config) throws RedbackException
	{
		processManager = pm;
		nodes = new HashMap<String, ProcessUnit>();
		name = config.getString("name");
		version = config.getNumber("version").intValue();
		startNode = config.getString("startnode");
		JSONList nodeList = config.getList("nodes");
		for(int i = 0; i < nodeList.size(); i++)
		{
			JSONObject nodeConfig = nodeList.getObject(i);
			String unitType = nodeConfig.getString("type");
			ProcessUnit unit = null;
			if(unitType.equals("script"))
				unit = new ScriptUnit(processManager, nodeConfig);
			else if(unitType.equals("action"))
				unit = new ActionUnit(processManager, nodeConfig);
			else if(unitType.equals("interaction"))
				unit = new InteractionUnit(processManager, nodeConfig);
			else
				error("Unit type '" + unitType + "' is not recognised");
			nodes.put(unit.getId(), unit);
		}
	}
	
	public ProcessUnit getNode(String n)
	{
		return nodes.get(n);
	}

	public ProcessInstance createInstance(Session session, JSONObject data)
	{
		ProcessInstance pi = new ProcessInstance(name, version, data);
		return pi;
	}
	
	public void startInstance(Session session, ProcessInstance pi) throws RedbackException
	{
		pi.setCurrentNode(startNode);
		pi.getData().put("originator", session.getUserProfile().getUsername());
		execute(pi);
	}
	
	public void processAction(Session session, String extpid, ProcessInstance pi, String action, JSONObject data) throws RedbackException
	{
		String currentNode = pi.getCurrentNode();
		if(currentNode != null)
		{
			if(nodes.get(currentNode) instanceof InteractionUnit)
			{
				((InteractionUnit)nodes.get(currentNode)).processAction(session, extpid,  pi, action, data);
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

	}
	
	protected void execute(ProcessInstance pi) throws RedbackException
	{
		Session session = processManager.getSystemUserSession();
		if(session != null  &&  session.getUserProfile().getAttribute("rb.process.sysuser").equals("true"))
		{
			String currentNode = pi.getCurrentNode();
			if(currentNode != null)
			{
				while(currentNode != null  &&  nodes.get(currentNode) != null  &&   !pi.isComplete()  &&  !(nodes.get(currentNode) instanceof InteractionUnit))
				{
					nodes.get(currentNode).execute(pi);
					currentNode = pi.getCurrentNode();
				}
				
				if(currentNode != null  &&  !pi.isComplete())
				{
					if(nodes.get(currentNode) != null)
					{
						if(nodes.get(currentNode) instanceof InteractionUnit)
						{
							((InteractionUnit)nodes.get(currentNode)).execute(pi);			
						}
					}
					else
					{
						throw new RedbackException("Current node '" + currentNode + " is unknown in process '" + name + "' version " + version);
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
