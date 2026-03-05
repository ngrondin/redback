package io.redback.utils.stream.impl;

import java.util.ArrayList;
import java.util.List;

import io.firebus.logging.Logger;
import io.redback.exceptions.RedbackException;
import io.redback.utils.stream.DataStream;


public class AccumulatingDataStream<T> extends DataStream<T> {
	protected List<T> list = new ArrayList<T>();
	protected boolean complete = false;
	
	public void received(T item) {
		if(item != null)
			list.add(item);
		requestNext();
	}

	public void completed() {
		try {
			synchronized(this) {
				complete = true;
				this.notify();
			}
		} catch(Exception e) {
			Logger.severe("Error completing data stream accumulation", e);
		}
	}
	
	public List<T> getList() throws RedbackException {
		try {
			synchronized(this) {
				if(!complete)
					this.wait(15000);
				return list;
			}
		} catch(Exception e) {
			throw new RedbackException("Error accumulating stream data", e);
		}
	}

}
