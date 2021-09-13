package io.redback.managers.processmanager.units;

import java.util.Map;
import java.util.logging.Logger;

import io.firebus.Payload;
import io.firebus.data.DataMap;
import io.firebus.script.Expression;
import io.redback.managers.processmanager.ProcessInstance;
import io.redback.managers.processmanager.ProcessManager;
import io.redback.managers.processmanager.ProcessUnit;
import io.redback.exceptions.RedbackException;
import io.redback.managers.jsmanager.ExpressionMap;
import io.redback.managers.processmanager.Process;
import io.redback.security.Session;

public class RedbackObjectGetUnit extends ProcessUnit 
{
	private Logger logger = Logger.getLogger("io.redback.managers.processmanager");
	protected String objectName;
	protected Expression objectUIDExpression;
	protected ExpressionMap outputExpressionMap;
	protected String nextNode;
	
	public RedbackObjectGetUnit(ProcessManager pm, Process p, DataMap config) throws RedbackException 
	{
		super(pm, p, config);
		try {
			objectName = config.getString("object");
			objectUIDExpression = pm.getScriptFactory().createExpression(jsFunctionNameRoot + "_uidexpr", config.getString("uid"));
			outputExpressionMap = new ExpressionMap(processManager.getScriptFactory(), jsFunctionNameRoot + "_outexpr", config.get("outmap") != null ? config.getObject("outmap") : new DataMap());
			nextNode = config.getString("nextnode");
		}
		catch(Exception e)
		{
			throw new RedbackException("Error initialising object get unit", e);
		}
	}

	public void execute(ProcessInstance pi) throws RedbackException
	{
		logger.finer("Starting redback object get node");
		try {
			if(processManager.getObjectServiceName() != null)
			{
				Session sysUserSession = pi.getOutboundActionner().getSession();
				Map<String, Object> context = pi.getScriptContext();
				String objectUID = (String)objectUIDExpression.eval(context);
				DataMap req = new DataMap();
				req.put("action", "get");
				req.put("object", objectName);
				req.put("uid", objectUID);
				Payload payload = new Payload();
				payload.setData(req.toString());
				payload.metadata.put("token", sysUserSession.getToken());
				payload.metadata.put("session", sysUserSession.getId());
				payload.metadata.put("mime", "application/json");
				try
				{
					logger.finest("Calling redback object service " + processManager.getObjectServiceName() + " " + payload.getString());
					Payload response = processManager.getFirebus().requestService(processManager.getObjectServiceName(), payload, 10000);
					DataMap respData = new DataMap(response.getString());
					context.put("result", respData);
					DataMap respOutput = outputExpressionMap.eval(context);
					logger.finest("Output data was: " + respOutput);
					pi.setData(respOutput);
				} 
				catch (Exception e)
				{
					throw new RedbackException("Error getting Redback object '" + objectName + "'",  e);
				}
				logger.finer("Finished redback object get node");
			}
			else
			{
				logger.info("No object service defined");
			}
			pi.setCurrentNode(nextNode);
		} catch(Exception e) {
			throw new RedbackException("Error executing get unit", e);
		}
	}
}
