package io.redback.managers.processmanager.units;

import javax.script.Bindings;

import io.firebus.Payload;
import io.firebus.utils.DataMap;
import io.redback.RedbackException;
import io.redback.managers.processmanager.ProcessInstance;
import io.redback.managers.processmanager.ProcessManager;
import io.redback.managers.processmanager.ProcessUnit;
import io.redback.security.Session;
import io.redback.utils.ExpressionMap;

public class FirebusRequestUnit extends ProcessUnit 
{
	//private Logger logger = Logger.getLogger("io.redback");
	protected String firebusServiceName;
	protected ExpressionMap inputExpressionMap;
	protected ExpressionMap outputExpressionMap;
	protected String nextNode;
	
	public FirebusRequestUnit(ProcessManager pm, DataMap config) throws RedbackException 
	{
		super(pm, config);
		processManager = pm;
		firebusServiceName = config.getString("service");
		inputExpressionMap = new ExpressionMap(processManager.getScriptEngine(), config.get("data") != null ? config.getObject("data") : new DataMap());
		outputExpressionMap = new ExpressionMap(processManager.getScriptEngine(), config.get("outmap") != null ? config.getObject("outmap") : new DataMap());
		nextNode = config.getString("nextnode");
	}

	public void execute(ProcessInstance pi) throws RedbackException
	{
		logger.finer("Starting firebus call node");
		Session sysUserSession = processManager.getSystemUserSession(pi.getDomain());
		Bindings context = processManager.createScriptContext(pi);
		DataMap data = inputExpressionMap.eval(context);
		Payload payload = new Payload(data.toString());
		payload.metadata.put("token", sysUserSession.getToken());
		try
		{
			logger.finest("Calling " + processManager.getGlobalVariables().getString("rbobjectservice") + " " + payload.getString());
			Payload response = processManager.getFirebus().requestService(firebusServiceName, payload, 10000);
			DataMap respData = new DataMap(response.getString());
			context.put("result", respData);
			DataMap respOutput = outputExpressionMap.eval(context);
			logger.finest("Output data was: " + respOutput);
			pi.getData().merge(respOutput);
		} 
		catch (Exception e)
		{
			error("Error executing firebus service '" + firebusServiceName + "' ",  e);
		}
		pi.setCurrentNode(nextNode);
		logger.finer("Finished firebus call node");
	}

}
