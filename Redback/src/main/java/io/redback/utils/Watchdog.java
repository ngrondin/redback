package io.redback.utils;

import java.util.List;
import java.util.logging.Logger;

import io.firebus.Firebus;
import io.firebus.information.Statistics;

public class Watchdog extends Thread {
	private Logger logger = Logger.getLogger("io.redback");
	public boolean quit;
	public Firebus firebus;
	
	public Watchdog(Firebus fb) {
		firebus = fb;
		quit = false;
		setName("rbWatchdog");
		start();
	}
	
	public void run() {
		while(!quit) {
			try {
				long maxHeap = (Runtime.getRuntime().maxMemory() / 1048576) ;
				long totalHeap = (Runtime.getRuntime().totalMemory() / 1048576);
				long freeHeap = (Runtime.getRuntime().freeMemory() / 1048576);
				long usedHeap = totalHeap - freeHeap;
				logger.info("memory::" + System.currentTimeMillis() + ":" + usedHeap + "/" + totalHeap + "/" + maxHeap);
				List<Statistics> stats = firebus.getStatistics();
				StringBuilder sb = new StringBuilder();
				for(Statistics stat : stats) 
					sb.append(stat.toString());
				logger.info("firebus:" + sb.toString());
				Thread.sleep(60000);
			} catch(Exception e) {
				e.printStackTrace();
			}			
		}
	}
	

}
