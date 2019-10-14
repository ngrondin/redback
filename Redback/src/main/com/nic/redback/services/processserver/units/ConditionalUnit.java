package com.nic.redback.services.processserver.units;

import java.util.logging.Logger;

import javax.script.Bindings;
import javax.script.Compilable;
import javax.script.CompiledScript;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import com.nic.firebus.utils.FirebusDataUtil;
import com.nic.firebus.utils.JSONObject;
import com.nic.redback.RedbackException;
import com.nic.redback.security.Session;
import com.nic.redback.services.processserver.ProcessInstance;
import com.nic.redback.services.processserver.ProcessManager;
import com.nic.redback.services.processserver.ProcessUnit;
import com.nic.redback.services.processserver.js.ProcessManagerJSWrapper;
import com.nic.redback.utils.FirebusJSWrapper;
import com.nic.redback.utils.StringUtils;

public class ConditionalUnit extends ProcessUnit 
{
	private Logger logger = Logger.getLogger("com.nic.redback");
	protected CompiledScript script;
	protected String trueNode;
	protected String falseNode;
	
	public ConditionalUnit(ProcessManager pm, JSONObject config) throws RedbackException 
	{
		super(pm, config);
		processManager = pm;
		trueNode = config.getString("truenode");
		falseNode = config.getString("falsenode");
		ScriptEngine jsEngine = new ScriptEngineManager().getEngineByName("javascript");
		String source = "var result = (" + StringUtils.unescape(config.getString("condition")) + ");";
		try
		{
			script = ((Compilable)jsEngine).compile(source);
		}
		catch(ScriptException e)
		{
			error("Error creating script unit", e);
		}
	}

	public void execute(ProcessInstance pi, JSONObject result) throws RedbackException
	{
		Session sysUserSession = processManager.getSystemUserSession(pi.getDomain());
		String fileName = (String)script.getEngine().get(ScriptEngine.FILENAME);
		logger.info("Start executing condition : " + fileName);
		Bindings context = script.getEngine().createBindings();
		context.put("pid", pi.getId());
		context.put("data", FirebusDataUtil.convertDataObjectToJSObject(pi.getData()));
		context.put("pm", new ProcessManagerJSWrapper(processManager, sysUserSession));
		context.put("global", FirebusDataUtil.convertDataObjectToJSObject(processManager.getGlobalVariables()));
		context.put("firebus", new FirebusJSWrapper(processManager.getFirebus(), sysUserSession.getSessionId().toString()));
		try
		{
			script.eval(context);
			boolean bool = (Boolean)context.get("result");
			if(bool == true)
				pi.setCurrentNode(trueNode);
			else
				pi.setCurrentNode(falseNode);
		} 
		catch (ScriptException e)
		{
			error("Problem occurred executing a condition", e);
		}		
		logger.info("Finish executing condition : " + fileName);		
	}

}
