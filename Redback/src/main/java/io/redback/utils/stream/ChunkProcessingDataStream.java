package io.redback.utils.stream;

import java.util.ArrayList;
import java.util.List;

import io.firebus.logging.Logger;
import io.redback.exceptions.RedbackException;


public class ChunkProcessingDataStream<T> extends DataStream<T> {
	public interface Processor<T> {
		public void process(List<T> item) throws Exception;
	};
	
	protected boolean complete = false;
	protected Processor<T> processor;
	protected Exception processException;
	protected List<T> accumulator;
	protected int chunkSize;
	
	public ChunkProcessingDataStream(int s, Processor<T> p) {
		processor = p;
		processException = null;
		accumulator = new ArrayList<T>();
		chunkSize = s;
	}
	
	public void received(T item) {
		try {
			if(item != null) {
				accumulator.add(item);
				if(accumulator.size() >= chunkSize) {
					processor.process(accumulator);	
					accumulator.clear();
				}
			}
			requestNext();
		} catch(Exception e) {
			processException = e;
			complete();
		}		
	}

	public void completed() {
		try {
			if(accumulator.size() > 0 && processException == null) {
				processor.process(accumulator);	
				accumulator.clear();
			}
			synchronized(this) {
				complete = true;
				this.notify();
			}
		} catch(Exception e) {
			Logger.severe("Error completing data stream processing", e);
		}
	}
	
	public void waitUntilDone() throws RedbackException {
		try {
			synchronized(this) {
				if(!complete)
					this.wait(15000);
			}
			if(processException != null) {
				throw processException;
			}
		} catch(Exception e) {
			throw new RedbackException("Error processing stream data", e);
		}
	}

}
