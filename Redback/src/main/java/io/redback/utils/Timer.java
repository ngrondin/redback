package io.redback.utils;

public class Timer {
	protected String name;
	protected String data;
	protected String session;
	protected long start;
	
	public Timer(String n, String s, String d) {
		name = n;
		session = s;
		data = d;
		start = System.currentTimeMillis();
	}
	
	public void mark(String l) {
		long end = System.currentTimeMillis();
		System.out.println("timer:" + name + (l != null ? "." + l : "") + ":" + (session != null ? session : "") + ":" + end + ":" + (end - start) + "ms" + (data != null ? ":" + data : ""));
	}
	
	public void mark() {
		mark(null);
	}
}
