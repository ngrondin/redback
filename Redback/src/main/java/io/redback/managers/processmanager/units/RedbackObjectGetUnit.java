package io.redback.managers.processmanager.units;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import io.firebus.Payload;
import io.firebus.utils.DataMap;
import io.redback.RedbackException;
import io.redback.managers.processmanager.ProcessInstance;
import io.redback.managers.processmanager.ProcessManager;
import io.redback.managers.processmanager.ProcessUnit;
import io.redback.managers.jsmanager.Expression;
import io.redback.managers.jsmanager.ExpressionMap;
import io.redback.managers.processmanager.Process;
import io.redback.security.Session;
import io.redback.utils.js.JSConverter;

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
		processManager = pm;
		objectName = config.getString("object");
		objectUIDExpression = new Expression(processManager.getJSManager(), jsFunctionNameRoot + "_uidexpr", pm.getScriptVariableNames(), config.getString("uid"));
		List<String> outVars = new ArrayList<String>(pm.getScriptVariableNames());
		outVars.add("result");
		outputExpressionMap = new ExpressionMap(processManager.getJSManager(), jsFunctionNameRoot + "_outexpr", outVars, config.get("outmap") != null ? config.getObject("outmap") : new DataMap());
		nextNode = config.getString("nextnode");
	}

	public void execute(ProcessInstance pi) throws RedbackException
	{
		logger.finer("Starting redback object get node");
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
				context.put("result", JSConverter.toJS(respData));
				DataMap respOutput = outputExpressionMap.eval(context);
				logger.finest("Output data was: " + respOutput);
				pi.setData(respOutput);
			} 
			catch (Exception e)
			{
				error("Error getting Redback object '" + objectName + "'",  e);
			}
			logger.finer("Finished redback object get node");
		}
		else
		{
			logger.info("No object service defined");
		}
		pi.setCurrentNode(nextNode);
	}
}
