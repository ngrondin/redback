package io.redback.managers.processmanager.units;

import java.util.List;

import io.firebus.utils.DataMap;
import io.redback.RedbackException;
import io.redback.managers.processmanager.Actionner;
import io.redback.managers.processmanager.Assignment;
import io.redback.managers.processmanager.Process;
import io.redback.managers.processmanager.ProcessInstance;
import io.redback.managers.processmanager.ProcessManager;
import io.redback.managers.processmanager.ProcessUnit;
import io.redback.managers.processmanager.Process;

public class ActionUnit extends ProcessUnit 
{
	protected String interactionCode;
	protected String action;
	protected String nextNode;

	public ActionUnit(ProcessManager pm, Process p, DataMap config) 
	{
		super(pm, p, config);
		interactionCode = config.getString("interaction");
		action = config.getString("action");
		nextNode = config.getString("nextnode");
	}
	
	public void execute(ProcessInstance pi) throws RedbackException
	{
		logger.finer("Starting Action node");
		Actionner actionner = new Actionner(pi);
		List<Assignment> assignments = processManager.getAssignments(actionner, null, null);
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
					processManager.processAction(actionner, assignment.pid, action, null);
				}
				else
				{
					logger.info("ActionUnit tried to process the interaction '" + interactionCode + "' with an invalid action '" + action + "'");
				}
			}
		}	

		pi.setCurrentNode(nextNode);
		logger.finer("Finished Action node");
	}

}
