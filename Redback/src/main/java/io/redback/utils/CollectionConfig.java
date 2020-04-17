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
		DataMap out = (DataMap)in.getCopy(); //new DataMap();
		if(config != null && config.getObject("map") != null) {
			Iterator<String> it = config.getObject("map").keySet().iterator();
			while(it.hasNext()) {
				String canonicalkey = it.next();
				String inKey = getField(canonicalkey);
				if(inKey != null && out.containsKey(inKey) && !inKey.equals(canonicalkey)) {
					out.put(canonicalkey, out.get(inKey));
					out.remove(inKey);
				}
			}
		}
		return out;
	}
}
