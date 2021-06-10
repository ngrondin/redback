package io.redback.utils;

import java.util.List;

import io.firebus.Firebus;
import io.firebus.information.Statistics;

public class Watchdog extends Thread {
	
	public boolean quit;
	public Firebus firebus;
	
	public Watchdog(Firebus fb) {
		firebus = fb;
		quit = false;
		start();
	}
	
	public void run() {
		while(!quit) {
			try {
				long maxHeap = (Runtime.getRuntime().maxMemory() / 1048576) ;
				long totalHeap = (Runtime.getRuntime().totalMemory() / 1048576);
				long freeHeap = (Runtime.getRuntime().freeMemory() / 1048576);
				long usedHeap = totalHeap - freeHeap;
				System.out.println("memory::" + System.currentTimeMillis() + ":" + usedHeap + "/" + totalHeap + "/" + maxHeap);
				List<Statistics> stats = firebus.getStatistics();
				StringBuilder sb = new StringBuilder();
				for(Statistics stat : stats) 
					sb.append(stat.toString());
				System.out.println("firebus:" + sb.toString());
				Thread.sleep(60000);
			} catch(Exception e) {
				e.printStackTrace();
			}			
		}
	}
	

}
