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

public class RedbackObjectGetUnit extends ProcessUnit 
{
	private Logger logger = Logger.getLogger("io.redback.managers.processmanager");
	protected String objectName;
	protected Expression objectUIDExpression;
	protected ExpressionMap outputExpressionMap;
	protected String nextNode;
	
	public RedbackObjectGetUnit(ProcessManager pm, DataMap config) throws RedbackException 
	{
		super(pm, config);
		processManager = pm;
		objectName = config.getString("object");
		objectUIDExpression = new Expression(config.getString("uid"));
		outputExpressionMap = new ExpressionMap(config.get("outmap") != null ? config.getObject("outmap") : new DataMap());
		nextNode = config.getString("nextnode");
	}

	public void execute(ProcessInstance pi) throws RedbackException
	{
		logger.info("Starting redback object get node");
		if(processManager.getObjectServiceName() != null)
		{
			Session sysUserSession = processManager.getSystemUserSession(pi.getDomain());
			String objectUID = (String)objectUIDExpression.eval("data", pi.getData());
			DataMap req = new DataMap();
			req.put("action", "get");
			req.put("object", objectName);
			req.put("uid", objectUID);
			Payload payload = new Payload();
			payload.setData(req.toString());
			payload.metadata.put("token", sysUserSession.getToken());
			try
			{
				logger.info("Calling redback object service " + processManager.getObjectServiceName() + " " + payload.getString());
				Payload response = processManager.getFirebus().requestService(processManager.getObjectServiceName(), payload, 10000);
				DataMap respData = new DataMap(response.getString());
				DataMap respOutput = outputExpressionMap.eval("result", respData);
				logger.fine("Output data was: " + respOutput);
				pi.getData().merge(respOutput);
			} 
			catch (Exception e)
			{
				error("Error getting Redback object '" + objectName + "'",  e);
			}
			logger.info("Finished redback object get node");
		}
		else
		{
			logger.info("No object service defined");
		}
		pi.setCurrentNode(nextNode);
	}
}
