package io.redback.utils.stream;

import io.firebus.logging.Logger;
import io.redback.exceptions.RedbackException;


public class ProcessingDataStream<T> extends DataStream<T> {
	public interface Processor<T> {
		public void process(T item) throws Exception;
	};
	
	protected boolean complete = false;
	protected Processor<T> processor;
	protected Exception processException;
	
	public ProcessingDataStream(Processor<T> p) {
		processor = p;
		processException = null;
	}
	
	public void received(T item) {
		try {
			if(item != null) {
				processor.process(item);
			}
			requestNext();
		} catch(Exception e) {
			processException = e;
			complete();
		}		
	}

	public void completed() {
		try {
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
