package io.redback.managers.objectmanager;

import java.util.HashMap;
import java.util.Map;

import io.firebus.data.DataList;
import io.firebus.data.DataMap;
import io.redback.security.Session;

public class Settings {
	protected Map<String, Map<String, Object>> settings;
	
	public Settings() {
		settings = new HashMap<String, Map<String, Object>>();

	}
	
	public void load(DataList list) {
		settings.clear();
		for(int i = 0; i < list.size(); i++) 
			updateSingleValue(list.getObject(i));
	}

	public void updateSingleValue(DataMap data) {
		String domain = data.getString("domain");
		String key = data.getString("key");
		Object value = data.get("value");
		updateSingleValue(domain, key, value);	
	}
	
	public void updateSingleValue(String domain, String key, Object value) {
		if(!settings.containsKey(domain)) settings.put(domain, new HashMap<String, Object>());
		settings.get(domain).put(key, value);		
	}
	
	public DataMap getSettingsContextForSession(Session session) {
		DataMap ret = new DataMap();
		for(String domain: settings.keySet()) {
			if(session.hasAccessToDomain(domain)) {
				Map<String, Object> domainSettings = settings.get(domain);
				for(String key: domainSettings.keySet()) {
					ret.put(key, domainSettings.get(key));
				}
			}
		}		
		return ret;
	}

}
