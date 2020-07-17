package io.redback.test;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.firebus.Firebus;
import io.firebus.Payload;
import io.firebus.utils.DataMap;
import io.redback.RedbackException;

public abstract class RedbackTestUnit extends Thread {
	protected RedbackTestUnitListener listener;
	protected Firebus firebus;
	protected String objectService;
	protected String processService;
	protected Map<String, String> tokens;
	protected boolean destroyFirebus;
	
	public interface RedbackTestUnitListener {
		public void unitCompleted(RedbackTestUnit unit);
	}
	
	public RedbackTestUnit() {
		selfConfigure();
	}
	
	public RedbackTestUnit(RedbackTestUnitListener l) {
		listener = l;
		selfConfigure();
	}
	
	public RedbackTestUnit(RedbackTestUnitListener l, Firebus fb, String os, String ps, Map<String, String> t) {
		listener = l;
		firebus = fb;
		destroyFirebus = false;
		objectService = os;
		processService = ps;
		tokens = t;
		start();
	}
	
	protected void selfConfigure() {
		try {
			destroyFirebus = true;
			InputStream is = getClass().getClassLoader().getResourceAsStream("io/redback/test/RedbackTester.json");
			DataMap cfg = new DataMap(is);
			is.close();
			firebus = new Firebus();
			Thread.sleep(2000);
			objectService = cfg.getString("objectservice");
			processService = cfg.getString("processservice");
			tokens = new HashMap<String, String>();
			for(int i = 0; i < cfg.getList("tokens").size(); i++)
				tokens.put(cfg.getList("tokens").getObject(i).getString("username"), cfg.getList("tokens").getObject(i).getString("token"));
			start();
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	public void run() {
		try {
			test();
		} catch(Exception e) {
			e.printStackTrace();
		} finally {
			if(destroyFirebus)
				firebus.close();
			if(listener != null)
				listener.unitCompleted(this);
		}		
	}

	public abstract void test() throws RedbackException;
	
	protected RedbackObjectProxy getObject(String user, String objectname, String uid) throws RedbackException  {
		try {
			DataMap req = new DataMap();
			req.put("action", "get");
			req.put("object", objectname);
			req.put("uid", uid);
			req.put("options", new DataMap("addrelated", true));
			Payload reqP = new Payload(req.toString());
			reqP.metadata.put("token", tokens.get(user));
			Payload respP = firebus.requestService(objectService, reqP);
			DataMap resp = new DataMap(respP.getString());
			return new RedbackObjectProxy(this, resp);
		} catch(Exception e) {
			throw new RedbackException("Error getting object", e);
		}
	}

	protected List<RedbackObjectProxy> listObjects(String user, String objectname) throws RedbackException  {
		return listObjects(user, objectname, null, null);
	}

	protected List<RedbackObjectProxy> listObjects(String user, String objectname, String attribute, Object value) throws RedbackException  {
		try {
			DataMap req = new DataMap();
			req.put("action", "list");
			req.put("object", objectname);
			req.put("filter", attribute != null ? new DataMap(attribute, value) : new DataMap());
			req.put("options", new DataMap("addrelated", true));
			Payload reqP = new Payload(req.toString());
			reqP.metadata.put("token", tokens.get(user));
			Payload respP = firebus.requestService(objectService, reqP);
			DataMap resp = new DataMap(respP.getString());
			List<RedbackObjectProxy> list = new ArrayList<RedbackObjectProxy>();
			for(int i = 0; i < resp.getList("list").size(); i++) {
				DataMap item = resp.getList("list").getObject(i);
				list.add(new RedbackObjectProxy(this, item));
			}
			return list;
		} catch(Exception e) {
			throw new RedbackException("Error listing objects", e);
		}
	}
	
	protected RedbackObjectProxy createObject(String user, String objectname) throws RedbackException  {
		return createObject(user, objectname, null);
	}
	
	protected RedbackObjectProxy createObject(String user, String objectname, DataMap data) throws RedbackException  {
		try {
			DataMap req = new DataMap();
			req.put("action", "create");
			req.put("object", objectname);
			if(data != null) 
				req.put("data", data);
			req.put("options", new DataMap("addrelated", true));
			Payload reqP = new Payload(req.toString());
			reqP.metadata.put("token", tokens.get(user));
			Payload respP = firebus.requestService(objectService, reqP);
			DataMap resp = new DataMap(respP.getString());
			return new RedbackObjectProxy(this, resp);
		} catch(Exception e) {
			throw new RedbackException("Error creating object", e);
		}
	}
	
	protected RedbackObjectProxy updateObject(String user, String objectname, String uid, String attribute, Object value) throws RedbackException  {
		try {
			DataMap req = new DataMap();
			req.put("action", "update");
			req.put("object", objectname);
			req.put("uid", uid);
			req.put("data", new DataMap(attribute, value));
			req.put("options", new DataMap("addrelated", true));
			Payload reqP = new Payload(req.toString());
			reqP.metadata.put("token", tokens.get(user));
			Payload respP = firebus.requestService(objectService, reqP);
			DataMap resp = new DataMap(respP.getString());
			return new RedbackObjectProxy(this, resp);
		} catch(Exception e) {
			throw new RedbackException("Error updating object", e);
		}
	}		
	
	protected RedbackObjectProxy executeObject(String user, String objectname, String uid, String function) throws RedbackException  {
		try {
			DataMap req = new DataMap();
			req.put("action", "execute");
			req.put("object", objectname);
			req.put("uid", uid);
			req.put("function", function);
			req.put("options", new DataMap("addrelated", true));
			Payload reqP = new Payload(req.toString());
			reqP.metadata.put("token", tokens.get(user));
			Payload respP = firebus.requestService(objectService, reqP);
			DataMap resp = new DataMap(respP.getString());
			return new RedbackObjectProxy(this, resp);
		} catch(Exception e) {
			throw new RedbackException("Error executing object", e);
		}
	}	
	
	protected RedbackAssignmentProxy getAssignment(String user, String objectname, String uid) throws RedbackException  {
		try {
			DataMap req = new DataMap();
			req.put("action", "getassignments");
			DataMap filter = new DataMap();
			filter.put("data.objectname", objectname);
			filter.put("data.uid", uid);
			req.put("filter", filter);
			Payload reqP = new Payload(req.toString());
			reqP.metadata.put("token", tokens.get(user));
			Payload respP = firebus.requestService(processService, reqP);
			DataMap resp = new DataMap(respP.getString());
			if(resp.getList("result").size() > 0)
				return new RedbackAssignmentProxy(this, resp.getList("result").getObject(0), user);
			else
				return null;
		} catch(Exception e) {
			throw new RedbackException("Error getting assignment", e);
		}
	}	
	
	protected void actionAssignment(String user, String pid, String action) throws RedbackException  {
		try {
			DataMap req = new DataMap();
			req.put("action", "processaction");
			req.put("processaction", action);
			req.put("pid", pid);
			Payload reqP = new Payload(req.toString());
			reqP.metadata.put("token", tokens.get(user));
			Payload respP = firebus.requestService(processService, reqP);
			DataMap resp = new DataMap(respP.getString());
		} catch(Exception e) {
			throw new RedbackException("Error actionning process", e);
		}
	}		
}
