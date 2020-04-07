package io.redback.utils;

import java.util.Iterator;

import io.firebus.utils.DataMap;

public class CollectionConfig {
	
	protected DataMap config;
	
	public CollectionConfig(String n) {
		config = new DataMap();
		config.put("name", n);
	}
	
	public CollectionConfig(DataMap cfg) {
		config = cfg;
		if(config == null)
			config = new DataMap();
	}
	
	public String getName() {
		return config.getString("name");
	}
	
	public String getField(String field) {
		DataMap fieldMap = config.getObject("map");
		String out = null;
		if(fieldMap != null) 
			out = fieldMap.getString(field);
		if(out == null)
			out = field;
		return out;
	}

	public DataMap convertObjectToCanonical(DataMap in) {
		DataMap out = new DataMap();
		Iterator<String> it = in.keySet().iterator();
		while(it.hasNext()) {
			String key = it.next();
			out.put(getField(key), in.get(key));
		}
		return out;
	}
}
