package io.redback.managers.objectmanagers.js;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.logging.Logger;

import org.graalvm.polyglot.Value;
import org.graalvm.polyglot.proxy.ProxyArray;
import org.graalvm.polyglot.proxy.ProxyExecutable;
import org.graalvm.polyglot.proxy.ProxyObject;


import io.redback.RedbackException;
import io.redback.managers.objectmanager.RedbackObject;
import io.redback.utils.js.JSConverter;

public class RedbackObjectJSWrapper implements ProxyObject
{
	private Logger logger = Logger.getLogger("io.redback");
	protected RedbackObject rbObject;
	protected String[] members = {"getRelated", "save", "getUpdatedAttributes"};
	
	public RedbackObjectJSWrapper(RedbackObject o)
	{
		rbObject = o;
	}
	
	public Object getMember(String name)
	{
		try
		{
			if(name.equals("getRelated"))
			{
				return new ProxyExecutable() {
					public Object execute(Value... arguments) {
						RedbackObject rbo = rbObject.getRelated(arguments[0].asString());
						if(rbo != null)
							return new RedbackObjectJSWrapper(rbo);
						return null;
					}
				};				
			}
			else if(name.equals("save"))
			{
				return new ProxyExecutable() {
					public Object execute(Value... arguments) {
						try {
							rbObject.save();
						} catch(Exception e) {
							logger.severe("Error saving objects : " + e.getMessage());
							throw new RuntimeException("Errror saving objects", e);
						}
						return null;
					}
				};				
			}
			else if(name.equals("getUpdatedAttributes"))
			{
				return new ProxyExecutable() {
					public Object execute(Value... arguments) {
						List<Object> list = new ArrayList<Object>(rbObject.getUpdatedAttributes());
						return ProxyArray.fromList(list);
					}
				};				
			}
			else if(rbObject.getObjectConfig().getScriptForEvent(name) != null)
			{
				return new ProxyExecutable() {
					public Object execute(Value... arguments) {
						try {
							rbObject.execute(name);
						} catch(Exception e) {
							logger.severe("Error executing object script : " + e.getMessage());
							throw new RuntimeException("Errror executing object script", e);
						}
						return null;
					}
				};				
			}
			else if(name.equals("objectname"))
			{
				return rbObject.getObjectConfig().getName();
			}
			else if(name.equals("uid"))
			{
				return rbObject.getUID().getObject();
			}
			else if(name.equals("domain"))
			{
				return rbObject.getDomain().getObject();
			}
			else
			{
				Object obj = rbObject.get(name).getObject();
				return JSConverter.toJS(obj);
			}
		} 
		catch (RedbackException e)
		{
			throw new RuntimeException("Error getting the Redback Object attribute '" + name + "'", e);
		}
	}

	public Object getMemberKeys() {
		return new HashSet<String>(Arrays.asList(members));
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

	public void putMember(String key, Value value) {
		try
		{
			rbObject.put(key, new io.redback.managers.objectmanager.Value(JSConverter.toJava(value)));
		} 
		catch (RedbackException e)
		{
			throw new RuntimeException("Error setting the Redback Object attribute '" + key + "' ", e);
		}		
	}


}
