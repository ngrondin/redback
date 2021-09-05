package io.redback.utils.js;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.graalvm.polyglot.Value;
import org.graalvm.polyglot.proxy.ProxyArray;
import org.graalvm.polyglot.proxy.ProxyExecutable;
import org.graalvm.polyglot.proxy.ProxyObject;

import io.firebus.data.DataEntity;
import io.firebus.data.DataList;
import io.firebus.data.DataLiteral;
import io.firebus.data.DataMap;

public class ConfigJSWrapper implements ProxyObject
{
	protected DataMap map;
	protected String[] members = {"get", "getString", "getObject", "getList", "containsKey", "getNumber", "getBoolean"};
	
	public ConfigJSWrapper(DataMap m) {
		map = m;
	}
	
	public Object getMember(String key) {
		if(key.equals("get")) {
			return new ProxyExecutable() {
				public Object execute(Value... arguments) {
					return JSConverter.toJS(map.get(JSConverter.toJava(arguments[0])));
				}
			};
		} else if(key.equals("getString")) {
			return new ProxyExecutable() {
				public Object execute(Value... arguments) {
					return map.getString((String)JSConverter.toJava(arguments[0]));
				}
			};
		} else if(key.equals("getObject")) {
			return new ProxyExecutable() {
				public Object execute(Value... arguments) {
					return new ConfigJSWrapper(map.getObject((String)JSConverter.toJava(arguments[0])));
				}
			};
		} else if(key.equals("getList")) {
			return new ProxyExecutable() {
				public Object execute(Value... arguments) {
					List<Object> list = new ArrayList<Object>();
					DataList dl = map.getList((String)JSConverter.toJava(arguments[0]));
					for(int i = 0; i < dl.size(); i++) { 
						DataEntity ent = dl.get(i);
						if(ent instanceof DataMap)
							list.add(JSConverter.toJS(ent));
						else
							list.add(((DataLiteral)ent).getObject());
					}
					return ProxyArray.fromList(list);
				}
			};
		} else if(key.equals("containsKey")) {
			return new ProxyExecutable() {
				public Object execute(Value... arguments) {
					return map.containsKey((String)JSConverter.toJava(arguments[0]));
				}
			};
		} else if(key.equals("getNumber")) {
			return new ProxyExecutable() {
				public Object execute(Value... arguments) {
					return map.getNumber((String)JSConverter.toJava(arguments[0]));
				}
			};
		} else if(key.equals("getBoolean")) {
			return new ProxyExecutable() {
				public Object execute(Value... arguments) {
					return map.getBoolean((String)JSConverter.toJava(arguments[0]));
				}
			};
		} else {
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
