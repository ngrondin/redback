package com.nic.redback.services.processserver.units;

import jdk.nashorn.api.scripting.JSObject;

import com.nic.firebus.Payload;
import com.nic.firebus.utils.FirebusDataUtil;
import com.nic.firebus.utils.JSONObject;
import com.nic.redback.RedbackException;
import com.nic.redback.security.Session;
import com.nic.redback.services.processserver.Expression;
import com.nic.redback.services.processserver.ProcessInstance;
import com.nic.redback.services.processserver.ProcessManager;
import com.nic.redback.services.processserver.ProcessUnit;

public class FirebusCallUnit extends ProcessUnit 
{
	//private Logger logger = Logger.getLogger("com.nic.redback");
	protected String firebusServiceName;
	protected Expression payloadExpression;
	protected String nextNode;
	
	public FirebusCallUnit(ProcessManager pm, JSONObject config) throws RedbackException 
	{
		super(pm, config);
		processManager = pm;
		firebusServiceName = config.getString("service");
		payloadExpression = new Expression(config.getString("payload"));
		nextNode = config.getString("nextnode");
	}

	public void execute(ProcessInstance pi, JSONObject result) throws RedbackException
	{
		logger.info("Starting firebus call node");
		Session sysUserSession = processManager.getSystemUserSession(pi.getDomain());
		String payloadStr = null;
		Object jsData = payloadExpression.eval("data", pi.getData());
		if(jsData instanceof JSObject)
			payloadStr = (String)jsData;
		else if(jsData instanceof String)
			payloadStr = FirebusDataUtil.convertJSObjectToDataObject((JSObject)jsData).toString();
		Payload payload = new Payload(payloadStr);
		payload.metadata.put("sessionid", sysUserSession.getSessionId().toString());
		try
		{
			processManager.getFirebus().requestService(firebusServiceName, payload);
		} 
		catch (Exception e)
		{
			error("Error executing firebus service '" + firebusServiceName + "' ",  e);
		}
		pi.setCurrentNode(nextNode);
		logger.info("Finished firebus call node");
	}

}
