package io.redback.managers.processmanager;

import java.util.logging.Logger;

import io.firebus.utils.DataMap;
import io.redback.exceptions.RedbackException;
import io.redback.utils.StringUtils;

public abstract class ProcessUnit 
{
	protected Logger logger = Logger.getLogger("io.redback.managers.processmanager");
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
/*
	protected void error(String msg) throws RedbackException
	{
		error(msg, null);
	}
	
	protected void error(String msg, Exception cause) throws RedbackException
	{
		String extendedMsg = msg;
		Throwable t = cause;
		while(t != null)
		{
			if(extendedMsg.length() > 0)
				extendedMsg += " : ";
			extendedMsg += t.getMessage();
			t = t.getCause();
		}
		logger.severe(extendedMsg);
		if(cause != null)
			throw new RedbackException(msg, cause);
		else
			throw new RedbackException(msg);
	}*/

}
