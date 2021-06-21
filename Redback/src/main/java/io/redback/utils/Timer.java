package io.redback.utils;

import java.util.logging.Logger;

public class Timer {
	private Logger logger = Logger.getLogger("io.redback");
	
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
		logger.info((end - start) + "ms" + (data != null ? ":" + data : ""));
	}
	
	public void mark() {
		mark(null);
	}
}
