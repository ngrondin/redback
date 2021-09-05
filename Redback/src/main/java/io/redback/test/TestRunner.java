package io.redback.test;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Constructor;

import io.firebus.data.DataMap;
import io.redback.exceptions.RedbackException;

public class TestRunner {
	protected TestClient testClient;
	protected int threadCount;
	protected String classPathRoot;
	
	public TestRunner() throws RedbackException{
		try {
			InputStream is = getClass().getClassLoader().getResourceAsStream("io/redback/test/TestRunner.json");
			DataMap cfg = new DataMap(is);
			is.close();
			classPathRoot = cfg.getString("classpath");
			testClient = new TestClient(
					cfg.getString("firebusnetwork"), 
					cfg.getString("firebuspassword"), 
					cfg.getString("objectservice"), 
					cfg.getString("processservice"), 
					cfg.getString("url"),
					cfg.getString("jwtissuer"), 
					cfg.getString("jwtsecret"),
					cfg.getObject("rolemap")
			);
		} catch(Exception e) {
			throw new RedbackException("Error initiating test runner", e);
		}		
	}
	
	public void interactive() {
		boolean quit = false;
		BufferedReader lineReader = new BufferedReader(new InputStreamReader(System.in));
		while(!quit) {
			try {
				System.out.print("> ");
				String line = lineReader.readLine();
				String[] parts = line.split(" ");
				execute(parts);
			} catch(Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	public void execute(String[] args) throws Exception {
		String name = args[0];
		int repeat = args.length > 1 ? Integer.parseInt(args[1]) : 1;
		int threads = args.length > 2 ? Integer.parseInt(args[2]) : 1;
		execute(name, repeat, threads);
	}
	
	public void execute(String name, int repeat, int threads) throws Exception {
		int waitCount = 0;
		while(!testClient.isReady() && waitCount++ < 40)
			try { Thread.sleep(500); } catch(Exception e) {} 
		if(testClient.isReady()) {
			System.out.println("Executing " + name);
			Class<?> c = Class.forName(classPathRoot + "." + name);
			Constructor<?> cons = c.getConstructor(TestClient.class);
			long start = System.currentTimeMillis();
			for(int i = 0; i < threads; i++) {
				final Test test = (Test)cons.newInstance(testClient);
				final int threadId = i;
				Runnable r = new Runnable() {
					public void run() {
						for(int cycle = 0; cycle < repeat; cycle++) {
							try {
								test.run(cycle, threadId);
							} catch(Exception e) {
								System.out.println("Error running thread " + threadId + " cycle " + cycle);
								e.printStackTrace();
							}
						}
						threadCount--;
						synchronized(TestRunner.this) {
							TestRunner.this.notify();
						}
					}
				};
				Thread t = new Thread(r);
				threadCount++;
				t.start();
			}
			synchronized(this) {
				while(threadCount > 0)
					this.wait();
			}
			long end = System.currentTimeMillis();
			long dur = end - start;
			System.out.println("Execution done in " + dur + "ms (" + dur / (repeat * threads) + "ms/test)");			
		} else {
			System.out.println("Test Client has no connections");
		}
	}
	
	public static void main(String[] args) {
		try {
			TestRunner tr = new TestRunner();
			if(args.length == 0) {
				tr.interactive();
			} else {
				tr.execute(args);
			}
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
}
