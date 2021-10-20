package io.redback.managers.processmanager.units;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

import io.firebus.Payload;
import io.firebus.data.DataMap;
import io.firebus.script.ScriptContext;
import io.redback.exceptions.RedbackException;
import io.redback.managers.jsmanager.ExpressionMap;
import io.redback.managers.processmanager.Process;
import io.redback.managers.processmanager.ProcessInstance;
import io.redback.managers.processmanager.ProcessManager;
import io.redback.managers.processmanager.ProcessUnit;
import io.redback.security.Session;

public class FirebusRequestUnit extends ProcessUnit 
{
	protected String firebusServiceName;
	protected ExpressionMap inputExpressionMap;
	protected ExpressionMap outputExpressionMap;
	protected String nextNode;
	
	public FirebusRequestUnit(ProcessManager pm, Process p, DataMap config) throws RedbackException 
	{
		super(pm, p, config);
		try {
			processManager = pm;
			firebusServiceName = config.getString("service");
			inputExpressionMap = new ExpressionMap(processManager.getScriptFactory(), jsFunctionNameRoot + "_inexpr", config.get("data") != null ? config.getObject("data") : new DataMap());
			List<String> outVars = new ArrayList<String>(pm.getScriptVariableNames());
			outVars.add("result");
			outputExpressionMap = new ExpressionMap(processManager.getScriptFactory(), jsFunctionNameRoot + "_outexpr", config.get("outmap") != null ? config.getObject("outmap") : new DataMap());
			nextNode = config.getString("nextnode");
		} catch(Exception e) {
			throw new RedbackException("Error initialising firebus request unit", e);
		}
	}

	public void execute(ProcessInstance pi) throws RedbackException
	{
		logger.finer("Starting firebus call node");
		Session sysUserSession = pi.getOutboundActionner().getSession();
		ScriptContext context = pi.getScriptContext();
		DataMap data = inputExpressionMap.eval(context);
		Payload payload = new Payload(data);
		payload.metadata.put("token", sysUserSession.getToken());
		payload.metadata.put("session", sysUserSession.getId());
		try
		{
			if(logger.getLevel() == Level.FINEST) logger.finest("Calling " + processManager.getGlobalVariables().getString("rbobjectservice") + " " + data);
			Payload response = processManager.getFirebus().requestService(firebusServiceName, payload, 10000);
			DataMap respData = new DataMap(response.getString());
			context.put("result",respData);
			DataMap respOutput = outputExpressionMap.eval(context);
			if(logger.getLevel() == Level.FINEST) logger.finest("Output data was: " + respOutput);
			pi.setData(respOutput);
		} 
		catch (Exception e)
		{
			throw new RedbackException("Error executing firebus service '" + firebusServiceName + "' ",  e);
		}
		pi.setCurrentNode(nextNode);
		logger.finer("Finished firebus call node");
	}

}
