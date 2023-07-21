package io.redback.utils.stream;

import io.firebus.logging.Logger;

public class BasicStreamPipeline<TARGET, SOURCE> implements DataStreamNextHandler {
	protected DataStream<TARGET> targetStream;
	protected DataStream<SOURCE> sourceStream;
	protected BasicConverter<TARGET, SOURCE> converter;
	
	public BasicStreamPipeline(DataStream<TARGET> ts, BasicConverter<TARGET, SOURCE> bc) {
		targetStream = ts;
		converter = bc;
		
		sourceStream = new DataStream<SOURCE>() {
			protected void received(SOURCE data) {
				try {
					targetStream.send(converter.convert(data));
				} catch (Exception e) {
					Logger.severe("rb.basicstreampipeline.convert", e);
				}
			}

			protected void completed() {
				targetStream.complete();
			}
		};
		targetStream.setNextHandler(this);
	}

	public void sendNext() {
		sourceStream.requestNext();
	}
	
	public DataStream<SOURCE> getSourceDataStream() {
		return sourceStream;
	}
}
