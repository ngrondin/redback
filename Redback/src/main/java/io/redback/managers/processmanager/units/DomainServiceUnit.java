package io.redback.managers.processmanager.units;

import java.util.Map;

import io.firebus.Payload;
import io.firebus.data.DataMap;
import io.redback.managers.processmanager.ProcessInstance;
import io.redback.managers.processmanager.ProcessManager;
import io.redback.managers.processmanager.ProcessUnit;
import io.redback.exceptions.RedbackException;
import io.redback.managers.jsmanager.ExpressionMap;
import io.redback.managers.processmanager.Process;
import io.redback.security.Session;
import io.redback.utils.StringUtils;

public class DomainServiceUnit extends ProcessUnit 
{
	protected String functionName;
	protected boolean async;
	protected ExpressionMap inputExpressionMap;
	protected ExpressionMap outputExpressionMap;
	protected String nextNode;
	
	public DomainServiceUnit(ProcessManager pm, Process p, DataMap config) throws RedbackException 
	{
		super(pm, p, config);
		try {
			processManager = pm;
			functionName = config.getString("function");
			async = config.getBoolean("async");
			inputExpressionMap = new ExpressionMap(processManager.getScriptFactory(), jsFunctionNameRoot + "_inexpr", config.get("data") != null ? config.getObject("data") : new DataMap());
			outputExpressionMap = new ExpressionMap(processManager.getScriptFactory(), jsFunctionNameRoot + "_outexpr", config.get("outmap") != null ? config.getObject("outmap") : new DataMap());
			nextNode = config.getString("nextnode");
		} catch(Exception e) {
			throw new RedbackException("Error initialising domain unit", e);
		}
	}

	public void execute(ProcessInstance pi) throws RedbackException
	{
		logger.finer("Starting domain service call node");
		if(processManager.getDomainServiceName() != null)
		{
			Session sysUserSession = pi.getOutboundActionner().getSession();
			Map<String, Object> context = pi.getScriptContext();
			DataMap data = inputExpressionMap.eval(context);
			DataMap req = new DataMap();
			req.put("action", "execute");
			req.put("domain", pi.getDomain());
			req.put("name", functionName);
			req.put("param", data);
			req.put("async", async);
			Payload payload = new Payload();
			payload.setData(req.toString());
			payload.metadata.put("token", sysUserSession.getToken());
			payload.metadata.put("session", sysUserSession.getId());
			payload.metadata.put("mime", "application/json");
			try
			{
				logger.finest("Calling " + processManager.getDomainServiceName() + " " + payload.getString());
				Payload response = processManager.getFirebus().requestService(processManager.getDomainServiceName(), payload, 10000);
				DataMap respData = new DataMap(response.getString());
				context.put("result", respData);
				DataMap respOutput = outputExpressionMap.eval(context);
				logger.finest("Output data was: " + respOutput);
				pi.setData(respOutput);
			} 
			catch (Exception e)
			{
				logger.warning(StringUtils.rollUpExceptions(e));
			}
			logger.finer("Finished domain service call node");
		}
		else
		{
			logger.fine("No domain service defined");
		}
		pi.setCurrentNode(nextNode);
	}

}
