package io.redback.utils;

public interface Sink<T> {

	public void next(T item);
	
	public void complete();
}
