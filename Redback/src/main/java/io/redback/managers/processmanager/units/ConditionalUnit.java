package io.redback.managers.processmanager.units;

import java.util.logging.Logger;

import javax.script.Bindings;
import javax.script.Compilable;
import javax.script.CompiledScript;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import io.firebus.utils.DataMap;
import io.firebus.utils.FirebusDataUtil;
import io.redback.RedbackException;
import io.redback.managers.processmanager.ProcessInstance;
import io.redback.managers.processmanager.ProcessManager;
import io.redback.managers.processmanager.ProcessUnit;
import io.redback.managers.processmanager.js.ProcessManagerJSWrapper;
import io.redback.security.Session;
import io.redback.utils.FirebusJSWrapper;
import io.redback.utils.StringUtils;

public class ConditionalUnit extends ProcessUnit 
{
	private Logger logger = Logger.getLogger("com.nic.redback");
	protected CompiledScript script;
	protected String trueNode;
	protected String falseNode;
	
	public ConditionalUnit(ProcessManager pm, DataMap config) throws RedbackException 
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

	public void execute(ProcessInstance pi, DataMap result) throws RedbackException
	{
		Session sysUserSession = processManager.getSystemUserSession(pi.getDomain());
		String fileName = (String)script.getEngine().get(ScriptEngine.FILENAME);
		logger.info("Start executing condition : " + fileName);
		Bindings context = script.getEngine().createBindings();
		context.put("pid", pi.getId());
		context.put("data", FirebusDataUtil.convertDataObjectToJSObject(pi.getData()));
		context.put("pm", new ProcessManagerJSWrapper(processManager, sysUserSession));
		context.put("global", FirebusDataUtil.convertDataObjectToJSObject(processManager.getGlobalVariables()));
		context.put("firebus", new FirebusJSWrapper(processManager.getFirebus(), sysUserSession));
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
