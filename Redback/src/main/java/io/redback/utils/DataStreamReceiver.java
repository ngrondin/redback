package io.redback.utils;

public interface DataStreamReceiver<T> {

	public void receive(T data);
}
