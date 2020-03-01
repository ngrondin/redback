package io.redback.managers.objectmanagers.js;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;

import javax.script.ScriptException;

import jdk.nashorn.api.scripting.JSObject;
import jdk.nashorn.api.scripting.ScriptObjectMirror;
import io.firebus.utils.DataList;
import io.firebus.utils.DataMap;
import io.firebus.utils.FirebusJSArray;
import io.redback.RedbackException;
import io.redback.managers.objectmanager.ObjectManager;
import io.redback.managers.objectmanager.RedbackObject;
import io.redback.security.Session;
import io.redback.security.UserProfile;

public class ObjectManagerJSWrapper
{
	protected ObjectManager objectManager;
	protected Session session;
	
	public ObjectManagerJSWrapper(ObjectManager om, Session s)
	{
		objectManager = om;
		session = s;
	}
	
	public JSObject getObject(String objectName, String id) throws RedbackException
	{
		RedbackObject rbo = objectManager.getObject(session, objectName, id);
		if(rbo != null)
			return new RedbackObjectJSWrapper(rbo);
		else
			return null;
	}
	
	public JSObject getObjectList(String objectName, JSObject filterData) throws RedbackException
	{
		return convertToJSArray(objectManager.listObjects(session, objectName, convertToJSONObject(filterData), null, false));
	}
	
	public JSObject getObjectList(String objectName, String uid, String attributeName, JSObject filterData) throws RedbackException
	{
		return convertToJSArray(objectManager.listObjects(session, objectName, uid, attributeName, convertToJSONObject(filterData), null, false));
	}
	
	public JSObject updateObject(UserProfile userProfile, String objectName, String id, JSObject updateData) throws RedbackException, ScriptException
	{
		RedbackObject rbo = objectManager.updateObject(session, objectName, id, convertToJSONObject(updateData));
		if(rbo != null)
			return new RedbackObjectJSWrapper(rbo);
		else
			return null;
	}
	
	public JSObject createObject(String objectName, JSObject initialData) throws RedbackException, ScriptException
	{
		RedbackObject rbo = objectManager.createObject(session, objectName, null, null, convertToJSONObject(initialData));
		if(rbo != null)
			return new RedbackObjectJSWrapper(rbo);
		else
			return null;
	}
	
	protected DataMap convertToJSONObject(JSObject jso)
	{
		DataMap retObj = new DataMap();
		if(jso.getClassName().equals("Object"))
		{
			Iterator<String> it = jso.keySet().iterator();
			while(it.hasNext())
			{
				String key = it.next();
				Object childObj = jso.getMember(key);
				if(childObj instanceof JSObject)
				{
					JSObject childJSObject = (JSObject)childObj;
					if(childJSObject.getClassName().equals("Object"))
					{
						retObj.put(key, convertToJSONObject(childJSObject));
					}
					else if(childJSObject.getClassName().equals("Array"))
					{
						retObj.put(key, convertToJSONList(childJSObject));
					}
					else if(childJSObject.getClassName().equals("Date")  &&  childJSObject instanceof ScriptObjectMirror)
					{
						ScriptObjectMirror jsDate = (ScriptObjectMirror)childJSObject;
						long timestampLocalTime = (long) (double) jsDate.callMember("getTime"); 
						int timezoneOffsetMinutes = (int) (double) jsDate.callMember("getTimezoneOffset");
						retObj.put(key, new Date(timestampLocalTime + timezoneOffsetMinutes * 60 * 1000));
					}
				}
				else
				{
					retObj.put(key, childObj);
				}
			}
		}
		return retObj;
	}
	
	protected DataList convertToJSONList(JSObject jso)
	{
		DataList retList = new DataList();
		if(jso.getClassName().equals("Array"))
		{
			Iterator<Object> it = jso.values().iterator();
			while(it.hasNext())
			{
				Object childObj = it.next();
				if(childObj instanceof JSObject)
				{
					JSObject childJSObject = (JSObject)childObj;
					if(childJSObject.getClassName().equals("Object"))
					{
						retList.add(convertToJSONObject(childJSObject));
					}
					else if(childJSObject.getClassName().equals("Array"))
					{
						retList.add(convertToJSONList(childJSObject));
					}
					else if(childJSObject.getClassName().equals("Date")  &&  childJSObject instanceof ScriptObjectMirror)
					{
						ScriptObjectMirror jsDate = (ScriptObjectMirror)childJSObject;
						long timestampLocalTime = (long) (double) jsDate.callMember("getTime"); 
						int timezoneOffsetMinutes = (int) (double) jsDate.callMember("getTimezoneOffset");
						retList.add(new Date(timestampLocalTime + timezoneOffsetMinutes * 60 * 1000));
					}
				}
				else
				{
					retList.add(childObj);
				}
			}
		}
		return retList;
	}
	
	/*
	protected RedbackJSObject convertToJSObject(JSONObject json)
	{
		return null;
	}
	 */
	
	protected FirebusJSArray convertToJSArray(ArrayList<RedbackObject> list)
	{
		FirebusJSArray array = new FirebusJSArray();
		for(int i = 0; i < list.size(); i++)
		{
			RedbackObjectJSWrapper ow = new RedbackObjectJSWrapper(list.get(i));
			array.setSlot(i,  ow);
		}
		return array;
	}

}
