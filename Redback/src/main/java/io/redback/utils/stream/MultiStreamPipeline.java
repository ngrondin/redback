package io.redback.utils.stream;

import java.util.ArrayList;
import java.util.List;

public class MultiStreamPipeline<T> implements DataStreamNextHandler {
	protected DataStream<T> targetStream;
	protected List<DataStream<T>> sourceStreams = new ArrayList<DataStream<T>>();
	protected int source = 0;
	
	public MultiStreamPipeline(DataStream<T> ts, int c) {
		targetStream = ts;
		
		for(int i = 0; i < c; i++) {
			sourceStreams.add(new DataStream<T>() {
				protected void received(T data) {
					targetStream.send(data);
				}

				protected void completed() {
					sendNext();
				}
			});
		}
		targetStream.setNextHandler(this);
	}

	public void sendNext() {
		for(DataStream<T> stream : sourceStreams) {
			if(!stream.isComplete()) {
				stream.requestNext();
				return;
			}
		}
		targetStream.complete();
	}
	
	public DataStream<T> getSourceDataStream(int i) {
		return sourceStreams.get(i);
	}
	
	
}
