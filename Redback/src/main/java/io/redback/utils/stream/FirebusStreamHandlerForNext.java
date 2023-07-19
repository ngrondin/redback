package io.redback.utils.stream;

import io.firebus.Payload;
import io.firebus.StreamEndpoint;
import io.firebus.interfaces.StreamHandler;
import io.firebus.logging.Logger;

public class FirebusStreamHandlerForNext implements StreamHandler {
	protected DataStream<?> dataStream;
	
	public FirebusStreamHandlerForNext(DataStream<?> ds) {
		dataStream = ds;
	}
	
	public void receiveStreamData(Payload payload, StreamEndpoint streamEndpoint) {
		try {
			String s = payload.getString();
			if(s.equals("next"))
				dataStream.requestNext();
		} catch(Exception e) {
			Logger.severe("rb.objectserver.stream.out", e);
		}
	}

	public void streamClosed(StreamEndpoint streamEndpoint) {
		dataStream.complete();
	}
}
