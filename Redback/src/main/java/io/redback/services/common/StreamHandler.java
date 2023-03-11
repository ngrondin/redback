package io.redback.services.common;

import io.firebus.Payload;
import io.firebus.StreamEndpoint;
import io.firebus.logging.Logger;
import io.redback.exceptions.RedbackException;
import io.redback.security.Session;

public abstract class StreamHandler implements io.firebus.interfaces.StreamHandler {
	protected Session session;
	protected StreamEndpoint streamEndpoint;
	
	public StreamHandler(Session s) {
		session = s;
	}
	
	protected void setStreamEndpoint(StreamEndpoint sep) {
		streamEndpoint = sep;
		streamEndpoint.setHandler(this);		
	}
	
	public void receiveStreamData(Payload payload, StreamEndpoint streamEndpoint) {
		if(streamEndpoint == this.streamEndpoint) {
			try {
				receiveData(payload);
			} catch(Exception e) {
				Logger.severe("rb.streamhandler.receive", "Error receiving stream data", e);
			}
		}	
	}

	public void streamClosed(StreamEndpoint streamEndpoint) {
		if(streamEndpoint == this.streamEndpoint) {
			try {
				closed();
			} catch(Exception e) {
				Logger.severe("rb.streamhandler.closed", "Error closing stream", e);
			}
		}
	}
	
	public void sendStreamData(Payload payload) {
		if(streamEndpoint != null)
			streamEndpoint.send(payload);
	}

	public abstract void receiveData(Payload payload) throws RedbackException;
	
	public abstract void closed() throws RedbackException;
	
	public abstract Payload getAcceptPayload() throws RedbackException;
	
	public Session getSession() {
		return session;
	}
}
