package io.redback.utils.stream;

public abstract class DataStream<TYPE> {
	//protected int sendNextBacklog = 0;
	protected DataStreamNextHandler nextHandler = null;
	protected Object waitLock = new Object();
	
	public DataStream() {
		
	}
	
	public void setNextHandler(DataStreamNextHandler handler) {
		nextHandler = handler;
		try { waitLock.notify(); } catch(Exception e) {}
		/*while(sendNextBacklog > 0) {
			nextHandler.sendNext();
			sendNextBacklog--;
		}*/
	}
	
	public void send(TYPE data) {
		received(data);
		if(nextHandler == null) {
			try {
				synchronized(waitLock) {
					waitLock.wait();
				}
			} catch(Exception e) {}			
		}
	}
	
	public void complete() {
		completed();
	}
	
	public void requestNext() {
		if(nextHandler != null) {
			nextHandler.sendNext();
		} else {
			//sendNextBacklog++;
			try { waitLock.notify(); } catch(Exception e) {}
		}
	}
	
	protected abstract void received(TYPE data);
	
	protected abstract void completed();
}
