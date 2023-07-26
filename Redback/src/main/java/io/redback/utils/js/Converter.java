package io.redback.utils.js;

import io.firebus.script.exceptions.ScriptException;
import io.firebus.script.exceptions.ScriptValueException;
import io.firebus.script.values.abs.SValue;
import io.redback.managers.objectmanager.RedbackObject;
import io.redback.managers.objectmanager.js.RedbackObjectJSWrapper;

public class Converter {
	public static SValue tryConvertIn(Object o) {
		try {
			return convertIn(o);
		} catch(ScriptException e) {
			return null;
		}
	}

	public static SValue convertIn(Object o) throws ScriptValueException {
		if(o instanceof RedbackObject) {
			return new RedbackObjectJSWrapper((RedbackObject)o);
		} else {
			return io.firebus.script.Converter.convertIn(o);
		}
	}
	
	public static Object tryConvertOut(SValue v) {
		try {
			return convertOut(v);
		} catch(ScriptException e) {
			return null;
		}
	}
	
	public static Object convertOut(SValue v) throws ScriptException {
		if(v instanceof RedbackObjectJSWrapper) {
			return ((RedbackObjectJSWrapper)v).getRedbackObject();
		} else {
			return io.firebus.script.Converter.convertOut(v);
		}
	}
}
