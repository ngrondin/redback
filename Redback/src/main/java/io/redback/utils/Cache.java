package io.redback.utils;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class Cache<T> {
	
	protected Map<String, CacheEntry<T>> cachedEntries;
	protected long lastClean;
	protected long timeout;
	
	public Cache(long to) {
		cachedEntries = new HashMap<String, CacheEntry<T>>();
		timeout = to;
		lastClean = System.currentTimeMillis();
		if(timeout <= 0) 
			timeout = 120000;
	}
	
	public CacheEntry<T> getEntry(String key) {
		clean();
		CacheEntry<T> ce = cachedEntries.get(key);
		if(ce != null && ce.hasExpired()) {
			cachedEntries.remove(key);
			ce = null;
		}
		return ce;
	}

	
	public T get(String key) {
		CacheEntry<T> ce = getEntry(key);
		if(ce != null)
			return ce.get();
		else
			return null;
	}
	
	public void put(String key, T entry) {
		cachedEntries.put(key, new CacheEntry<T>(entry, System.currentTimeMillis() + timeout));
	}
	
	protected void clean() {
		if(System.currentTimeMillis() > lastClean + (5 * timeout)) {
			Iterator<String> it = cachedEntries.keySet().iterator();
			if(it.hasNext()) {
				String key = it.next();
				CacheEntry<T> ce = cachedEntries.get(key);
				if(ce.hasExpired())
					cachedEntries.remove(key);
			}				
			lastClean = System.currentTimeMillis();			
		}
		
	}
	
	public void clear(String key) {
		cachedEntries.remove(key);
	}
	
	public void clear() {
		cachedEntries.clear();
	}

}
