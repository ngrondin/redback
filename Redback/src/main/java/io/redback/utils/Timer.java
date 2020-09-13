package io.redback.utils;

public class Timer {
	protected String name;
	protected String data;
	protected long start;
	
	public Timer(String n, String d) {
		name = n;
		data = d;
		start = System.currentTimeMillis();
	}
	
	public void mark(String l) {
		long end = System.currentTimeMillis();
		System.out.println("timer:" + name + (l != null ? "." + l : "") + ":" + end + ":" + (end - start) + "ms" + (data != null ? ":" + data : ""));
	}
	
	public void mark() {
		mark(null);
	}
}
