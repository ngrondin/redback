package io.redback.utils.stream;

public abstract class DataStream<TYPE> {
	protected DataStreamNextHandler nextHandler = null;
	protected Object waitLock = new Object();
	protected int nextBacklog = 1;
	protected boolean _isComplete = false;
	
	public DataStream() {
		
	}
	
	public void setNextHandler(DataStreamNextHandler handler) {
		nextHandler = handler;
		try { waitLock.notify(); } catch(Exception e) {}
	}
	
	public void send(TYPE data) {
		received(data);
		if(nextHandler == null) {
			try { 
				synchronized(waitLock) { 
					nextBacklog--;
					if(nextBacklog == 0) {
						waitLock.wait();	
					}
				} 
			} catch(Exception e) {}			
		}
	}
	
	public void complete() {
		_isComplete = true;
		completed();
	}
	
	public void requestNext() {
		if(nextHandler != null) {
			nextHandler.sendNext();
		} else {
			try { 
				synchronized(waitLock) { 
					nextBacklog++;
					waitLock.notify(); 
				} 
			} catch(Exception e) {}
		}
	}
	
	public boolean isComplete() {
		return _isComplete;
	}
	
	protected abstract void received(TYPE data);
	
	protected abstract void completed();
}
