package io.redback.services.common;


import java.util.logging.Logger;

import io.firebus.Firebus;
import io.firebus.Payload;
import io.firebus.StreamEndpoint;
import io.firebus.exceptions.FunctionErrorException;
import io.firebus.threads.FirebusThread;
import io.firebus.utils.DataMap;
import io.redback.RedbackException;
import io.redback.security.Session;
import io.redback.utils.StringUtils;
import io.redback.utils.Timer;

public abstract class StreamProvider extends Service implements io.firebus.interfaces.StreamProvider {
	private Logger logger = Logger.getLogger("io.redback");
	
	public StreamProvider(String n, DataMap c, Firebus f) {
		super(n, c, f);
	}

	public Payload acceptStream(Payload payload, StreamEndpoint streamEndpoint) throws FunctionErrorException {
		Timer timer = null;
		try {
			Session session = new Session(payload.metadata.get("session"));
			session.setTimezone(payload.metadata.get("timezone"));
			if(Thread.currentThread() instanceof FirebusThread) 
				((FirebusThread)Thread.currentThread()).setTrackingId(session.getId());
			timer = new Timer(serviceName, session.getId(), getLogline(payload));
			logger.finer("Stream '" + serviceName + "' started");
			StreamHandler streamHandler = redbackStream(session, payload);
			Payload acceptPayload = null;
			if(streamHandler != null) {
				streamHandler.setStreamEndpoint(streamEndpoint);
				acceptPayload = streamHandler.getAcceptPayload();
			} else {
				throw new RedbackException("No handler returned");
			}
			logger.finer("Stream '" + this.serviceName + "' finished");
			return acceptPayload;
		} catch(Exception e) {
			logger.severe(StringUtils.getStackTrace(e));
			throw new FunctionErrorException("Exception in redback stream '" + serviceName + "'", e);
		} finally {
			if(timer != null) timer.mark();
			if(Thread.currentThread() instanceof FirebusThread) 
				((FirebusThread)Thread.currentThread()).setTrackingId(null);			
		}
	}


	public abstract StreamHandler redbackStream(Session session, Payload payload) throws RedbackException;
}
