package io.redback.managers.processmanager;

import java.util.logging.Logger;

import io.firebus.utils.DataMap;
import io.redback.RedbackException;

public abstract class ProcessUnit 
{
	protected Logger logger = Logger.getLogger("io.redback.managers.processmanager");
	protected String nodeId;
	protected ProcessManager processManager;
	
	public ProcessUnit(ProcessManager pm, DataMap config)
	{
		nodeId = config.getString("id");
		processManager = pm;
	}
	
	public String getId()
	{
		return nodeId;
	}
	
	public void execute(ProcessInstance pi, DataMap result) throws RedbackException
	{
		pi.setCurrentNode(null);
	}

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
	}

}
