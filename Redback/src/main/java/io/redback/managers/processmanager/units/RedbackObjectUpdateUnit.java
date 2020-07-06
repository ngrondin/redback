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

public class RedbackObjectUpdateUnit extends ProcessUnit 
{
	private Logger logger = Logger.getLogger("io.redback.managers.processmanager");
	protected String objectName;
	protected Expression objectUIDExpression;
	protected ExpressionMap inputExpressionMap;
	protected ExpressionMap outputExpressionMap;
	protected String nextNode;
	
	public RedbackObjectUpdateUnit(ProcessManager pm, Process p, DataMap config) throws RedbackException 
	{
		super(pm, p, config);
		processManager = pm;
		objectName = config.getString("object");
		objectUIDExpression = new Expression(processManager.getJSManager(), jsFunctionNameRoot + "_uidexpr", pm.getScriptVariableNames(), config.getString("uid"));
		inputExpressionMap = new ExpressionMap(processManager.getJSManager(), jsFunctionNameRoot + "_inexpr", pm.getScriptVariableNames(), config.get("data") != null ? config.getObject("data") : new DataMap());
		List<String> outVars = new ArrayList<String>(pm.getScriptVariableNames());
		outVars.add("result");
		outputExpressionMap = new ExpressionMap(processManager.getJSManager(), jsFunctionNameRoot + "_outexpr", outVars, config.get("outmap") != null ? config.getObject("outmap") : new DataMap());
		nextNode = config.getString("nextnode");
	}

	public void execute(ProcessInstance pi) throws RedbackException
	{
		logger.finer("Starting redback object update node");
		if(processManager.getObjectServiceName() != null)
		{
			Session sysUserSession = processManager.getSystemUserSession(pi.getDomain());
			Map<String, Object> context = pi.getScriptContext();
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
				error("Error updating Redback object '" + objectName + "'",  e);
			}
			logger.finer("Finished redback object update node");
		}
		else
		{
			logger.info("No object service defined");
		}
		pi.setCurrentNode(nextNode);
		
	}

}
