package io.redback.utils;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class Cache<T> {
	
	protected Map<String, CacheEntry<T>> cachedEntries;
	protected long lastClean = 0;
	protected long timeout;
	
	public Cache(long to) {
		cachedEntries = new HashMap<String, CacheEntry<T>>();
		timeout = to;
		if(timeout == 0) 
			timeout = 120000;
	}
	
	public T get(String key) {
		if(System.currentTimeMillis() > lastClean + (5 * timeout)) {
			clean();
			lastClean = System.currentTimeMillis();			
		}
		
		CacheEntry<T> ce = cachedEntries.get(key);
		if(ce != null && ce.hasExpired()) {
			cachedEntries.remove(key);
			ce = null;
		}
		
		if(ce != null)
			return ce.get();
		else
			return null;
	}
	
	public void put(String key, T entry) {
		cachedEntries.put(key, new CacheEntry<T>(entry, System.currentTimeMillis() + timeout));
	}
	
	protected void clean() {
		Iterator<String> it = cachedEntries.keySet().iterator();
		if(it.hasNext()) {
			String key = it.next();
			CacheEntry<T> ce = cachedEntries.get(key);
			if(ce.hasExpired())
				cachedEntries.remove(key);
		}			
	}
	

}
