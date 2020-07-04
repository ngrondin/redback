package io.redback.utils;

public class Timer {
	protected String name;
	protected long start;
	
	public Timer(String n) {
		name = n;
		start = System.currentTimeMillis();
	}
	
	public void mark(String l) {
		long end = System.currentTimeMillis();
		System.out.println("time " + name + (l != null ? "." + l : "") + ":" + (end - start) + "ms");
	}
	
	public void mark() {
		mark(null);
	}
}
