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

public class RedbackObjectUpdateUnit extends ProcessUnit 
{
	private Logger logger = Logger.getLogger("io.redback.managers.processmanager");
	protected String objectName;
	protected Expression objectUIDExpression;
	protected ExpressionMap inputExpressionMap;
	protected ExpressionMap outputExpressionMap;
	protected String nextNode;
	
	public RedbackObjectUpdateUnit(ProcessManager pm, DataMap config) throws RedbackException 
	{
		super(pm, config);
		processManager = pm;
		objectName = config.getString("object");
		objectUIDExpression = new Expression(processManager.getScriptEngine(), config.getString("uid"));
		inputExpressionMap = new ExpressionMap(processManager.getScriptEngine(), config.get("data") != null ? config.getObject("data") : new DataMap());
		outputExpressionMap = new ExpressionMap(processManager.getScriptEngine(), config.get("outmap") != null ? config.getObject("outmap") : new DataMap());
		nextNode = config.getString("nextnode");
	}

	public void execute(ProcessInstance pi) throws RedbackException
	{
		logger.info("Starting redback object update node");
		if(processManager.getObjectServiceName() != null)
		{
			Session sysUserSession = processManager.getSystemUserSession(pi.getDomain());
			Bindings context = processManager.createScriptContext(pi);
			DataMap updateData = inputExpressionMap.eval(context);
			String objectUID = (String)objectUIDExpression.eval(context);
			DataMap req = new DataMap();
			req.put("action", "update");
			req.put("object", objectName);
			req.put("uid", objectUID);
			req.put("data", updateData);
			Payload payload = new Payload();
			payload.setData(req.toString());
			payload.metadata.put("token", sysUserSession.getToken());
			try
			{
				logger.info("Calling redback object service " + processManager.getObjectServiceName() + " " + payload.getString());
				Payload response = processManager.getFirebus().requestService(processManager.getObjectServiceName(), payload, 10000);
				DataMap respData = new DataMap(response.getString());
				context.put("result", respData);
				DataMap respOutput = outputExpressionMap.eval(context);
				logger.fine("Output data was: " + respOutput);
				pi.getData().merge(respOutput);
			} 
			catch (Exception e)
			{
				error("Error updating Redback object '" + objectName + "'",  e);
			}
			logger.info("Finished redback object update node");
		}
		else
		{
			logger.info("No object service defined");
		}
		pi.setCurrentNode(nextNode);
		
	}

}
