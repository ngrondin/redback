package io.redback.managers.processmanager.units;

import java.util.logging.Logger;

import javax.script.Bindings;

import io.firebus.Payload;
import io.firebus.utils.DataMap;
import io.redback.RedbackException;
import io.redback.managers.processmanager.ProcessInstance;
import io.redback.managers.processmanager.ProcessManager;
import io.redback.managers.processmanager.ProcessUnit;
import io.redback.security.Session;
import io.redback.utils.Expression;
import io.redback.utils.ExpressionMap;

public class RedbackObjectExecuteUnit extends ProcessUnit 
{
	private Logger logger = Logger.getLogger("io.redback.managers.processmanager");
	protected String objectName;
	protected Expression objectUIDExpression;
	protected String objectFunctionName;
	protected ExpressionMap inputExpressionMap;
	protected ExpressionMap outputExpressionMap;
	protected String nextNode;
	
	public RedbackObjectExecuteUnit(ProcessManager pm, DataMap config) throws RedbackException 
	{
		super(pm, config);
		processManager = pm;
		objectName = config.getString("object");
		objectUIDExpression = new Expression(pm.getScriptEngine(), config.getString("uid"));
		objectFunctionName = config.getString("function");
		inputExpressionMap = new ExpressionMap(pm.getScriptEngine(), config.get("data") != null ? config.getObject("data") : new DataMap());
		outputExpressionMap = new ExpressionMap(pm.getScriptEngine(), config.get("outmap") != null ? config.getObject("outmap") : new DataMap());
		nextNode = config.getString("nextnode");
	}

	public void execute(ProcessInstance pi) throws RedbackException
	{
		logger.finer("Starting redback object execute node");
		if(processManager.getObjectServiceName() != null)
		{
			Bindings context = processManager.createScriptContext(pi);
			Session sysUserSession = processManager.getSystemUserSession(pi.getDomain());
			DataMap functionParams = inputExpressionMap.eval(context);
			String objectUID = (String)objectUIDExpression.eval(context);
			DataMap req = new DataMap();
			req.put("action", "execute");
			req.put("object", objectName);
			req.put("uid", objectUID);
			req.put("function", objectFunctionName);
			req.put("data", functionParams);
			Payload payload = new Payload();
			payload.setData(req.toString());
			payload.metadata.put("token", sysUserSession.getToken());
			try
			{
				logger.finest("Calling redback object service " + processManager.getObjectServiceName() + " " + payload.getString());
				Payload response = processManager.getFirebus().requestService(processManager.getObjectServiceName(), payload, 10000);
				DataMap respData = new DataMap(response.getString());
				context.put("result", respData);
				DataMap respOutput = outputExpressionMap.eval(context);
				logger.finest("Output data was: " + respOutput);
				pi.getData().merge(respOutput);
			} 
			catch (Exception e)
			{
				error("Error executing function '" + objectFunctionName + "' on Redback object '" + objectName + "'",  e);
			}
			logger.finer("Finished redback object execute node");
		}
		else
		{
			logger.info("No object service defined");
		}
		pi.setCurrentNode(nextNode);
	}

}
