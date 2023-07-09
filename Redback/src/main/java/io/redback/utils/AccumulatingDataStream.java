package io.redback.utils;

import java.util.ArrayList;
import java.util.List;

import io.firebus.logging.Logger;
import io.redback.exceptions.RedbackException;


public class AccumulatingDataStream<T> extends DataStream<List<T>, Boolean> {
	protected List<T> list = new ArrayList<T>();
	protected boolean complete = false;
	
	public void received(List<T> sublist) {
		list.addAll(sublist);
		sendOut(true);
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
