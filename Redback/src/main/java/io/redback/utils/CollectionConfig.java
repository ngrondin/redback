package io.redback.utils;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import io.firebus.data.DataMap;

public class CollectionConfig {
	
	protected DataMap config;
	
	public CollectionConfig(String n) {
		config = new DataMap();
		config.put("name", n);
	}
	
	public CollectionConfig(DataMap cfg) {
		init(cfg, null);
	}
	
	public CollectionConfig(DataMap cfg, String defaultName) {
		init(cfg, defaultName);
	}
		
	protected void init(DataMap cfg, String defaultName) {
		config = cfg;
		if(config == null)
			config = new DataMap();
		if(config.get("name") == null) 
			config.put("name", defaultName);
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

	public DataMap convertObjectToCanonical(DataMap specific) {
		DataMap canonical = new DataMap(); //(DataMap)specific.getCopy(); 
		List<String> specificKeysTranslated = new ArrayList<String>();
		if(config != null && config.getObject("map") != null) {
			DataMap map = config.getObject("map");
			Iterator<String> it = map.keySet().iterator();
			while(it.hasNext()) {
				String canonicalkey = it.next();
				String specificKey = getField(canonicalkey);
				specificKeysTranslated.add(specificKey);
				if(specific.containsKey(specificKey))
					canonical.put(canonicalkey, specific.get(specificKey));
			}			
		}
		Iterator<String> it2 = specific.keySet().iterator();
		while(it2.hasNext()) {
			String specificKey = it2.next();
			if(!specificKeysTranslated.contains(specificKey))
				canonical.put(specificKey, specific.get(specificKey));
		}
		return canonical;
	}
	
	public DataMap convertObjectToSpecific(DataMap canonical) {
		DataMap specific = new DataMap(); //(DataMap)specific.getCopy(); 
		List<String> canonicalKeysTranslated = new ArrayList<String>();
		if(config != null && config.getObject("map") != null) {
			DataMap map = config.getObject("map");
			Iterator<String> it = map.keySet().iterator();
			while(it.hasNext()) {
				String canonicalkey = it.next();
				String specificKey = getField(canonicalkey);
				canonicalKeysTranslated.add(canonicalkey);
				if(canonical.containsKey(canonicalkey))
					specific.put(specificKey, canonical.get(canonicalkey));
			}			
		}
		Iterator<String> it2 = canonical.keySet().iterator();
		while(it2.hasNext()) {
			String canonicalKey = it2.next();
			if(!canonicalKeysTranslated.contains(canonicalKey))
				specific.put(canonicalKey, canonical.get(canonicalKey));
		}
		return specific;
	}
}
