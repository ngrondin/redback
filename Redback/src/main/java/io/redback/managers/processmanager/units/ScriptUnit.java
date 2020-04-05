package io.redback.managers.processmanager.units;

import javax.script.Bindings;
import javax.script.Compilable;
import javax.script.CompiledScript;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import jdk.nashorn.api.scripting.JSObject;
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

public class ScriptUnit extends ProcessUnit 
{
	protected CompiledScript script;
	protected String nextNode;
	
	public ScriptUnit(ProcessManager pm, DataMap config) throws RedbackException 
	{
		super(pm, config);
		processManager = pm;
		nextNode = config.getString("nextnode");
		ScriptEngine jsEngine = new ScriptEngineManager().getEngineByName("javascript");
		String source = StringUtils.unescape(config.getString("source"));
		try
		{
			script = ((Compilable)jsEngine).compile(source);
		}
		catch(ScriptException e)
		{
			error("Error creating script unit id '" + getId() + "'", e);
		}
	}

	public void execute(ProcessInstance pi) throws RedbackException
	{
		Session sysUserSession = processManager.getSystemUserSession(pi.getDomain());
		logger.info("Start executing script");
		Bindings context = script.getEngine().createBindings();
		context.put("pid", pi.getId());
		context.put("data", FirebusDataUtil.convertDataObjectToJSObject(pi.getData()));
		context.put("pm", new ProcessManagerJSWrapper(processManager, pi));
		context.put("global", FirebusDataUtil.convertDataObjectToJSObject(processManager.getGlobalVariables()));
		context.put("firebus", new FirebusJSWrapper(processManager.getFirebus(), sysUserSession));
		try
		{
			script.eval(context);
			pi.setCurrentNode(nextNode);
			JSObject piDataJS = (JSObject)context.get("data");
			pi.setData(FirebusDataUtil.convertJSObjectToDataObject(piDataJS));
		} 
		catch (ScriptException e)
		{
			error("Problem occurred executing script of node " + nodeId, e);
		}		
		catch(NullPointerException e)
		{
			error("Null pointer exception in script of node " + nodeId, e);
		}
		catch(RuntimeException e)
		{
			error("Problem occurred executing script of node " + nodeId, e);
		}		
		logger.info("Finish executing script ");		
	}

}
