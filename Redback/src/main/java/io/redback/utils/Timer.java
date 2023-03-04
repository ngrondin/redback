package io.redback.utils;

public class Timer {
	protected String name;
	protected String data;
	protected String session;
	protected long start;
	
	public Timer() {
		start = System.currentTimeMillis();
	}
	
	public long mark() {
		long end = System.currentTimeMillis();
		return (end - start);
	}
}
