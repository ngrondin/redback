package io.redback.managers.processmanager;

import io.firebus.data.DataMap;
import io.redback.exceptions.RedbackException;
import io.redback.utils.StringUtils;

public abstract class ProcessUnit 
{
	protected String nodeId;
	protected String name;
	protected ProcessManager processManager;
	protected String jsFunctionNameRoot;
	
	public ProcessUnit(ProcessManager pm, Process p, DataMap config)
	{
		nodeId = config.getString("id");
		name = config.getString("name");
		processManager = pm;
		jsFunctionNameRoot = p.getName() + "_node_" + StringUtils.base16(config.getString("id").hashCode());
	}
	
	public String getId()
	{
		return nodeId;
	}
	
	public String getName()
	{
		return name;
	}
	
	public void execute(ProcessInstance pi) throws RedbackException
	{
		pi.setCurrentNode(null);
	}

}
