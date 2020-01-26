package io.redback.managers.processmanager.units;

import java.util.logging.Logger;

import io.firebus.Payload;
import io.firebus.utils.DataList;
import io.firebus.utils.DataMap;
import io.redback.RedbackException;
import io.redback.managers.processmanager.Expression;
import io.redback.managers.processmanager.ProcessInstance;
import io.redback.managers.processmanager.ProcessManager;
import io.redback.managers.processmanager.ProcessUnit;
import io.redback.security.Session;

public class RedbackObjectExecuteUnit extends ProcessUnit 
{
	private Logger logger = Logger.getLogger("io.redback.managers.processmanager");
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
			Payload response = processManager.getFirebus().requestService(processManager.getGlobalVariables().getString("rbobjectservice"), payload, 10000);
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
