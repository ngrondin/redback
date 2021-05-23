package io.redback.managers.objectmanager.js;

import java.util.Arrays;
import java.util.List;

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
	protected ObjectManager objectManager;
	protected Session session;
	protected String[] members = {"getObject", "listObjects", "listAllObjects", "getObjectList", "getRelatedObjectList", "updateObject", "createObject", "deleteObject", "execute", "fork", "elevate", "iterate"};
	
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
						throw new RuntimeException("Error in getObject", e);
					}
				}
			};
		} else if(key.equals("getObjectList") || key.equals("listObjects")) {
			return new ProxyExecutable() {
				public Object execute(Value... arguments) {
					try {
						String objectName = arguments[0].asString();
						DataMap filter = arguments.length > 1 ? (DataMap)JSConverter.toJava(arguments[1]) : null;
						DataMap sort = arguments.length > 2 ? (DataMap)JSConverter.toJava(arguments[2]) : null;
						String search = arguments.length > 3 ? arguments[3].asString() : null;
						return JSConverter.toJS(objectManager.listObjects(session, objectName, filter, search, sort, false, 0, 50));
					} catch (Exception e) {
						throw new RuntimeException("Error in listObjects", e);
					}
				}
			};
		} else if(key.equals("listAllObjects")) {
			return new ProxyExecutable() {
				public Object execute(Value... arguments) {
					try {
						String objectName = arguments[0].asString();
						DataMap filter = arguments.length > 1 ? (DataMap)JSConverter.toJava(arguments[1]) : null;
						DataMap sort = arguments.length > 2 ? (DataMap)JSConverter.toJava(arguments[2]) : null;
						String search = arguments.length > 3 ? arguments[3].asString() : null;
						return JSConverter.toJS(objectManager.listObjects(session, objectName, filter, search, sort, false, 0, 5000));
					} catch (Exception e) {
						throw new RuntimeException("Error in listObjects", e);
					}
				}
			};
		} else if(key.equals("getRelatedObjectList")) {
			return new ProxyExecutable() {
				public Object execute(Value... arguments) {
					try {
						return JSConverter.toJS(objectManager.listRelatedObjects(session, arguments[0].asString(), arguments[1].asString(), arguments[2].asString(), (DataMap)JSConverter.toJava(arguments[3]), null, null, false));
					} catch (Exception e) {
						throw new RuntimeException("Error in getRelatedObjectList", e);
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
						throw new RuntimeException("Error in updateObject", e);
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
						throw new RuntimeException("Error in createObject", e);
					}
				}
			};
		} else if(key.equals("deleteObject")) {
			return new ProxyExecutable() {
				public Object execute(Value... arguments) {
					String objectName = arguments[0].asString(); 
					String uid = arguments[1].asString(); 
					try {
						objectManager.deleteObject(session, objectName, uid);
						return null;
					} catch (Exception e) {
						throw new RuntimeException("Error in deleteObject", e);
					}
				}
			};		
		} else if(key.equals("execute")) {
			return new ProxyExecutable() {
				public Object execute(Value... arguments) {
					String functionName = arguments[0].asString(); 
					DataMap param = arguments.length > 1 ? (DataMap)JSConverter.toJava(arguments[1]) : null;
					try {
						objectManager.executeFunction(session, functionName, param);
						return null;
					} catch (Exception e) {
						throw new RuntimeException("Error in execute", e);
					}
				}
			};
		} else if(key.equals("fork")) {
			return new ProxyExecutable() {
				public Object execute(Value... arguments) {
					RedbackObjectJSWrapper obj = arguments[0].asProxyObject();
					String function = arguments[1].asString(); 
					try {
						objectManager.fork(session, obj.getMember("objectname").toString(), obj.getMember("uid").toString(), function);
						return null;
					} catch (Exception e) {
						throw new RuntimeException("Error in fork", e);
					}
				}
			};
		} else if(key.equals("elevate")) {
			return new ProxyExecutable() {
				public Object execute(Value... arguments) {
					if(arguments.length > 0 && arguments[0].canExecute()) {
						try {
							objectManager.elevateSession(session);
							arguments[0].execute();
							objectManager.demoteSession(session);
							return null;
						} catch(Exception e) {
							throw new RuntimeException("Error executing elevated script");
						}
					} else {
						throw new RuntimeException("Requires an executable argument");
					}
				}
			};
		}
		else if(key.equals("iterate")) {
			return new ProxyExecutable() {
				public Object execute(Value... arguments) {
					String objectName = arguments[0].asString();
					DataMap filter = (DataMap)JSConverter.toJava(arguments[1]);
					if(arguments.length >=3 && arguments[2].canExecute()) {
						try {
							boolean hasMore = true;
							int page = 0;
							while(hasMore) {
								List<RedbackObject> list = objectManager.listObjects(session, objectName, filter, null, null, false, page, 50);
								for(RedbackObject object: list) {
									arguments[2].execute(JSConverter.toJS(object));
								}
								if(list.size() < 50) {
									hasMore = false;
								} else {
									page++;
								}
							}
						} catch(Exception e) {
							throw new RuntimeException("Error iterating through list");
						}
						
					} else {
						throw new RuntimeException("3rd argument needs to be executable");
					}
					return null;
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
