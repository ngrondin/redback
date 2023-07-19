package io.redback.utils.stream;

public class PassthroughDataStreamNextHandler implements DataStreamNextHandler {
	protected DataStream<?> dataStream;
	
	public PassthroughDataStreamNextHandler(DataStream<?> ds) {
		dataStream = ds;
	}
	
	public void sendNext() {
		dataStream.requestNext();
	}
}
