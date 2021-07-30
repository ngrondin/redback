package io.redback.managers.processmanager.units;

import io.firebus.utils.DataMap;
import io.redback.exceptions.RedbackException;
import io.redback.managers.processmanager.Process;
import io.redback.managers.processmanager.ProcessInstance;
import io.redback.managers.processmanager.ProcessManager;
import io.redback.managers.processmanager.ProcessUnit;

public class JoinUnit extends ProcessUnit 
{
	protected String nextNode;

	public JoinUnit(ProcessManager pm, Process p, DataMap config) 
	{
		super(pm, p, config);
		nextNode = config.getString("nextnode");
	}
	
	public void execute(ProcessInstance pi) throws RedbackException
	{
		logger.finer("Starting Join node");
		pi.setCurrentNode(nextNode);
		logger.finer("Finished Join node");
	}

}
