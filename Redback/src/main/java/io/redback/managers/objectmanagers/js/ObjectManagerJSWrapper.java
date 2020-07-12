package io.redback.managers.objectmanagers.js;

import java.util.Arrays;
import java.util.logging.Logger;

import org.graalvm.polyglot.Value;
import org.graalvm.polyglot.proxy.ProxyArray;
import org.graalvm.polyglot.proxy.ProxyExecutable;
import org.graalvm.polyglot.proxy.ProxyObject;

import io.firebus.utils.DataMap;
import io.redback.managers.objectmanager.ObjectManager;
import io.redback.managers.objectmanager.RedbackObject;
import io.redback.security.Session;
import io.redback.utils.js.JSConverter;

public class ObjectManagerJSWrapper implements ProxyObject
{
	private Logger logger = Logger.getLogger("io.redback");
	protected ObjectManager objectManager;
	protected Session session;
	protected String[] members = {"getObject", "getObjectList", "getRelatedObjectList", "updateObject", "createObject", "execute"};
	
	public ObjectManagerJSWrapper(ObjectManager om, Session s)
	{
		objectManager = om;
		session = s;
	}

	public Object getMember(String key) {
		if(key.equals("getObject")) {
			return new ProxyExecutable() {
				public Object execute(Value... arguments) {
					try {
						RedbackObject rbo = objectManager.getObject(session, arguments[0].asString(), arguments[1].asString());
						if(rbo != null)
							return new RedbackObjectJSWrapper(rbo);
						else
							return null;
					} catch (Exception e) {
						logger.severe("Errror in getObject : " + e.getMessage());
						throw new RuntimeException("Errror in getObject", e);
					}
				}
			};
		} else if(key.equals("getObjectList")) {
			return new ProxyExecutable() {
				public Object execute(Value... arguments) {
					try {
						String objectName = arguments[0].asString();
						DataMap filter = arguments.length > 1 ? (DataMap)JSConverter.toJava(arguments[1]) : null;
						DataMap sort = arguments.length > 2 ? (DataMap)JSConverter.toJava(arguments[2]) : null;
						return JSConverter.toJS(objectManager.listObjects(session, objectName, filter, null, sort, false, 0, 50));
					} catch (Exception e) {
						logger.severe("Errror in getObjectList : " + e.getMessage());
						throw new RuntimeException("Errror in getObjectList", e);
					}
				}
			};
		} else if(key.equals("getRelatedObjectList")) {
			return new ProxyExecutable() {
				public Object execute(Value... arguments) {
					try {
						return JSConverter.toJS(objectManager.listRelatedObjects(session, arguments[0].asString(), arguments[1].asString(), arguments[2].asString(), (DataMap)JSConverter.toJava(arguments[3]), null, false));
					} catch (Exception e) {
						logger.severe("Errror in getRelatedObjectList : " + e.getMessage());
						throw new RuntimeException("Errror in getRelatedObjectList", e);
					}
				}
			};
		} else if(key.equals("updateObject")) {
			return new ProxyExecutable() {
				public Object execute(Value... arguments) {
					try {
						RedbackObject rbo = objectManager.updateObject(session, arguments[0].asString(), arguments[1].asString(), (DataMap)JSConverter.toJava(arguments[2]));
						if(rbo != null)
							return new RedbackObjectJSWrapper(rbo);
						else
							return null;
					} catch (Exception e) {
						logger.severe("Errror in updateObject : " + e.getMessage());
						throw new RuntimeException("Errror in updateObject", e);
					}
				}
			};
		} else if(key.equals("createObject")) {
			return new ProxyExecutable() {
				public Object execute(Value... arguments) {
					String objectName = arguments[0].asString(); 
					String domain = arguments.length == 2 ? null : arguments[1].asString(); 
					DataMap data = (DataMap)JSConverter.toJava(arguments.length == 2 ? arguments[1] : arguments[2]); 
					try {
						RedbackObject rbo = objectManager.createObject(session, objectName, null, domain, data);
						if(rbo != null)
							return new RedbackObjectJSWrapper(rbo);
						else
							return null;
					} catch (Exception e) {
						logger.severe("Errror in createObject : " + e.getMessage());
						throw new RuntimeException("Errror in createObject", e);
					}
				}
			};
		} else if(key.equals("execute")) {
			return new ProxyExecutable() {
				public Object execute(Value... arguments) {
					String functionName = arguments[0].asString(); 
					try {
						objectManager.executeFunction(session, functionName);
						return null;
					} catch (Exception e) {
						logger.severe("Error in execute : " + e.getMessage());
						throw new RuntimeException("Error in execute", e);
					}
				}
			};
		}
		return null;
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
