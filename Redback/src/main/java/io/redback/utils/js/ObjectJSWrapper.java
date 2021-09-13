package io.redback.utils.js;

import java.util.Arrays;

import io.firebus.script.Converter;
import io.firebus.script.exceptions.ScriptException;
import io.firebus.script.values.abs.SObject;
import io.firebus.script.values.abs.SValue;
import io.redback.exceptions.RedbackException;

public abstract class ObjectJSWrapper extends SObject {
	protected String[] members;
	
	public ObjectJSWrapper(String[] m) {
		members = m;
	}
	
	public SValue getMember(String key) throws ScriptException {
		try {
			Object o = get(key);
			return Converter.tryConvertIn(o);
		} catch(RedbackException e) {
			throw new ScriptException("Error in getting member", e);
		}
	}
	
	public abstract Object get(String key) throws RedbackException;
	
	public String[] getMemberKeys() {
		return members;
	}
	
	public boolean hasMember(String key) {
		return Arrays.asList(members).contains(key);
	}
}
