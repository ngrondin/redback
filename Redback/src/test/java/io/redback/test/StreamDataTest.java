package io.redback.test;

import io.firebus.Firebus;
import io.firebus.Payload;
import io.firebus.StreamEndpoint;
import io.firebus.data.DataMap;
import io.firebus.interfaces.StreamHandler;

public class StreamDataTest {

	public static void main(String[] args) {
		Firebus firebus = new Firebus("firebus", "firebuspassword0");
		try {
			StreamEndpoint sep = firebus.requestStream("db", new Payload(new DataMap("object", "statushistory", "filter", new DataMap("object", "task"))), 10000);
			sep.setHandler(new StreamHandler() {
				public int c = 0;
				public long start = 0;
				public void receiveStreamData(Payload payload, StreamEndpoint streamEndpoint) {
					try {
						DataMap data = payload.getDataMap();
						//System.out.println(data.getString("_id"));
						if(start == 0) {
							start = System.currentTimeMillis();
							c = 0;
						} else {
							c++;
						}
						sep.send(new Payload("next"));
					} catch(Exception e) {
						System.err.println(e.getMessage());
					}
				}

				public void streamClosed(StreamEndpoint streamEndpoint) {
					streamEndpoint.close();
					long dur = System.currentTimeMillis() - start;
					System.out.println(dur);
					System.out.println(c);
				}
			});
		} catch(Exception e) {
			System.err.println(e.getMessage());
		}
	}
}
