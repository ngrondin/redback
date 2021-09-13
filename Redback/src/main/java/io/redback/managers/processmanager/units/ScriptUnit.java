package io.redback.managers.processmanager.units;

import java.util.Map;


import io.firebus.data.DataMap;
import io.firebus.script.Function;
import io.redback.managers.processmanager.ProcessInstance;
import io.redback.managers.processmanager.ProcessManager;
import io.redback.managers.processmanager.ProcessUnit;
import io.redback.exceptions.RedbackException;
import io.redback.managers.processmanager.Process;
import io.redback.utils.StringUtils;

public class ScriptUnit extends ProcessUnit 
{
	protected Function script;
	protected String nextNode;
	
	public ScriptUnit(ProcessManager pm, Process p, DataMap config) throws RedbackException 
	{
		super(pm, p, config);
		try
		{
			processManager = pm;
			nextNode = config.getString("nextnode");
			String source = StringUtils.unescape(config.getString("source")) + "\r\nreturn data;";
			script = pm.getScriptFactory().createFunction(jsFunctionNameRoot + "_script", pm.getScriptVariableNames().toArray(new String[] {}), source);
		}
		catch(Exception e)
		{
			throw new RedbackException("Error creating script unit id '" + getId() + "'", e);
		}
	}

	public void execute(ProcessInstance pi) throws RedbackException
	{
		logger.finer("Start executing script");
		Map<String, Object> context = pi.getScriptContext();
		try
		{
			DataMap piData = (DataMap)script.call(context);
			pi.setData(piData);
			pi.setCurrentNode(nextNode);
		} 
		catch (Exception e)
		{
			throw new RedbackException("Error executing script of node '" + name + "'", e);
		}		
		logger.finer("Finish executing script ");		
	}

}
