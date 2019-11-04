package com.nic.redback.services.processserver.units;

import javax.script.Bindings;
import javax.script.Compilable;
import javax.script.CompiledScript;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import jdk.nashorn.api.scripting.JSObject;

import com.nic.firebus.utils.FirebusDataUtil;
import com.nic.firebus.utils.DataMap;
import com.nic.redback.RedbackException;
import com.nic.redback.security.Session;
import com.nic.redback.services.processserver.ProcessInstance;
import com.nic.redback.services.processserver.ProcessManager;
import com.nic.redback.services.processserver.ProcessUnit;
import com.nic.redback.services.processserver.js.ProcessManagerJSWrapper;
import com.nic.redback.utils.FirebusJSWrapper;
import com.nic.redback.utils.StringUtils;

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
		//jsEngine.put(ScriptEngine.FILENAME, scriptName);
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

	public void execute(ProcessInstance pi, DataMap result) throws RedbackException
	{
		Session sysUserSession = processManager.getSystemUserSession(pi.getDomain());
		logger.info("Start executing script");
		Bindings context = script.getEngine().createBindings();
		context.put("pid", pi.getId());
		context.put("data", FirebusDataUtil.convertDataObjectToJSObject(pi.getData()));
		context.put("pm", new ProcessManagerJSWrapper(processManager, sysUserSession));
		context.put("global", FirebusDataUtil.convertDataObjectToJSObject(processManager.getGlobalVariables()));
		context.put("firebus", new FirebusJSWrapper(processManager.getFirebus(), sysUserSession));
		context.put("result", FirebusDataUtil.convertDataObjectToJSObject(result));
		try
		{
			script.eval(context);
			pi.setCurrentNode(nextNode);
			JSObject piDataJS = (JSObject)context.get("data");
			pi.setData(FirebusDataUtil.convertJSObjectToDataObject(piDataJS));
			result = FirebusDataUtil.convertJSObjectToDataObject((JSObject)context.get("result"));
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
