package com.nic.redback.services.processserver.units;

import jdk.nashorn.api.scripting.JSObject;

import com.nic.firebus.Payload;
import com.nic.firebus.utils.FirebusDataUtil;
import com.nic.firebus.utils.JSONObject;
import com.nic.redback.RedbackException;
import com.nic.redback.services.processserver.Expression;
import com.nic.redback.services.processserver.ProcessInstance;
import com.nic.redback.services.processserver.ProcessManager;
import com.nic.redback.services.processserver.ProcessUnit;

public class RedbackObjectExecuteUnit extends ProcessUnit 
{
	//private Logger logger = Logger.getLogger("com.nic.redback");
	protected String objectName;
	protected Expression objectUIDExpression;
	protected String objectFunctionName;
	protected Expression dataExpression;
	protected String nextNode;
	
	public RedbackObjectExecuteUnit(ProcessManager pm, JSONObject config) throws RedbackException 
	{
		super(pm, config);
		processManager = pm;
		objectName = config.getString("object");
		objectUIDExpression = new Expression(config.getString("uid"));
		objectFunctionName = config.getString("function");
		dataExpression = new Expression(config.getString("data"));
		nextNode = config.getString("nextnode");
	}

	public void execute(ProcessInstance pi) throws RedbackException
	{
		JSONObject data = FirebusDataUtil.convertJSObjectToDataObject((JSObject)dataExpression.eval(pi));
		String objectUID = (String)objectUIDExpression.eval(pi);
		JSONObject req = new JSONObject();
		req.put("action", "execute");
		req.put("object", objectName);
		req.put("uid", objectUID);
		req.put("function", objectFunctionName);
		req.put("data", data);
		Payload payload = new Payload();
		payload.setData(req.toString());
		payload.metadata.put("sessionid", processManager.getSystemUserSession().getSessionId().toString());
		try
		{
			processManager.getFirebus().requestService(processManager.getGlobalVariables().getString("rbobjectservice"), payload);
		} 
		catch (Exception e)
		{
			error("Error executing function '" + objectFunctionName + "' on Redback object '" + objectName + "'",  e);
		}
		pi.setCurrentNode(nextNode);
	}

}
