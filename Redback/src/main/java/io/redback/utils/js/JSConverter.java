package io.redback.utils.js;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.graalvm.polyglot.Value;
import org.graalvm.polyglot.proxy.ProxyArray;
import org.graalvm.polyglot.proxy.ProxyObject;

import io.firebus.utils.DataEntity;
import io.firebus.utils.DataList;
import io.firebus.utils.DataLiteral;
import io.firebus.utils.DataMap;
import io.redback.managers.objectmanager.RedbackObject;
import io.redback.managers.objectmanagers.js.RedbackObjectJSWrapper;
import io.redback.managers.processmanager.ProcessInstance;
import io.redback.managers.processmanager.js.ProcessInstanceJSWrapper;

public class JSConverter {

	public static Object toJS(Object object) {
		if(object == null) {
			return null;
		} else if(object instanceof DataMap) {
			DataMap map = (DataMap)object;
			Map<String, Object> out = new HashMap<String, Object>();
			Iterator<String> it = map.keySet().iterator();
			while(it.hasNext()) {
				String key = it.next();
				DataEntity ent = map.get(key);
				Object o = JSConverter.toJS(ent);
				out.put(key, o);
			}			
			return ProxyObject.fromMap(out);
		} else if(object instanceof DataList) {
			DataList list = (DataList)object;
			List<Object> out = new ArrayList<Object>();
			for(int i = 0; i < list.size(); i++) {
				DataEntity ent = list.get(i);
				Object o = JSConverter.toJS(ent);
				out.add(o);
			}
			return ProxyArray.fromList(out);
		} else if(object instanceof List) {
			List<?> list = (List<?>)object;
			List<Object> out = new ArrayList<Object>();
			for(int i = 0; i < list.size(); i++) {
				Object o = JSConverter.toJS(list.get(i));
				out.add(o);
			}
			return ProxyArray.fromList(out);
		} else if(object instanceof DataLiteral) {
			return ((DataLiteral)object).getObject();
		} else if(object instanceof Date) {
			return new JSDate((Date)object);
		} else if(object instanceof RedbackObject) {
			return new RedbackObjectJSWrapper((RedbackObject)object);
		} else if(object instanceof ProcessInstance) {
			return new ProcessInstanceJSWrapper((ProcessInstance)object);
		} else {
			return object;
		}
	}
	
	public static Object toJava(Value value) {
		if(value == null) {
			return null;
		} else if(value.isInstant()) {
			Instant ins = value.asInstant();
			Date dt = Date.from(ins);
			return dt;
		} else if(value.hasArrayElements()) {
			DataList list = new DataList();
			for(int i = 0; i < value.getArraySize(); i++) {
				Value subValue = value.getArrayElement(i);
				Object obj = JSConverter.toJava(subValue);
				list.add(obj);
			}
			return list;
		} else if(value.hasMembers()) {
			DataMap map = new DataMap();
			Iterator<String> it = value.getMemberKeys().iterator();
			while(it.hasNext()) {
				String key = it.next();
				Value subValue = value.getMember(key);
				Object obj = JSConverter.toJava(subValue);
				map.put(key, obj);
			}
			return map;
		} else if(value.isBoolean()) {
			return value.asBoolean();
		} else if(value.isDate()) {
			return value.asDate();
		} else if(value.isNumber()) {
			if(value.fitsInInt()) 
				return value.asInt();
			else if(value.fitsInLong())
				return value.asLong();
			else
				return value.asDouble();
		} else if(value.isString()) {
			return value.asString();
		} else {
			return null;
		}
	}
	
}
