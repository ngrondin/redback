package io.redback.managers.objectmanager.js;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

import org.graalvm.polyglot.Value;
import org.graalvm.polyglot.proxy.ProxyArray;
import org.graalvm.polyglot.proxy.ProxyExecutable;
import org.graalvm.polyglot.proxy.ProxyObject;

import io.redback.exceptions.RedbackException;
import io.redback.managers.objectmanager.RedbackObject;
import io.redback.utils.js.JSConverter;

public class RedbackObjectJSWrapper implements ProxyObject
{
	private Logger logger = Logger.getLogger("io.redback");
	protected RedbackObject rbObject;
	protected String[] members = {"getRelated", "save", "getUpdatedAttributes", "isNew", "isAttributeUpdated", "delete"};
	
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
							String errMsg = "Error saving objects : " + constructErrorString(e);
							logger.severe(errMsg);
							throw new RuntimeException(errMsg);
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
						ProxyArray pa = ProxyArray.fromList(list); 
						return pa;
					}
				};				
			}
			else if(name.equals("isAttributeUpdated"))
			{
				return new ProxyExecutable() {
					public Object execute(Value... arguments) {
						String attribute = arguments[0].asString();
						Boolean ret = false;
						if(rbObject.getUpdatedAttributes().contains(attribute))
							ret = true;
						return JSConverter.toJS(ret);
					}
				};				
			}			
			else if(name.equals("delete"))
			{
				return new ProxyExecutable() {
					public Object execute(Value... arguments) {
						try {
							rbObject.delete();
							return null;
						} catch(Exception e) {
							String errMsg = "Error deleting object : " + constructErrorString(e);
							logger.severe(errMsg);
							throw new RuntimeException(errMsg);
						}	
					}
				};				
			}			
			else if(rbObject.getObjectConfig().getScriptForEvent(name) != null)
			{
				return new ProxyExecutable() {
					public Object execute(Value... arguments) {
						try {
							return JSConverter.toJS(rbObject.execute(name));
						} catch(Exception e) {
							String errMsg = "Error executing object script : " + constructErrorString(e);
							logger.severe(errMsg);
							throw new RuntimeException(errMsg);
						}
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
			else if(name.equals("isNew"))
			{
				return rbObject.isNew();
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
		List<Object> list = new ArrayList<Object>();
		for(int i = 0; i < members.length; i++)
			list.add(members[i]);
		list.add("objectname");
		list.add("uid");
		list.add("domain");
		list.addAll(rbObject.getObjectConfig().getAttributeNames());
		return ProxyArray.fromList(list);
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
			String errMsg = "Error setting the Redback Object attribute '" + key + "' : " + constructErrorString(e);
			logger.severe(errMsg);
			throw new RuntimeException(errMsg);		
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

}
