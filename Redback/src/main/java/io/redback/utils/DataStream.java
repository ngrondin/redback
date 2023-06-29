package io.redback.utils;

import java.util.ArrayList;
import java.util.List;

public abstract class DataStream<INTYPE, OUTTYPE> {
	protected DataStreamReceiver<OUTTYPE> outReceiver;
	protected List<OUTTYPE> outBuffer = new ArrayList<OUTTYPE>();
	
	public DataStream() {
		
	}
	
	public void setReceiver(DataStreamReceiver<OUTTYPE> receiver) {
		outReceiver = receiver;
		if(outReceiver != null)
			for(OUTTYPE data: outBuffer) 
				outReceiver.receive(data);
	}

	public void sendOut(OUTTYPE data) {
		if(outReceiver != null) {
			outReceiver.receive(data);
		} else {
			outBuffer.add(data);
		}
	}
	
	public void sendIn(INTYPE data) {
		received(data);
	}
	
	public void complete() {
		completed();
	}

	protected abstract void received(INTYPE data);
	
	protected abstract void completed();
}
