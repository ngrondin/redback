package com.nic.redback.services.processserver.units;

import java.util.logging.Logger;

import javax.script.Bindings;
import javax.script.Compilable;
import javax.script.CompiledScript;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import com.nic.firebus.utils.JSONObject;
import com.nic.redback.RedbackException;
import com.nic.redback.security.UserProfile;
import com.nic.redback.services.processserver.ProcessInstance;
import com.nic.redback.services.processserver.ProcessManager;
import com.nic.redback.services.processserver.ProcessUnit;
import com.nic.redback.utils.StringUtils;

public class ScriptUnit extends ProcessUnit 
{
	private Logger logger = Logger.getLogger("com.nic.redback");
	protected CompiledScript script;
	protected String nextNode;
	
	public ScriptUnit(ProcessManager pm, JSONObject config) throws RedbackException 
	{
		super(pm, config);
		processManager = pm;
		nextNode = config.getString("next");
		ScriptEngine jsEngine = new ScriptEngineManager().getEngineByName("javascript");
		//jsEngine.put(ScriptEngine.FILENAME, scriptName);
		String source = StringUtils.unescape(config.getString("source"));
		try
		{
			script = ((Compilable)jsEngine).compile(source);
		}
		catch(ScriptException e)
		{
			error("Error creating script unit", e);
		}
	}

	public void execute(UserProfile up, ProcessInstance pi) throws RedbackException
	{
		String fileName = (String)script.getEngine().get(ScriptEngine.FILENAME);
		logger.info("Start executing script : " + fileName);
		Bindings context = script.getEngine().createBindings();
		context.put("pi", pi);
		context.put("pm", processManager);
		try
		{
			script.eval(context);
			pi.setCurrentNode(nextNode);
		} 
		catch (ScriptException e)
		{
			error("Problem occurred executing a script", e);
		}		
		logger.info("Finish executing script : " + fileName);		
	}

}
