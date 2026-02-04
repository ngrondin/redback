package io.redback.managers.processmanager.units;

import io.firebus.data.DataEntity;
import io.firebus.data.DataMap;
import io.firebus.logging.Logger;
import io.firebus.script.Expression;
import io.firebus.script.ScriptContext;
import io.redback.client.ObjectClient;
import io.redback.client.RedbackObjectRemote;
import io.redback.exceptions.RedbackException;
import io.redback.managers.jsmanager.ExpressionMap;
import io.redback.managers.processmanager.Process;
import io.redback.managers.processmanager.ProcessInstance;
import io.redback.managers.processmanager.ProcessManager;
import io.redback.managers.processmanager.ProcessUnit;
import io.redback.security.Session;

public class RedbackObjectExecuteUnit extends ProcessUnit 
{
	protected String objectName;
	protected Expression objectUIDExpression;
	protected String functionName;
	protected ExpressionMap inputExpressionMap;
	protected ExpressionMap outputExpressionMap;
	protected String nextNode;
	protected ObjectClient objectClient;
	
	public RedbackObjectExecuteUnit(ProcessManager pm, Process p, DataMap config) throws RedbackException 
	{
		super(pm, p, config);
		try {
			objectName = config.getString("object");
			objectUIDExpression = pm.getScriptFactory().createExpression(jsFunctionNameRoot + "_uidexpr", config.getString("uid"));
			functionName = config.getString("function");
			inputExpressionMap = new ExpressionMap(pm.getScriptFactory(), jsFunctionNameRoot + "_inexpr", config.get("data") != null ? config.getObject("data") : new DataMap());
			outputExpressionMap = new ExpressionMap(pm.getScriptFactory(), jsFunctionNameRoot + "_outexpr", config.get("outmap") != null ? config.getObject("outmap") : new DataMap());
			nextNode = config.getString("nextnode");
			objectClient = new ObjectClient(processManager.getFirebus(), processManager.getObjectServiceName());
		}
		catch(Exception e)
		{
			throw new RedbackException("Error initialising object execute unit", e);
		}
	}

	public void execute(ProcessInstance pi) throws RedbackException
	{
		Logger.finer("rb.process.objectexecute.start");
		try {
			if(processManager.getObjectServiceName() != null)
			{
				ScriptContext context = pi.getScriptContext();
				Session sysUserSession = pi.getOutboundActionner().getSession();
				DataMap functionParams = inputExpressionMap.eval(context);
				String objectUID = (String)objectUIDExpression.eval(context);
				if(objectName != null && objectUID != null) {
					RedbackObjectRemote ror = objectClient.execute(sysUserSession, objectName, objectUID, functionName, functionParams);
					context.put("result", ror.data);
				} else {
					DataEntity resp = objectClient.execute(sysUserSession, functionName, functionParams);
					context.put("result", resp);
				}
				DataMap respOutput = outputExpressionMap.eval(context);
				pi.setData(respOutput);				
				Logger.finer("rb.process.objectexecute.finish");
			}
			pi.setCurrentNode(nextNode);
		} catch(Exception e) {
			throw new RedbackException("Error executing execution unti", e);
		}
	}

}
