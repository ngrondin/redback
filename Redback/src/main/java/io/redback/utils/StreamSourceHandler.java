package io.redback.utils;

public interface StreamSourceHandler<T> {

	public void source(T item);
}
