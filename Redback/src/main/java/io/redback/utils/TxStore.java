package io.redback.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TxStore<T> {

	protected Map<String, List<T>> all;
	protected Map<String, Map<String, T>> map;
	
	public TxStore() {
		all = new HashMap<String, List<T>>(); //new ArrayList<T>();
		//updated = new ArrayList<T>();
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
	
	/*public void updated(String key) {
		T item = map.get(key);
		updated(item);
	}
	
	public void updated(T item) {
		if(all.contains(item) && !updated.contains(item))
			updated.add(item);
	}
	
	public boolean isUpdated(String key) {
		T item = map.get(key);
		return isUpdated(item);
	}
	
	public boolean isUpdated(T item) {
		return updated.contains(item);
	}*/
	
	public T get(String cat, String key) {
		return map.containsKey(cat) ? map.get(cat).get(key) : null;
	}
	
	public List<T> getAll(String cat) {
		return all.containsKey(cat) ? all.get(cat) : new ArrayList<T>();
	}
	
	/*public List<T> getUpdated() {
		return updated;
	}*/
	
	/*public List<T> getCopyOfAll() {
		List<T> copy = new ArrayList<T>();
		for(T item : all) {
			copy.add(item);
		}
		return copy;
	}*/
}
