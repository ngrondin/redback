package com.nic.redback.services.processserver.units;

import java.util.logging.Logger;

import com.nic.firebus.Payload;
import com.nic.firebus.utils.DataList;
import com.nic.firebus.utils.DataMap;
import com.nic.redback.RedbackException;
import com.nic.redback.security.Session;
import com.nic.redback.services.processserver.Expression;
import com.nic.redback.services.processserver.ProcessInstance;
import com.nic.redback.services.processserver.ProcessManager;
import com.nic.redback.services.processserver.ProcessUnit;

public class RedbackObjectExecuteUnit extends ProcessUnit 
{
	private Logger logger = Logger.getLogger("com.nic.redback.services.processserver");
	protected String objectName;
	protected Expression objectUIDExpression;
	protected String objectFunctionName;
	protected Expression functionParamsExpression;
	protected Expression outputMapExpression;
	protected String nextNode;
	
	public RedbackObjectExecuteUnit(ProcessManager pm, DataMap config) throws RedbackException 
	{
		super(pm, config);
		processManager = pm;
		objectName = config.getString("object");
		objectUIDExpression = new Expression(config.getString("uid"));
		objectFunctionName = config.getString("function");
		functionParamsExpression = new Expression(config.get("data") != null ? config.getString("data") : "{}");
		outputMapExpression = new Expression(config.get("outmap") != null ? config.getString("outmap") : "{}");
		nextNode = config.getString("nextnode");
	}

	public void execute(ProcessInstance pi, DataMap result) throws RedbackException
	{
		logger.info("Starting redback object execute node");
		Session sysUserSession = processManager.getSystemUserSession(pi.getDomain());
		DataMap functionParams = (DataMap)functionParamsExpression.eval("data", pi.getData());
		String objectUID = (String)objectUIDExpression.eval("data", pi.getData());
		DataMap req = new DataMap();
		req.put("action", "execute");
		req.put("object", objectName);
		req.put("uid", objectUID);
		req.put("function", objectFunctionName);
		req.put("data", functionParams);
		Payload payload = new Payload();
		payload.setData(req.toString());
		payload.metadata.put("token", sysUserSession.getToken());
		try
		{
			logger.info("Calling " + processManager.getGlobalVariables().getString("rbobjectservice") + " " + payload.getString());
			Payload response = processManager.getFirebus().requestService(processManager.getGlobalVariables().getString("rbobjectservice"), payload);
			DataMap respJSON = new DataMap(response.getString());
			//respJSON.remove("data");
			DataMap respOutput = (DataMap)outputMapExpression.eval("result", respJSON);
			logger.fine("Output data was: " + respOutput);
			pi.getData().merge(respOutput);
			if(result.get("rbobjectupdate") == null)
				result.put("rbobjectupdate", new DataList());
			result.getList("rbobjectupdate").add(new DataMap("{objectname:" + respJSON.getString("objectname") + ", uid:" + respJSON.getString("uid") + "}"));
		} 
		catch (Exception e)
		{
			error("Error executing function '" + objectFunctionName + "' on Redback object '" + objectName + "'",  e);
		}
		pi.setCurrentNode(nextNode);
		logger.info("Finished redback object execute node");
	}

}
