package io.redback.managers.aimanager;

import java.util.ArrayList;
import java.util.List;

import io.firebus.data.DataMap;
import io.redback.client.RedbackObjectRemote;
import io.redback.security.Session;

public class SeqExContext {
	public Session session;
	public List<String> uiActions = new ArrayList<String>();
	public List<ObjectContext> objectContextStack = new ArrayList<ObjectContext>();
	public StringBuilder textResponse = new StringBuilder();
	
	public SeqExContext(Session s) {
		session = s;
	}
	
	public void addResponse(String s) {
		if(textResponse.length() > 0) 
			textResponse.append(" ");
		textResponse.append(s.trim());
	}
	
	public void pushObjectContext(ObjectContext oc) {
		objectContextStack.add(oc);
	}
	
	public void pushObjectContext(RedbackObjectRemote o, DataMap filter, String search) {
		pushObjectContext(new ObjectContext(o, filter, search));
	}
	
	public void pushObjectContext(List<RedbackObjectRemote> l, DataMap filter, String search) {
		pushObjectContext(new ObjectContext(l, filter, search));
	}
	
	public ObjectContext popObjectContext() {
		if(objectContextStack.size() > 1) {
	 		ObjectContext oc = objectContextStack.get(objectContextStack.size() - 1);
	 		objectContextStack.remove(oc);
	 		return oc;
		} else {
			return null;
		}
	}
	
	public ObjectContext getObjectContext() {
		if(objectContextStack.size() > 0) {
	 		return objectContextStack.get(objectContextStack.size() - 1);
		} else {
			return null;
		}
	}
}
