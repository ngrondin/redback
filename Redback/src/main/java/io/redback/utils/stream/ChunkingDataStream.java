package io.redback.utils.stream;

import java.util.ArrayList;
import java.util.List;

public abstract class ChunkingDataStream<T> extends DataStream<T> {
	protected List<T> list = new ArrayList<T>();
	protected int chunkSize;
	
	public ChunkingDataStream(int size) {
		chunkSize = size;
	}
	
	protected void received(T data) {
		list.add(data);
		if(list.size() >= chunkSize) {
			List<T> currentList = list;
			list = new ArrayList<T>();
			receivedChunk(currentList);
		} else {
			requestNext();
		}
	}
	
	protected void completed() {
		if(list.size() > 0) {
			receivedChunk(list);
		}
		chunksCompleted();
	}

	protected abstract void receivedChunk(List<T> list);
	
	protected abstract void chunksCompleted();
}
