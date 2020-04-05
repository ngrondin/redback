package io.redback.managers.processmanager.units;

import java.util.logging.Logger;

import io.firebus.Payload;
import io.firebus.utils.DataMap;
import io.redback.RedbackException;
import io.redback.managers.processmanager.Expression;
import io.redback.managers.processmanager.ExpressionMap;
import io.redback.managers.processmanager.ProcessInstance;
import io.redback.managers.processmanager.ProcessManager;
import io.redback.managers.processmanager.ProcessUnit;
import io.redback.security.Session;

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
		objectUIDExpression = new Expression(config.getString("uid"));
		objectFunctionName = config.getString("function");
		inputExpressionMap = new ExpressionMap(config.get("data") != null ? config.getObject("data") : new DataMap());
		outputExpressionMap = new ExpressionMap(config.get("outmap") != null ? config.getObject("outmap") : new DataMap());
		nextNode = config.getString("nextnode");
	}

	public void execute(ProcessInstance pi) throws RedbackException
	{
		logger.info("Starting redback object execute node");
		Session sysUserSession = processManager.getSystemUserSession(pi.getDomain());
		DataMap functionParams = inputExpressionMap.eval("data", pi.getData());
		String objectUID = (String)objectUIDExpression.eval("data", pi.getData());
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
			logger.info("Calling " + processManager.getGlobalVariables().getString("rbobjectservice") + " " + payload.getString());
			Payload response = processManager.getFirebus().requestService(processManager.getGlobalVariables().getString("rbobjectservice"), payload, 10000);
			DataMap respData = new DataMap(response.getString());
			DataMap respOutput = outputExpressionMap.eval("result", respData);
			logger.fine("Output data was: " + respOutput);
			pi.getData().merge(respOutput);
		} 
		catch (Exception e)
		{
			error("Error executing function '" + objectFunctionName + "' on Redback object '" + objectName + "'",  e);
		}
		pi.setCurrentNode(nextNode);
		logger.info("Finished redback object execute node");
	}

}
