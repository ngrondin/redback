package io.redback.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TxStore<T> {

	protected List<T> all;
	protected List<T> updated;
	protected Map<String, T> map;
	
	public TxStore() {
		all = new ArrayList<T>();
		updated = new ArrayList<T>();
		map = new HashMap<String, T>();
	}
	
	public void add(String key, T item) {
		if(map.get(key) == null) {
			all.add(item);
			map.put(key, item);
		}
	}
	
	public void updated(String key) {
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
	}
	
	public T get(String key) {
		return map.get(key);
	}
	
	public List<T> getAll() {
		return all;
	}
	
	public List<T> getUpdated() {
		return updated;
	}
	
	public List<T> getCopyOfAll() {
		List<T> copy = new ArrayList<T>();
		for(T item : all) {
			copy.add(item);
		}
		return copy;
	}
}
