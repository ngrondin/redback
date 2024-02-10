package io.redback.utils;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import io.firebus.data.DataList;
import io.firebus.data.DataMap;
import io.redback.client.DataClient;
import io.redback.exceptions.RedbackException;

public class CollectionConfig {
	
	protected DataClient dataClient;
	protected DataMap config;
	
	public CollectionConfig(String n) {
		config = new DataMap();
		config.put("name", n);
	}
	
	public CollectionConfig(DataMap cfg) {
		init(null, cfg, null);
	}
	
	public CollectionConfig(DataMap cfg, String defaultName) {
		init(null, cfg, defaultName);
	}
	
	public CollectionConfig(DataClient dc, DataMap cfg, String defaultName) {
		init(dc, cfg, defaultName);
	}
		
		
	protected void init(DataClient dc, DataMap cfg, String defaultName) {
		dataClient = dc;
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
	
	public DataMap getData(DataMap canonicalFilter) throws RedbackException {
		return getData(canonicalFilter, 0, 50);
	}
	
	public DataMap getData(DataMap canonicalFilter, int page, int pageSize) throws RedbackException {
		DataMap filter = convertObjectToSpecific(canonicalFilter);
		DataMap resp = dataClient.getData(getName(), filter, null, page, pageSize);
		DataMap canonicalResp = null;
		if(resp.containsKey("result")) {
			DataList respList = resp.getList("result");
			DataList canonicalList = new DataList();
			for(int i = 0; i < respList.size(); i++) {
				DataMap obj = respList.getObject(i);
				canonicalList.add(convertObjectToCanonical(obj));
			}
			canonicalResp = new DataMap("result", canonicalList);
		} else {
			 canonicalResp = convertObjectToCanonical(resp);
		}
		return canonicalResp;
	}
	
	public DataMap putData(DataMap canonicalKey, DataMap canonicalData) throws RedbackException {
		DataMap key = convertObjectToSpecific(canonicalKey);
		DataMap data = convertObjectToSpecific(canonicalData);
		DataMap resp = dataClient.putData(getName(), key, data);
		DataMap canonicalResp = convertObjectToCanonical(resp);
		return canonicalResp;
	}
}
