package io.redback.managers.processmanager.units;

import jdk.nashorn.api.scripting.JSObject;
import io.firebus.Payload;
import io.firebus.utils.DataMap;
import io.firebus.utils.FirebusDataUtil;
import io.redback.RedbackException;
import io.redback.managers.processmanager.Expression;
import io.redback.managers.processmanager.ProcessInstance;
import io.redback.managers.processmanager.ProcessManager;
import io.redback.managers.processmanager.ProcessUnit;
import io.redback.security.Session;

@SuppressWarnings("restriction")
public class FirebusCallUnit extends ProcessUnit 
{
	//private Logger logger = Logger.getLogger("com.nic.redback");
	protected String firebusServiceName;
	protected Expression payloadExpression;
	protected String nextNode;
	
	public FirebusCallUnit(ProcessManager pm, DataMap config) throws RedbackException 
	{
		super(pm, config);
		processManager = pm;
		firebusServiceName = config.getString("service");
		payloadExpression = new Expression(config.getString("payload"));
		nextNode = config.getString("nextnode");
	}

	public void execute(ProcessInstance pi, DataMap result) throws RedbackException
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
		payload.metadata.put("token", sysUserSession.getToken());
		try
		{
			processManager.getFirebus().requestService(firebusServiceName, payload, 10000);
		} 
		catch (Exception e)
		{
			error("Error executing firebus service '" + firebusServiceName + "' ",  e);
		}
		pi.setCurrentNode(nextNode);
		logger.info("Finished firebus call node");
	}

}
