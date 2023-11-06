package io.redback.managers.aimanager;

import io.redback.client.RedbackObjectRemote;

public class ObjectContext extends SEContextLevel {
	public String objectname;
	public String uid;
	public RedbackObjectRemote object;
	
	public ObjectContext(RedbackObjectRemote o) {
		object = o;
		objectname = o.getObjectName();
		uid = o.getUid();
	}
		
	public ObjectContext(String on, String u) {
		objectname = on;
		uid = u;
	}
}
