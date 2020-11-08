package io.redback.managers.processmanager.units;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import io.firebus.Payload;
import io.firebus.utils.DataMap;
import io.redback.RedbackException;
import io.redback.managers.processmanager.ProcessInstance;
import io.redback.managers.processmanager.ProcessManager;
import io.redback.managers.processmanager.ProcessUnit;
import io.redback.managers.jsmanager.ExpressionMap;
import io.redback.managers.processmanager.Process;
import io.redback.security.Session;
import io.redback.utils.js.JSConverter;

public class FirebusRequestUnit extends ProcessUnit 
{
	//private Logger logger = Logger.getLogger("io.redback");
	protected String firebusServiceName;
	protected ExpressionMap inputExpressionMap;
	protected ExpressionMap outputExpressionMap;
	protected String nextNode;
	
	public FirebusRequestUnit(ProcessManager pm, Process p, DataMap config) throws RedbackException 
	{
		super(pm, p, config);
		processManager = pm;
		firebusServiceName = config.getString("service");
		inputExpressionMap = new ExpressionMap(processManager.getJSManager(), jsFunctionNameRoot + "_inexpr", pm.getScriptVariableNames(), config.get("data") != null ? config.getObject("data") : new DataMap());
		List<String> outVars = new ArrayList<String>(pm.getScriptVariableNames());
		outVars.add("result");
		outputExpressionMap = new ExpressionMap(processManager.getJSManager(), jsFunctionNameRoot + "_outexpr", outVars, config.get("outmap") != null ? config.getObject("outmap") : new DataMap());
		nextNode = config.getString("nextnode");
	}

	public void execute(ProcessInstance pi) throws RedbackException
	{
		logger.finer("Starting firebus call node");
		Session sysUserSession = pi.getOutboundActionner().getSession();
		Map<String, Object> context = pi.getScriptContext();
		DataMap data = inputExpressionMap.eval(context);
		Payload payload = new Payload(data.toString());
		payload.metadata.put("token", sysUserSession.getToken());
		payload.metadata.put("session", sysUserSession.getId());
		try
		{
			logger.finest("Calling " + processManager.getGlobalVariables().getString("rbobjectservice") + " " + payload.getString());
			Payload response = processManager.getFirebus().requestService(firebusServiceName, payload, 10000);
			DataMap respData = new DataMap(response.getString());
			context.put("result", JSConverter.toJS(respData));
			DataMap respOutput = outputExpressionMap.eval(context);
			logger.finest("Output data was: " + respOutput);
			pi.setData(respOutput);
		} 
		catch (Exception e)
		{
			error("Error executing firebus service '" + firebusServiceName + "' ",  e);
		}
		pi.setCurrentNode(nextNode);
		logger.finer("Finished firebus call node");
	}

}
