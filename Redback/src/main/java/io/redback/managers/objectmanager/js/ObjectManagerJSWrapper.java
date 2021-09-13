package io.redback.managers.objectmanager.js;

import java.util.List;

import io.firebus.data.DataMap;
import io.firebus.script.Converter;
import io.firebus.script.exceptions.ScriptException;
import io.firebus.script.values.abs.SCallable;
import io.redback.exceptions.RedbackException;
import io.redback.managers.objectmanager.ObjectManager;
import io.redback.managers.objectmanager.RedbackObject;
import io.redback.security.Session;
import io.redback.utils.js.CallableJSWrapper;
import io.redback.utils.js.ObjectJSWrapper;

public class ObjectManagerJSWrapper extends ObjectJSWrapper
{
	protected ObjectManager objectManager;
	protected Session session;
	
	public ObjectManagerJSWrapper(ObjectManager om, Session s)
	{
		super(new String[] {"getObject", "listObjects", "listAllObjects", "getObjectList", "getRelatedObjectList", "updateObject", "createObject", "deleteObject", "execute", "fork", "elevate", "iterate"});
		objectManager = om;
		session = s;
	}

	public Object get(String key) {
		if(key.equals("getObject")) {
			return new CallableJSWrapper() {
				public Object call(Object... arguments) throws RedbackException {
					RedbackObject rbo = objectManager.getObject(session, (String)arguments[0], (String)arguments[1]);
					if(rbo != null)
						return new RedbackObjectJSWrapper(rbo);
					else
						return null;
				}
			};
		} else if(key.equals("getObjectList") || key.equals("listObjects")) {
			return new CallableJSWrapper() {
				public Object call(Object... arguments) throws RedbackException {
					String objectName = (String)arguments[0];
					DataMap filter = arguments.length > 1 ? (DataMap)(arguments[1]) : null;
					DataMap sort = arguments.length > 2 ? (DataMap)(arguments[2]) : null;
					String search = arguments.length > 3 ? (String)arguments[3] : null;
					List<RedbackObject> list = objectManager.listObjects(session, objectName, filter, search, sort, false, 0, 50);
					return RedbackObjectJSWrapper.convertList(list);
				}
			};
		} else if(key.equals("listAllObjects")) {
			return new CallableJSWrapper() {
				public Object call(Object... arguments) throws RedbackException {
					String objectName = (String)arguments[0];
					DataMap filter = arguments.length > 1 ? (DataMap)(arguments[1]) : null;
					DataMap sort = arguments.length > 2 ? (DataMap)(arguments[2]) : null;
					String search = arguments.length > 3 ? (String)arguments[3] : null;
					List<RedbackObject> list = objectManager.listObjects(session, objectName, filter, search, sort, false, 0, 5000);
					return RedbackObjectJSWrapper.convertList(list);
				}
			};
		} else if(key.equals("getRelatedObjectList")) {
			return new CallableJSWrapper() {
				public Object call(Object... arguments) throws RedbackException {
					String objectName = (String)arguments[0];
					String uid = (String)arguments[1];
					String attributeName = (String)arguments[2];
					DataMap filter = (DataMap)arguments[3];
					List<RedbackObject> list = objectManager.listRelatedObjects(session, objectName, uid, attributeName, filter, null, null, false);
					return RedbackObjectJSWrapper.convertList(list);
				}
			};
		} else if(key.equals("updateObject")) {
			return new CallableJSWrapper() {
				public Object call(Object... arguments) throws RedbackException {
					try {
						String objectName = (String)arguments[0];
						String uid = (String)arguments[1];
						DataMap data = (DataMap)arguments[2];
						RedbackObject rbo = objectManager.updateObject(session, objectName, uid, data);
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
			return new CallableJSWrapper() {
				public Object call(Object... arguments) throws RedbackException {
					String objectName = (String)arguments[0]; 
					String domain = arguments.length == 2 ? null : (String)arguments[1]; 
					DataMap data = (DataMap)(arguments.length == 2 ? arguments[1] : arguments[2]); 
					RedbackObject rbo = objectManager.createObject(session, objectName, null, domain, data);
					if(rbo != null)
						return new RedbackObjectJSWrapper(rbo);
					else
						return null;
				}
			};
		} else if(key.equals("deleteObject")) {
			return new CallableJSWrapper() {
				public Object call(Object... arguments) throws RedbackException {
					String objectName = (String)arguments[0]; 
					String uid = (String)arguments[1]; 
					objectManager.deleteObject(session, objectName, uid);
					return null;
				}
			};		
		} else if(key.equals("execute")) {
			return new CallableJSWrapper() {
				public Object call(Object... arguments) throws RedbackException {
					String functionName = (String)arguments[0]; 
					DataMap param = arguments.length > 1 ? (DataMap)(arguments[1]) : null;
					objectManager.executeFunction(session, functionName, param);
					return null;
				}
			};
		} else if(key.equals("fork")) {
			return new CallableJSWrapper() {
				public Object call(Object... arguments) throws RedbackException {
					RedbackObjectJSWrapper obj = (RedbackObjectJSWrapper)arguments[0];
					if(obj != null) {
						RedbackObject o = obj.getRedbackObject();
						String function = (String)arguments[1]; 
						objectManager.fork(session, o.getObjectConfig().getName(), o.getUID().getString(), function);
						return null;
					} else {
						throw new RedbackException("Fork argument must be an object");
					}
				}
			};
		} else if(key.equals("elevate")) {
			return new CallableJSWrapper() {
				public Object call(Object... arguments) throws RedbackException {
					if(arguments.length > 0 && arguments[0] instanceof SCallable) {
						SCallable callable = (SCallable)arguments[0];
						try {
							objectManager.elevateSession(session);
							callable.call();
							objectManager.demoteSession(session);
						} catch(ScriptException e) {
							throw new RedbackException("Error in elevate", e);
						}
						return null;
					} else {
						throw new RedbackException("Requires an executable argument");
					}
				}
			};
		}
		else if(key.equals("iterate")) {
			return new CallableJSWrapper() {
				public Object call(Object... arguments) throws RedbackException {
					String objectName = (String)arguments[0];
					DataMap filter = (DataMap)(arguments[1]);
					if(arguments.length >= 3 && arguments[2] instanceof SCallable) {
						SCallable callable = (SCallable)arguments[2];
						try {
							boolean hasMore = true;
							int page = 0;
							while(hasMore) {
								List<RedbackObject> list = objectManager.listObjects(session, objectName, filter, null, null, false, page, 50);
								for(RedbackObject object: list) {
									callable.call(Converter.convertIn(object));
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
}
