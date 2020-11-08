package io.redback.utils;

public class CacheEntry<T> {
	protected T entry;
	protected long expiry;

	public CacheEntry(T e, long exp) {
		entry = e;
		expiry = exp;
	}
	
	public T get() {
		return entry;
	}
	
	public boolean hasExpired() {
		return expiry < System.currentTimeMillis();
	}
}
