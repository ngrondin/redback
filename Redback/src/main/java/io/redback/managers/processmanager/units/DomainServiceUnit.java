package io.redback.managers.processmanager.units;

import io.firebus.Payload;
import io.firebus.utils.DataMap;
import io.redback.RedbackException;
import io.redback.managers.processmanager.ExpressionMap;
import io.redback.managers.processmanager.ProcessInstance;
import io.redback.managers.processmanager.ProcessManager;
import io.redback.managers.processmanager.ProcessUnit;
import io.redback.security.Session;

public class DomainServiceUnit extends ProcessUnit 
{
	//private Logger logger = Logger.getLogger("io.redback");
	protected String domainServiceName;
	protected ExpressionMap inputExpressionMap;
	protected ExpressionMap outputExpressionMap;
	protected String nextNode;
	
	public DomainServiceUnit(ProcessManager pm, DataMap config) throws RedbackException 
	{
		super(pm, config);
		processManager = pm;
		domainServiceName = config.getString("service");
		inputExpressionMap = new ExpressionMap(config.get("data") != null ? config.getObject("data") : new DataMap());
		outputExpressionMap = new ExpressionMap(config.get("outmap") != null ? config.getObject("outmap") : new DataMap());
		nextNode = config.getString("nextnode");
	}

	public void execute(ProcessInstance pi) throws RedbackException
	{
		logger.info("Starting domain service call node");
		if(processManager.getDomainServiceName() != null)
		{
			Session sysUserSession = processManager.getSystemUserSession(pi.getDomain());
			DataMap data = inputExpressionMap.eval("data", pi.getData());
			DataMap req = new DataMap();
			req.put("service", domainServiceName);
			req.put("domain", pi.getDomain());
			req.put("data", data);
			Payload payload = new Payload();
			payload.setData(req.toString());
			payload.metadata.put("token", sysUserSession.getToken());
			try
			{
				logger.info("Calling " + processManager.getDomainServiceName() + " " + payload.getString());
				Payload response = processManager.getFirebus().requestService(processManager.getDomainServiceName(), payload, 10000);
				DataMap respData = new DataMap(response.getString());
				DataMap respOutput = outputExpressionMap.eval("result", respData);
				logger.fine("Output data was: " + respOutput);
				pi.getData().merge(respOutput);
			} 
			catch (Exception e)
			{
				error("Error executing domain service call ",  e);
			}
			logger.info("Finished domain service call node");
		}
		else
		{
			logger.info("No domain service defined");
		}
		pi.setCurrentNode(nextNode);
	}

}
