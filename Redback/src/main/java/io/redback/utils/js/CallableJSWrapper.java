package io.redback.utils.js;

import io.firebus.script.Converter;
import io.firebus.script.exceptions.ScriptCallException;
import io.firebus.script.exceptions.ScriptPassthroughException;
import io.firebus.script.values.abs.SCallable;
import io.firebus.script.values.abs.SValue;
import io.redback.exceptions.RedbackException;

public abstract class CallableJSWrapper extends SCallable {
	
	public SValue call(SValue... arguments) throws ScriptCallException {
		try {
			Object[] javaArgs = new Object[arguments.length];
			for(int i = 0; i < arguments.length; i++)
				javaArgs[i] = Converter.convertOut(arguments[i]);
			Object ret = call(javaArgs);
			return Converter.convertIn(ret);
		} catch(Exception e) {
			throw new ScriptPassthroughException("Error in external call", e);
		}
	}

	public abstract Object call(Object... arguments) throws RedbackException;

}
