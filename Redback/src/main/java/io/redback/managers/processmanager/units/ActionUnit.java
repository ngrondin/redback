package io.redback.managers.processmanager.units;

import java.util.ArrayList;

import io.firebus.utils.DataMap;
import io.redback.RedbackException;
import io.redback.managers.processmanager.Assignment;
import io.redback.managers.processmanager.ProcessInstance;
import io.redback.managers.processmanager.ProcessManager;
import io.redback.managers.processmanager.ProcessUnit;
import io.redback.security.Session;

public class ActionUnit extends ProcessUnit 
{
	protected String interactionCode;
	protected String action;
	protected String nextNode;

	public ActionUnit(ProcessManager pm, DataMap config) 
	{
		super(pm, config);
		interactionCode = config.getString("interaction");
		action = config.getString("action");
		nextNode = config.getString("nextnode");
	}
	
	public void execute(ProcessInstance pi, DataMap result) throws RedbackException
	{
		logger.info("Starting Action node");
		Session sysUserSession = processManager.getSystemUserSession(pi.getDomain());
		ArrayList<Assignment> assignments = processManager.getAssignments(sysUserSession, pi.getId(), null, null);
		for(int i = 0; i < assignments.size(); i++)
		{
			Assignment assignment = assignments.get(i);
			if(interactionCode.equals(assignment.interaction))
			{
				boolean actionExists = false;
				for(int j = 0; j < assignment.actions.size(); j++)
					if(assignment.actions.get(j).action.equals(action))
						actionExists = true;
				if(actionExists)
				{
					logger.fine("Actionning interaction '" + interactionCode + "' with action '" + action +"' in instance '" + assignment.pid +"'");
					DataMap actionResult = processManager.processAction(sysUserSession, pi.getId(), assignment.pid, action, null);
					result.merge(actionResult);
				}
				else
				{
					logger.info("ActionUnit tried to process the interaction '" + interactionCode + "' with an invalid action '" + action + "'");
				}
			}
		}	

		pi.setCurrentNode(nextNode);
		logger.info("Finished Action node");
	}

}
