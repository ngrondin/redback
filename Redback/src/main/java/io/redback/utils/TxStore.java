package io.redback.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TxStore<T> {

	protected Map<String, List<T>> all;
	protected Map<String, Map<String, T>> map;
	
	public TxStore() {
		all = new HashMap<String, List<T>>(); 
		map = new HashMap<String, Map<String, T>>();
	}
	
	public void add(String cat, String key, T item) {
		if(!all.containsKey(cat))
			all.put(cat, new ArrayList<T>());
		if(!map.containsKey(cat))
			map.put(cat, new HashMap<String, T>());
		if(map.get(cat).get(key) == null) {
			all.get(cat).add(item);
			map.get(cat).put(key, item);
		}
	}
	
	public void remove(String cat, String key) {
		if(all.containsKey(cat)) {
			Object o = map.get(cat).remove(key);
			if(o != null)
				all.get(cat).remove(o);
		}
	}

	public T get(String cat, String key) {
		return map.containsKey(cat) ? map.get(cat).get(key) : null;
	}
	
	public List<String> getKeys(String cat) {
		return map.containsKey(cat) ? new ArrayList<String>(map.keySet()) : null;
	}
	
	public List<T> getAll(String cat) {
		return all.containsKey(cat) ? all.get(cat) : new ArrayList<T>();
	}
	
	public void clear(String cat) {
		if(all.containsKey(cat)) all.remove(cat);
		if(map.containsKey(cat)) map.remove(cat);		map.clear();
	}
}
