package io.redback.client.js;

import java.util.ArrayList;
import java.util.List;

import io.firebus.data.DataMap;
import io.firebus.script.Converter;
import io.firebus.script.values.abs.SDynamicObject;
import io.firebus.script.values.abs.SValue;
import io.redback.client.RedbackObjectRemote;
import io.redback.exceptions.RedbackException;
import io.redback.utils.js.CallableJSWrapper;

public class RedbackObjectRemoteJSWrapper extends SDynamicObject {
	protected RedbackObjectRemote rbObjectRemote;
	protected List<String> members;
	
	public RedbackObjectRemoteJSWrapper(RedbackObjectRemote o)
	{
		super();
		rbObjectRemote = o;
	}
	
	public SValue getMember(String name)  
	{
		if(name.equals("getRelated"))
		{
			return new CallableJSWrapper() {
				public Object call(Object... arguments) throws RedbackException {
					RedbackObjectRemote ror = rbObjectRemote.getRelated((String)arguments[0]);
					if(ror != null)
						return new RedbackObjectRemoteJSWrapper(ror);
					else
						return null;
				}
			};				
		}
		else if(name.equals("related"))
		{
			return new SDynamicObject() {
				public SValue getMember(String relatedname)  
				{
					try {
						RedbackObjectRemote relatedRor = rbObjectRemote.getRelated(relatedname);
						if(relatedRor != null)
							return new RedbackObjectRemoteJSWrapper(relatedRor);
					} catch(Exception e) { }
					return null;
				}
			};				
		}		
		else if(name.equals("data"))
		{
			return new SDynamicObject() {
				public SValue getMember(String attributename)  
				{
					try {
						return Converter.tryConvertIn(rbObjectRemote.getObject(attributename));
					} catch(Exception e) {
						return null;
					}
				}
			};				
		}			
		else if(name.equals("execute"))
		{
			return new CallableJSWrapper() {
				public Object call(Object... arguments) throws RedbackException {
					String function = (String)arguments[0];
					DataMap data = arguments.length > 1 ? (DataMap)(arguments[1]) : null;
					rbObjectRemote.execute(function, data);
					return null;
				}
			};				
		}
		else if(name.equals("uid"))
		{
			return Converter.tryConvertIn(rbObjectRemote.getUid());
		}
		else
		{
			try {
				return Converter.tryConvertIn(rbObjectRemote.getObject(name));
			} catch(RedbackException e) {
				return null;
			}
		}
	}

	public String[] getMemberKeys() {
		List<Object> list = new ArrayList<Object>();
		list.add("objectname");
		list.add("uid");
		list.add("domain");
		list.addAll(rbObjectRemote.getAttributeNames());
		list.add("getRelated");
		list.add("execute");
		return list.toArray(new String[] {});
	}

	public boolean hasMember(String key) {
		try {
			if(key.equals("execute") || key.equals("getRelated")) {
				return true;
			} else if(rbObjectRemote.hasAttribute(key)) {
				return true;
			} else if(key.equals("uid") || key.equals("objectname") || key.equals("domain")) {
				return true;
			} else {
				return false;
			}
		} catch(RedbackException e) {
			return false;
		}
	}

	public void putMember(String key, SValue value) {
		try
		{
			rbObjectRemote.set(key, Converter.convertOut(value));
		} 
		catch (Exception e)
		{
			String errMsg = "Error setting the Redback Object attribute '" + key + "'";
			throw new RuntimeException(errMsg, e);		
		}		
	}

	public static List<RedbackObjectRemoteJSWrapper> convertList(List<RedbackObjectRemote> list) 
	{
		List<RedbackObjectRemoteJSWrapper> ret = new ArrayList<RedbackObjectRemoteJSWrapper>();
		if(list != null)
			for(RedbackObjectRemote ror: list) 
				ret.add(new RedbackObjectRemoteJSWrapper(ror));
		return ret;
	}

}
