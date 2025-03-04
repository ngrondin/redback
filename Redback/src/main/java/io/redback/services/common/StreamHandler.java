package io.redback.services.common;

import io.firebus.Payload;
import io.firebus.StreamEndpoint;
import io.firebus.logging.Logger;
import io.redback.exceptions.RedbackException;
import io.redback.security.Session;
import io.redback.utils.StringUtils;

public abstract class StreamHandler implements io.firebus.interfaces.StreamHandler {
	protected Session session;
	protected StreamEndpoint streamEndpoint;
	
	public StreamHandler(Session s, StreamEndpoint se) {
		session = s;
		streamEndpoint = se;
		streamEndpoint.setHandler(this);
	}
	
	public void receiveStreamData(Payload payload, StreamEndpoint streamEndpoint) {
		if(streamEndpoint == this.streamEndpoint) {
			try {
				receiveData(payload);
			} catch(Exception e) {
				logException("rb.streamhandler.receive", "Error receiving stream data", e);
			}
		}	
	}

	public void streamClosed(StreamEndpoint streamEndpoint) {
		if(streamEndpoint == this.streamEndpoint) {
			try {
				closed();
			} catch(Exception e) {
				logException("rb.streamhandler.closed", "Error closing stream", e);
			}
		}
	}
	
	public void sendStreamData(Payload payload) {
		if(streamEndpoint != null)
			streamEndpoint.send(payload);
	}
	
	public Session getSession() {
		return session;
	}
	
	protected void logException(String event, String msg, Exception e) {
		int errorCode = 0;
		if(e instanceof RedbackException) {
			RedbackException rbe = (RedbackException)e;
			errorCode = rbe.getErrorCode();
		}
		if(errorCode == 0 || errorCode >= 500)
			Logger.severe(event, msg, e);
		if(errorCode == 401) 
			Logger.security(event, msg, e);
		else 
			Logger.userError(event, StringUtils.rollUpExceptions(e) + ": " + msg);
	}

	public abstract void receiveData(Payload payload) throws RedbackException;
	
	public abstract void closed() throws RedbackException;
}