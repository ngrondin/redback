package io.redback.test;

public abstract class Test {

	protected TestClient testClient;
	
	public Test(TestClient tc) {
		testClient = tc;
	}
	
	public abstract void run(int cycle, int thread) throws Exception;
}
