package io.redback.utils.stream;

public abstract class DataStream<TYPE> {
	protected int sendNextBacklog = 0;
	protected DataStreamNextHandler nextHandler = null;
	
	public DataStream() {
		
	}
	
	public void setNextHandler(DataStreamNextHandler handler) {
		nextHandler = handler;
		while(sendNextBacklog > 0) {
			nextHandler.sendNext();
			sendNextBacklog--;
		}
	}
	
	public void send(TYPE data) {
		received(data);
	}
	
	public void complete() {
		completed();
	}
	
	public void requestNext() {
		if(nextHandler != null) {
			nextHandler.sendNext();
		} else {
			sendNextBacklog++;
		}
	}
	
	protected abstract void received(TYPE data);
	
	protected abstract void completed();
}
