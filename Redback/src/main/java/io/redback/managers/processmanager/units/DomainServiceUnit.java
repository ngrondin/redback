package io.redback.managers.processmanager.units;

import io.firebus.data.DataMap;
import io.firebus.logging.Logger;
import io.redback.exceptions.RedbackException;
import io.redback.managers.jsmanager.ExpressionMap;
import io.redback.managers.processmanager.Process;
import io.redback.managers.processmanager.ProcessInstance;
import io.redback.managers.processmanager.ProcessManager;
import io.redback.managers.processmanager.ProcessUnit;

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
		Logger.warning("rb.process.domain", new DataMap("warning","deprecated", "process", pi.getProcessName()));
		/*Logger.finer("rb.process.domain.start", null);
		if(processManager.getDomainServiceName() != null)
		{
			Session sysUserSession = pi.getOutboundActionner().getSession();
			ScriptContext context = pi.getScriptContext();
			DataMap data = inputExpressionMap.eval(context);
			DataMap req = new DataMap();
			req.put("action", "execute");
			req.put("domain", pi.getDomain());
			req.put("name", functionName);
			req.put("param", data);
			req.put("async", async);
			Payload payload = new Payload(req);
			payload.metadata.put("token", sysUserSession.getToken());
			payload.metadata.put("session", sysUserSession.getId());
			payload.metadata.put("mime", "application/json");
			try
			{
				Payload response = processManager.getFirebus().requestService(processManager.getDomainServiceName(), payload, 10000);
				DataMap respData = new DataMap(response.getString());
				context.put("result", respData.containsKey("data") ? respData.get("data") : null);
				DataMap respOutput = outputExpressionMap.eval(context);
				pi.setData(respOutput);
			} 
			catch (Exception e)
			{
				Logger.warning("rb.process.domain", "Error executing domain function", e);
			}
			Logger.finer("rb.process.domain.finish", null);
		}*/
		pi.setCurrentNode(nextNode);
	}

}
