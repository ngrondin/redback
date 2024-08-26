package io.redback.managers.objectmanager.js;

import java.util.List;

import io.firebus.data.DataList;
import io.firebus.data.DataMap;
import io.firebus.script.Function;
import io.firebus.script.exceptions.ScriptException;
import io.redback.exceptions.RedbackException;
import io.redback.managers.objectmanager.ObjectManager;
import io.redback.managers.objectmanager.RedbackAggregate;
import io.redback.managers.objectmanager.RedbackObject;
import io.redback.security.Session;
import io.redback.utils.js.CallableJSWrapper;
import io.redback.utils.js.Converter;
import io.redback.utils.js.ObjectJSWrapper;
import io.redback.utils.stream.AccumulatingDataStream;

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
					if(arguments[0] != null && arguments[1] != null) {
						RedbackObject rbo = objectManager.getObject(session, arguments[0].toString(), arguments[1].toString());
						if(rbo != null)
							return new RedbackObjectJSWrapper(rbo);					
					}
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
					long page = arguments.length > 4 ? (long)arguments[4] : 0;
					long pageSize = arguments.length > 5 ? (long)arguments[5] : 50;
					List<RedbackObject> list = objectManager.listObjects(session, objectName, filter, search, sort, false, (int)page, (int)pageSize);
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
					int chunkSize = arguments.length > 4 ? (Integer)arguments[4] : 50;
					AccumulatingDataStream<RedbackObject> stream = new AccumulatingDataStream<RedbackObject>();
					objectManager.streamObjects(session, objectName, filter, search, sort, chunkSize, 0, stream);
					List<RedbackObject> list = stream.getList();
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
		} else if(key.equals("aggregateObjects")) {
			return new CallableJSWrapper() {
				public Object call(Object... arguments) throws RedbackException {
					String objectname = (String)arguments[0];
					DataMap filter = (DataMap)arguments[1];
					DataList tuple = (DataList)arguments[2];
					DataList metrics = (DataList)arguments[3];
					DataMap sort = (DataMap)arguments[4];
					try {
						List<RedbackAggregate> list = objectManager.aggregateObjects(session, objectname, filter, null, tuple, metrics, sort, null, true, 0, 5000);
						return Converter.convertIn(list);
					} catch(ScriptException e) {
						throw new RedbackException("Error in aggregateObjects", e);
					}
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
					String domain = arguments.length >= 3 ? (String)arguments[1] : null; 
					String uid = arguments.length >= 4 ? (String)arguments[2] : null;
					DataMap data = (DataMap)(arguments[arguments.length - 1]); 
					RedbackObject rbo = objectManager.createObject(session, objectName, uid, domain, data);
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
					Object ret = null;
					if(arguments.length == 1 && arguments[0] instanceof String) {
						ret = objectManager.executeFunction(session, (String)arguments[0], null);						
					} else if(arguments.length == 2 && arguments[0] instanceof String && arguments[1] instanceof DataMap) {
						ret = objectManager.executeFunction(session, (String)arguments[0], (DataMap)(arguments[1]));	
					}
					return ret;
				}
			};			
		} else if(key.equals("fork")) {
			return new CallableJSWrapper() {
				public Object call(Object... arguments) throws RedbackException {
					if(arguments[0] instanceof RedbackObjectJSWrapper) {
						RedbackObjectJSWrapper obj = (RedbackObjectJSWrapper)arguments[0];
						RedbackObject o = obj.getRedbackObject();
						String function = (String)arguments[1]; 
						DataMap param = arguments.length > 2 ? (DataMap)arguments[2] : null;
						objectManager.fork(session, o.getObjectConfig().getName(), o.getUID().getString(), function, param);
						return null;
					} else if(arguments[0] instanceof String) {
						String function = (String)arguments[0];
						DataMap param = arguments.length > 1 ? (DataMap)arguments[1] : null;
						objectManager.fork(session, null, null, function, param);
						return null;
					} else {
						throw new RuntimeException("Bad argument on call to fork");
					}
				}
			};
		} else if(key.equals("elevate")) {
			return new CallableJSWrapper() {
				public Object call(Object... arguments) throws RedbackException {
					if(arguments.length > 0 && arguments[0] instanceof Function) {
						Function function = (Function)arguments[0];
						try {
							objectManager.elevateSession(session);
							function.call();
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
		} else if(key.equals("iterate")) {
			return new CallableJSWrapper() {
				public Object call(Object... arguments) throws RedbackException {
					String objectName = (String)arguments[0];
					DataMap filter = (DataMap)(arguments[1]);
					if(arguments.length >= 3 && arguments[2] instanceof Function) {
						Function callable = (Function)arguments[2];
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
							throw new RuntimeException("Error iterating through list", e);
						}
						
					} else {
						throw new RuntimeException("3rd argument needs to be executable");
					}
					return null;
				}
			};
		} else if(key.equals("clearObjectConfig")) {
			return new CallableJSWrapper() {
				public Object call(Object... arguments) throws RedbackException {
					String objectName = (String)arguments[0]; 
					String domain = (String)arguments[1]; 
					objectManager.clearDomainObjectConfig(session, objectName, domain);
					return null;
				}
			};
		} else if(key.equals("clearScriptConfig")) {
			return new CallableJSWrapper() {
				public Object call(Object... arguments) throws RedbackException {
					String objectName = (String)arguments[0]; 
					String domain = (String)arguments[1]; 
					objectManager.clearDomainScriptConfig(session, objectName, domain);
					return null;
				}
			};
		}			
		return null;
	}
}
