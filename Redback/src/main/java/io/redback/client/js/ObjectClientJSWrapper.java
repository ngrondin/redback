package io.redback.client.js;

import java.util.List;

import io.firebus.data.DataList;
import io.firebus.data.DataMap;
import io.firebus.script.Function;
import io.redback.client.ObjectClient;
import io.redback.client.RedbackObjectRemote;
import io.redback.exceptions.RedbackException;
import io.redback.managers.objectmanager.requests.MultiRequest;
import io.redback.security.Session;
import io.redback.utils.js.CallableJSWrapper;
import io.redback.utils.js.ObjectJSWrapper;
import io.redback.utils.stream.ChunkProcessingDataStream;
import io.redback.utils.stream.ProcessingDataStream;

public class ObjectClientJSWrapper extends ObjectJSWrapper {
	protected ObjectClient objectClient;
	protected Session session;
	protected String domainLock;
	protected String[] members = {"getObject", "listObjects", "listAllObjects", "listObjects", "createObject", "updateObject", "execute", "multi", "aggregate"};

	public ObjectClientJSWrapper(ObjectClient oc, Session s)
	{
		super(new String[] {"getObject", "listObjects", "listAllObjects", "listObjects", "createObject", "execute"});
		objectClient = oc;
		session = s;
	}
	
	public ObjectClientJSWrapper(ObjectClient oc, Session s, String dl)
	{
		super(new String[] {"getObject", "listObjects", "listAllObjects", "listObjects", "createObject", "execute"});
		objectClient = oc;
		session = s;
		domainLock = dl;
	}
	
