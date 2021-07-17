package io.redback.services.common;

import java.util.logging.Logger;

import io.firebus.Payload;
import io.firebus.StreamEndpoint;
import io.redback.RedbackException;
import io.redback.security.Session;
import io.redback.utils.StringUtils;

public abstract class StreamHandler implements io.firebus.interfaces.StreamHandler {
	private Logger logger = Logger.getLogger("io.redback");
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
				logger.severe(StringUtils.getStackTrace(e));
			}
		}	
	}

	public void streamClosed(StreamEndpoint streamEndpoint) {
		if(streamEndpoint == this.streamEndpoint) {
			try {
				closed();
			} catch(Exception e) {
				logger.severe(StringUtils.getStackTrace(e));
			}
		}
	}
	
	public void sendStreamData(Payload payload) {
		streamEndpoint.send(payload);
	}

	public abstract void receiveData(Payload payload) throws RedbackException;
	
	public abstract void closed() throws RedbackException;
	
	public abstract Payload getAcceptPayload() throws RedbackException;
	
	public Session getSession() {
		return session;
	}
}
