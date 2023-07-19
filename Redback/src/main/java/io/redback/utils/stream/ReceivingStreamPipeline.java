package io.redback.utils.stream;

import java.util.ArrayList;
import java.util.List;

import io.firebus.Payload;
import io.firebus.StreamEndpoint;
import io.firebus.interfaces.StreamHandler;
import io.firebus.logging.Logger;

public class ReceivingStreamPipeline<T> implements StreamHandler, DataStreamNextHandler {
	protected List<T> buffer;
	protected StreamEndpoint sep;
	protected DataStream<T> dataStream;
	protected ReceivingConverter<T> converter;
	
	public ReceivingStreamPipeline(StreamEndpoint s, DataStream<T> ds, ReceivingConverter<T> rc) {
		sep = s;
		dataStream = ds;
		converter = rc;
		buffer = new ArrayList<T>();
		sep.setHandler(this);
		ds.setNextHandler(this);
	}

	public void receiveStreamData(Payload payload, StreamEndpoint streamEndpoint) {
		try {
			List<T> list = converter.convert(payload);
			buffer.addAll(list);
			sendNext();
		} catch(Exception e) {
			Logger.severe("rb.receivingstreampipeline.convert", e);
		}
	}

	public void streamClosed(StreamEndpoint streamEndpoint) {
		if(buffer.size() == 0)
			dataStream.complete();
	}

	public void sendNext() {
		if(buffer.size() > 0) {
			T item = buffer.remove(0);
			dataStream.send(item);
		} else if(sep.isActive()) {
			sep.send(new Payload("next"));
		} else {
			dataStream.complete();
		}
	}
}
