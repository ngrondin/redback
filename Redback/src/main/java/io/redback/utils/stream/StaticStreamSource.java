package io.redback.utils.stream;

import java.util.List;

public class StaticStreamSource<T> implements DataStreamNextHandler {
	protected DataStream<T> dataStream;
	protected List<T> list;
	protected int i = 0;
	
	public StaticStreamSource(DataStream<T> ds, List<T> l) {
		dataStream = ds;
		list = l;
		dataStream.setNextHandler(this);
		sendNext();
	}

	public void sendNext() {
		if(i < list.size()) {
			System.out.println("Static source send, i=" + i);
			dataStream.send(list.get(i));
			i++;	
		} else {
			System.out.println("Static source complete");
			dataStream.complete();
		}
	}
}
