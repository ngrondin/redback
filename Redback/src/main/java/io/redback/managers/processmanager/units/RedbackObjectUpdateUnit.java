package io.redback.managers.processmanager.units;

import io.firebus.Payload;
import io.firebus.data.DataMap;
import io.firebus.logging.Logger;
import io.firebus.script.Expression;
import io.firebus.script.ScriptContext;
import io.redback.exceptions.RedbackException;
import io.redback.managers.jsmanager.ExpressionMap;
import io.redback.managers.processmanager.Process;
import io.redback.managers.processmanager.ProcessInstance;
import io.redback.managers.processmanager.ProcessManager;
import io.redback.managers.processmanager.ProcessUnit;
import io.redback.security.Session;

public class RedbackObjectUpdateUnit extends ProcessUnit 
{
	protected String objectName;
	protected Expression objectUIDExpression;
	protected ExpressionMap inputExpressionMap;
	protected ExpressionMap outputExpressionMap;
	protected String nextNode;
	
	public RedbackObjectUpdateUnit(ProcessManager pm, Process p, DataMap config) throws RedbackException 
	{
		super(pm, p, config);
		try {
			objectName = config.getString("object");
			objectUIDExpression = pm.getScriptFactory().createExpression(jsFunctionNameRoot + "_uidexpr", config.getString("uid"));
			inputExpressionMap = new ExpressionMap(processManager.getScriptFactory(), jsFunctionNameRoot + "_inexpr", config.get("data") != null ? config.getObject("data") : new DataMap());
			outputExpressionMap = new ExpressionMap(processManager.getScriptFactory(), jsFunctionNameRoot + "_outexpr", config.get("outmap") != null ? config.getObject("outmap") : new DataMap());
			nextNode = config.getString("nextnode");
		}
		catch(Exception e)
		{
			throw new RedbackException("Error initialising object update unit", e);
		}
	}

	public void execute(ProcessInstance pi) throws RedbackException
	{
		Logger.finer("rb.process.updateobject.start", null);
		try {
			if(processManager.getObjectServiceName() != null)
			{
				Session sysUserSession = pi.getOutboundActionner().getSession();
				ScriptContext context = pi.getScriptContext();
				DataMap updateData = inputExpressionMap.eval(context);
				String objectUID = (String)objectUIDExpression.eval(context);
				DataMap req = new DataMap();
				req.put("action", "update");
				req.put("object", objectName);
				req.put("uid", objectUID);
				req.put("data", updateData);
				Payload payload = new Payload(req);
				payload.metadata.put("token", sysUserSession.getToken());
				payload.metadata.put("session", sysUserSession.getId());
				payload.metadata.put("mime", "application/json");
				try
				{
					Payload response = processManager.getFirebus().requestService(processManager.getObjectServiceName(), payload, 10000);
					DataMap respData = new DataMap(response.getString());
					context.put("result", respData);
					DataMap respOutput = outputExpressionMap.eval(context);
					pi.setData(respOutput);
				} 
				catch (Exception e)
				{
					throw new RedbackException("Error updating Redback object '" + objectName + "'",  e);
				}
				Logger.finer("rb.process.updateobject.finish", null);

			}
			pi.setCurrentNode(nextNode);
		} catch(Exception e) {
			throw new RedbackException("Error executing update unit", e);
		}
	}

}
