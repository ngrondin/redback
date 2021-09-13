package io.redback.managers.processmanager.js;

import io.redback.exceptions.RedbackException;
import io.redback.managers.processmanager.ProcessInstance;
import io.redback.utils.js.ObjectJSWrapper;


public class ProcessInstanceJSWrapper extends ObjectJSWrapper {
	protected ProcessInstance processInstance;
	
	public ProcessInstanceJSWrapper(ProcessInstance pi)
	{
		super(new String[] {"pid", "data"});
		processInstance = pi;
	}
	
	public Object get(String name) throws RedbackException
	{
		if(name.equals("pid"))
		{
			return processInstance.getId();
		}
		else if(name.equals("data"))
		{
			return processInstance.getData();
		}
		else
		{
			return null;
		}
	}

}
