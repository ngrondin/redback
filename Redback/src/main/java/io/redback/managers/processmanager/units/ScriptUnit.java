package io.redback.managers.processmanager.units;

import javax.script.Bindings;
import javax.script.Compilable;
import javax.script.CompiledScript;
import javax.script.ScriptException;

import jdk.nashorn.api.scripting.JSObject;
import io.firebus.utils.DataMap;
import io.firebus.utils.FirebusDataUtil;
import io.redback.RedbackException;
import io.redback.managers.processmanager.ProcessInstance;
import io.redback.managers.processmanager.ProcessManager;
import io.redback.managers.processmanager.ProcessUnit;
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
		String source = StringUtils.unescape(config.getString("source"));
		try
		{
			script = ((Compilable)pm.getScriptEngine()).compile(source);
		}
		catch(ScriptException e)
		{
			error("Error creating script unit id '" + getId() + "'", e);
		}
	}

	public void execute(ProcessInstance pi) throws RedbackException
	{
		logger.finer("Start executing script");
		Bindings context = processManager.createScriptContext(pi);
		try
		{
			script.eval(context);
			pi.setCurrentNode(nextNode);
			JSObject piDataJS = (JSObject)context.get("data");
			pi.setData(FirebusDataUtil.convertJSObjectToDataObject(piDataJS));
		} 
		catch (ScriptException e)
		{
			error("Problem occurred executing script of node '" + name + "'", e);
		}		
		catch(NullPointerException e)
		{
			error("Null pointer exception in script of node '" + name + "'", e);
		}
		catch(RuntimeException e)
		{
			error("Problem occurred executing script of node '" + name + "'", e);
		}		
		logger.finer("Finish executing script ");		
	}

}