	public Object get(String key) {
		if(key.equals("getObject")) {
			return new CallableJSWrapper() {
				public Object call(Object... arguments) throws RedbackException {
					String objectname = (String)arguments[0];
					String uid = (String)arguments[1];
					RedbackObjectRemote ror = objectClient.getObject(session, objectname, uid);
					if(domainLock == null || (domainLock != null && ror.getDomain().equals(domainLock)))
						return new RedbackObjectRemoteJSWrapper(ror);
					else
						return null;
				}
			};
		} else if(key.equals("listObjects")) {
			return new CallableJSWrapper() {
				public Object call(Object... arguments) throws RedbackException {
					String objectname = (String)arguments[0];
					DataMap filter = (DataMap)(arguments[1]);
					String search = null;
					DataMap sort = null;
					boolean addRelated = false;
					if(arguments.length > 2) {
						if(arguments[2] instanceof DataMap) {
							sort = (DataMap)(arguments[2]);
							if(arguments.length > 3) {
								addRelated = (boolean)(arguments[3]);
							}
						} else if(arguments[2] instanceof String) {
							search = arguments[2].toString();
							if(arguments.length > 3) {
								sort = (DataMap)(arguments[2]);
								if(arguments.length > 4) {
									addRelated = (boolean)(arguments[4]);
								}
							}
						}
					}					
					if(domainLock != null)
						filter.put("domain", domainLock);
					List<RedbackObjectRemote> list = objectClient.listObjects(session, objectname, filter, search, sort, addRelated, false, 0, 50);
					return RedbackObjectRemoteJSWrapper.convertList(list);
				}
			};
		} else if(key.equals("listAllObjects")) {
			return new CallableJSWrapper() {
				public Object call(Object... arguments) throws RedbackException {
					String objectname = (String)arguments[0];
					DataMap filter = (DataMap)(arguments[1]);
					String search = null;
					DataMap sort = null;
					boolean addRelated = false;
					if(arguments.length > 2) {
						if(arguments[2] instanceof DataMap) {
							sort = (DataMap)(arguments[2]);
							if(arguments.length > 3) {
								addRelated = (boolean)(arguments[3]);
							}
						} else if(arguments[2] instanceof String) {
							search = arguments[2].toString();
							if(arguments.length > 3) {
								sort = (DataMap)(arguments[2]);
								if(arguments.length > 4) {
									addRelated = (boolean)(arguments[4]);
								}
							}
						}
					}
					if(domainLock != null)
						filter.put("domain", domainLock);
					List<RedbackObjectRemote> list = objectClient.listAllObjects(session, objectname, filter, search, sort, addRelated, false);
					return RedbackObjectRemoteJSWrapper.convertList(list);
				}
			};
		} else if(key.equals("streamObjects")) {
			return new CallableJSWrapper() {
				public Object call(Object... arguments) throws RedbackException {
					String objectname = (String)arguments[0];
					DataMap filter = (DataMap)(arguments[1]);
					DataMap sort = (DataMap)(arguments[2]);
					Function callable = (Function)arguments[3];
					ProcessingDataStream<RedbackObjectRemote> stream = new ProcessingDataStream<RedbackObjectRemote>(new ProcessingDataStream.Processor<RedbackObjectRemote>() {
						public void process(RedbackObjectRemote object) throws Exception {
							callable.call(new RedbackObjectRemoteJSWrapper(object));
						}});
					objectClient.streamObjects(session, objectname, filter, sort, false, 50, stream);
					stream.waitUntilDone();
					return null;
				}
			};
		} else if(key.equals("streamObjectChunks")) {
			return new CallableJSWrapper() {
				public Object call(Object... arguments) throws RedbackException {
					String objectname = (String)arguments[0];
					DataMap filter = (DataMap)(arguments[1]);
					DataMap sort = (DataMap)(arguments[2]);
					long chunkSize = (long)arguments[3];
					Function callable = (Function)arguments[4];
					ChunkProcessingDataStream<RedbackObjectRemote> stream = new ChunkProcessingDataStream<RedbackObjectRemote>((int)chunkSize, new ChunkProcessingDataStream.Processor<RedbackObjectRemote>() {
						public void process(List<RedbackObjectRemote> list) throws Exception {
							List<RedbackObjectRemoteJSWrapper> jsList = RedbackObjectRemoteJSWrapper.convertList(list);
							callable.call(jsList);
						}});
					objectClient.streamObjects(session, objectname, filter, sort, false, 50, stream);
					stream.waitUntilDone();
					return null;
				}
			};			
		} else if(key.equals("createObject")) {
			return new CallableJSWrapper() {
				public Object call(Object... arguments) throws RedbackException {
					String objectname = (String)arguments[0];
					String domain = arguments.length == 2 ? null : (String)arguments[1]; 
					DataMap data = (DataMap)(arguments.length == 2 ? arguments[1] : arguments[2]);
					if(domainLock != null)
						domain = domainLock;
					RedbackObjectRemote ror =  objectClient.createObject(session, objectname, domain, data, true);
					return new RedbackObjectRemoteJSWrapper(ror);
				}
			};
		} else if(key.equals("updateObject")) {
			return new CallableJSWrapper() {
				public Object call(Object... arguments) {
					String objectname = (String)arguments[0];
					String uid = (String)arguments[1]; 
					DataMap data = (DataMap)arguments[2];
					try
					{
						RedbackObjectRemote ror = objectClient.updateObject(session, objectname, uid, data, true);
						return new RedbackObjectRemoteJSWrapper(ror);
					}
					catch(Exception e)
					{
						throw new RuntimeException("Error updating remote object", e);
					}
				}
			};			
		} else if(key.equals("execute")) {
			return new CallableJSWrapper() {
				public Object call(Object... arguments) throws RedbackException {
					Object ret = null;
					if(arguments.length >= 3) {
						String objectname = (String)arguments[0];
						String uid = (String)arguments[1];
						String function = (String)arguments[2];
						DataMap data = arguments.length > 3 ? (DataMap)(arguments[3]) : null;
						ret = objectClient.execute(session, objectname, uid, function, data);						
					} else if(arguments.length >= 1) {
						String function = (String)arguments[0];
						DataMap data = arguments.length > 1 ? (DataMap)(arguments[1]) : null;
						ret = objectClient.execute(session, function, data);						
					}
					return ret;
				}
			};		
		} else if(key.equals("multi")) {
			return new CallableJSWrapper() {
				public Object call(Object... arguments) throws RedbackException {
					DataList list = (DataList)arguments[0];
					try
					{
						/*MultiResponse mr = */objectClient.multi(session, new MultiRequest(list));
						return null;//TODO Return a response for multi gets and lists
					}
					catch(Exception e)
					{
						throw new RuntimeException("Error executing function on remote object", e);
					}
				}
			};		
		} else if(key.equals("aggregate")) {
			return new CallableJSWrapper() {
				public Object call(Object... arguments) throws RedbackException {
					String objectname = (String)arguments[0];
					DataMap filter = (DataMap)arguments[1];
					DataList tuple = (DataList)arguments[2];
					DataList metrics = (DataList)arguments[3];
					DataMap sort = (DataMap)arguments[4];
					return RedbackAggregateRemoteJSWrapper.convertList(objectClient.aggregate(session, objectname, filter, null, tuple, metrics, sort, null, true, 0, 5000));
				}
			};		
		} else {
			return null;
		}
	}	
}
