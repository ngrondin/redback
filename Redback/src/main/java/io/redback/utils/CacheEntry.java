package io.redback.utils;

public class CacheEntry<T> {
	protected T value;
	protected long expiry;

	public CacheEntry(T v, long exp) {
		value = v;
		expiry = exp;
	}
	
	public T get() {
		return value;
	}
	
	public boolean hasExpired() {
		return expiry < System.currentTimeMillis();
	}
}
