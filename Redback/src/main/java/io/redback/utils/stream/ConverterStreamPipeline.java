package io.redback.utils.stream;

import io.firebus.logging.Logger;

public class ConverterStreamPipeline<TARGET, SOURCE> implements DataStreamNextHandler {
	protected DataStream<TARGET> targetStream;
	protected DataStream<SOURCE> sourceStream;
	protected Converter<TARGET, SOURCE> converter;
	
	public ConverterStreamPipeline(DataStream<TARGET> ts, Converter<TARGET, SOURCE> bc) {
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
