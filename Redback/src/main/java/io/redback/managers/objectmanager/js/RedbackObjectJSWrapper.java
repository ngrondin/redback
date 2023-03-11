package io.redback.managers.objectmanager.js;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import io.firebus.script.Converter;
import io.firebus.script.exceptions.ScriptValueException;
import io.firebus.script.values.SNull;
import io.firebus.script.values.abs.SDynamicObject;
import io.firebus.script.values.abs.SValue;
import io.redback.exceptions.RedbackException;
import io.redback.managers.objectmanager.RedbackObject;
import io.redback.managers.objectmanager.Value;
import io.redback.utils.js.CallableJSWrapper;

public class RedbackObjectJSWrapper extends SDynamicObject
{
	protected RedbackObject rbObject;
	protected String[] members = {"getRelated", "save", "getUpdatedAttributes", "isNew", "isAttributeUpdated", "delete"};
	
	public RedbackObjectJSWrapper(RedbackObject o)
	{
		rbObject = o;
	}
	
	public RedbackObject getRedbackObject()
	{
		return rbObject;
	}
	
	public SValue getMember(String name)
	{
		if(name.equals("getRelated"))
		{
			return new CallableJSWrapper() {
				public Object call(Object... arguments) throws RedbackException {
					RedbackObject rbo = rbObject.getRelated((String)arguments[0]);
					if(rbo != null)
						return new RedbackObjectJSWrapper(rbo);
					return null;
				}
			};				
		}
		else if(name.equals("getUpdatedAttributes"))
		{
			return new CallableJSWrapper() {
				public Object call(Object... arguments) throws RedbackException {
					List<Object> list = new ArrayList<Object>(rbObject.getUpdatedAttributes());
					return list;
				}
			};				
		}
		else if(name.equals("isAttributeUpdated"))
		{
			return new CallableJSWrapper() {
				public Object call(Object... arguments) throws RedbackException {
					String attribute = (String)arguments[0];
					Boolean ret = false;
					if(rbObject.getUpdatedAttributes().contains(attribute))
						ret = true;
					return ret;
				}
			};				
		}			
		else if(name.equals("delete"))
		{
			return new CallableJSWrapper() {
				public Object call(Object... arguments) throws RedbackException {
					rbObject.delete();
					return null;
				}
			};				
		}			
		else if(rbObject.getObjectConfig().getScriptForEvent(name) != null)
		{
			return new CallableJSWrapper() {
				public Object call(Object... arguments) throws RedbackException {
					return rbObject.execute(name);
				}
			};				
		}
		else if(name.equals("objectname"))
		{
			return Converter.tryConvertIn(rbObject.getObjectConfig().getName());
		}
		else if(name.equals("uid"))
		{
			return Converter.tryConvertIn(rbObject.getUID().getObject());
		}
		else if(name.equals("domain"))
		{
			return Converter.tryConvertIn(rbObject.getDomain().getObject());
		}
		else if(name.equals("isNew"))
		{
			return Converter.tryConvertIn(rbObject.isNew());
		}
		else
		{
			try {
				Value val = rbObject.get(name);
				if(val != null) {
					return Converter.convertIn(val.getObject());
				} else {
					return SNull.get();
				}				
			} catch(Exception e) {
				throw new RuntimeException("Error converting attribute", e);
			}
		}
	}

	public String[] getMemberKeys() {
		List<Object> list = new ArrayList<Object>();
		for(int i = 0; i < members.length; i++)
			list.add(members[i]);
		list.add("objectname");
		list.add("uid");
		list.add("domain");
		list.addAll(rbObject.getObjectConfig().getAttributeNames());
		return list.toArray(new String[] {});
	}

	public boolean hasMember(String key) {
		if(Arrays.asList(members).contains(key)) {
			return true;
		} else if(rbObject.getObjectConfig().getScriptForEvent(key) != null) {
			return true;
		} else if(rbObject.getAttributeNames().contains(key)) {
			return true;
		} else if(key.equals("uid") || key.equals("objectname") || key.equals("domain")) {
			return true;
		} else {
			return false;
		}
	}

	public void putMember(String key, SValue value) throws ScriptValueException {
		try
		{
			rbObject.put(key, new io.redback.managers.objectmanager.Value(Converter.convertOut(value)), false);
		} 
		catch (Exception e)
		{
			String errMsg = "Error setting the Redback Object attribute '" + key + "' : " + constructErrorString(e);
			throw new ScriptValueException(errMsg, e);		
		}		
	}

	
	protected String constructErrorString(Throwable e) 
	{
		String ret = "";
		Throwable t = e;
		while(t != null) {
			if(ret.length() > 0)
				ret = ret + " : ";
			ret = ret + t.getMessage();
			t = t.getCause();
		}
		return ret;
	}

	public static List<RedbackObjectJSWrapper> convertList(List<RedbackObject> list) 
	{
		List<RedbackObjectJSWrapper> ret = new ArrayList<RedbackObjectJSWrapper>();
		for(RedbackObject rbo: list) 
			ret.add(new RedbackObjectJSWrapper(rbo));
		return ret;
	}
	
	public String toString() {
		return rbObject.getObjectConfig().getName() + ":" + rbObject.getUID();
	}
}
