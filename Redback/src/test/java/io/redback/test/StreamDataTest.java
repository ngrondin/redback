package io.redback.test;

import io.firebus.Firebus;
import io.firebus.Payload;
import io.firebus.StreamEndpoint;
import io.firebus.data.DataList;
import io.firebus.data.DataMap;
import io.firebus.exceptions.FunctionErrorException;
import io.firebus.interfaces.StreamHandler;
import io.firebus.logging.Level;
import io.firebus.logging.Logger;

public class StreamDataTest {

	public static void main(String[] args) {
		Logger.setLevel(Level.FINE);
		Firebus firebus = new Firebus("firebus", "firebuspassword0");
		try {
			StreamEndpoint sep = firebus.requestStream("db", new Payload(new DataMap("object", "statushistory", "filter", new DataMap())), 10000);
			sep.setHandler(new StreamHandler() {
				public int c = 0;
				public int i = 0;
				public long start = 0;
				public void receiveStreamData(Payload payload) {
					try {
						DataMap data = payload.getDataMap();
						DataList list = data.getList("result");
						for(int j = 0; j < list.size(); j++) 
							i++;
						if(start == 0) {
							start = System.currentTimeMillis();
						} 
						c++;
						sep.send(new Payload("next"));
					} catch(Exception e) {
						System.err.println(e.getMessage());
					}
				}

				public void streamClosed() {
					sep.close();
					long dur = System.currentTimeMillis() - start;
					System.out.println(dur);
					System.out.println(i);
					System.out.println(c);
				}

				public void streamError(FunctionErrorException error) {
					
				}
				
				
			});
		} catch(Exception e) {
			System.err.println(e.getMessage());
		}
	}
}
