package io.redback.managers.processmanager.js;

import java.util.Arrays;

import org.graalvm.polyglot.Value;
import org.graalvm.polyglot.proxy.ProxyArray;
import org.graalvm.polyglot.proxy.ProxyObject;

import io.redback.managers.processmanager.ProcessInstance;
import io.redback.utils.js.JSConverter;


public class ProcessInstanceJSWrapper implements ProxyObject
{
	protected ProcessInstance processInstance;
	protected String[] members = {"pid", "data"};
	
	public ProcessInstanceJSWrapper(ProcessInstance pi)
	{
		processInstance = pi;
	}
	


	public Object getMember(String name)
	{
		if(name.equals("pid"))
		{
			return processInstance.getId();
		}
		else if(name.equals("data"))
		{
			return JSConverter.toJS(processInstance.getData());
		}
		else
		{
			return null;
		}
	}


	public Object getMemberKeys() {
		return ProxyArray.fromArray(((Object[])members));
	}

	public boolean hasMember(String key) {
		return Arrays.asList(members).contains(key);
	}

	public void putMember(String key, Value value) {
		
	}

}
