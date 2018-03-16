package com.nic.redback.services.processserver;

import java.util.HashMap;

import com.nic.firebus.utils.JSONList;
import com.nic.firebus.utils.JSONObject;
import com.nic.redback.RedbackException;
import com.nic.redback.security.UserProfile;
import com.nic.redback.services.processserver.units.InteractionUnit;
import com.nic.redback.services.processserver.units.NotificationUnit;
import com.nic.redback.services.processserver.units.ScriptUnit;

public class Process 
{
	protected HashMap<String, ProcessUnit> nodes;
	protected String startNode;
	protected String name;
	protected int version;
	
	protected Process(ProcessManager pm, JSONObject config) throws RedbackException
	{
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
				unit = new ScriptUnit(pm, nodeConfig);
			else if(unitType.equals("notification"))
				unit = new NotificationUnit(pm, nodeConfig);
			else if(unitType.equals("interaction"))
				unit = new InteractionUnit(pm, nodeConfig);
			nodes.put(unit.getId(), unit);
		}
	}
	
	public ProcessUnit getNode(String n)
	{
		return nodes.get(n);
	}

	public ProcessInstance createInstance(UserProfile up, JSONObject data)
	{
		ProcessInstance pi = new ProcessInstance(name, version, data);
		return pi;
	}
	
	public void startInstance(UserProfile up, ProcessInstance pi) throws RedbackException
	{
		pi.setCurrentNode(startNode);
		pi.getData().put("originator", up.getUsername());
		execute(up, pi);
	}
	
	public void processAction(UserProfile up, ProcessInstance pi, String action, JSONObject data) throws RedbackException
	{
		if(nodes.get(pi.getCurrentNode()) instanceof InteractionUnit)
		{
			String
			((InteractionUnit)nodes.get(pi.getCurrentNode())).processAction(up, pi, action, data);
			execute(up, pi);
		}
	}
	
	protected void execute(UserProfile up, ProcessInstance pi) throws RedbackException
	{
		while(!(nodes.get(pi.getCurrentNode()) instanceof InteractionUnit)  &&  !pi.isComplete())
		{
			nodes.get(pi.getCurrentNode()).execute(up, pi);
		}
		
		if(nodes.get(pi.getCurrentNode()) instanceof InteractionUnit)
			((InteractionUnit)nodes.get(pi.getCurrentNode())).execute(up, pi);
	}
}
