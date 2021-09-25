package io.redback.managers.objectmanager.requests;

import io.firebus.data.DataMap;

public abstract class ObjectRequest {
	public boolean addRelated;
	public boolean addValidation;

	public ObjectRequest(DataMap data) {
		DataMap options = data.getObject("options");
		if(options != null)
		{
			addValidation = options.getBoolean("addvalidation");
			addRelated = options.getBoolean("addrelated");
		}
	}
	
	public ObjectRequest(boolean ar, boolean av) {
		addRelated = ar;
		addValidation = av;
	}
	
	public abstract DataMap getDataMap();
}
