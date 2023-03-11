package io.redback.services.common;



import io.firebus.Firebus;
import io.firebus.Payload;
import io.firebus.StreamEndpoint;
import io.firebus.data.DataMap;
import io.firebus.exceptions.FunctionErrorException;
import io.firebus.logging.Logger;
import io.firebus.threads.FirebusThread;
import io.redback.exceptions.RedbackException;
import io.redback.security.Session;
import io.redback.utils.Timer;

public abstract class StreamProvider extends Provider implements io.firebus.interfaces.StreamProvider {
	
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
			timer = new Timer();
			StreamHandler streamHandler = redbackAcceptStream(session, payload);
			Payload acceptPayload = null;
			if(streamHandler != null) {
				streamHandler.setStreamEndpoint(streamEndpoint);
				acceptPayload = streamHandler.getAcceptPayload();
			} else {
				throw new RedbackException("No handler returned");
			}
			Logger.info("rb.stream", new DataMap("ms", timer.mark(), "req", payload.getDataObject()));
			return acceptPayload;
		} catch(Exception e) {
			throw handleException("rb.stream", "Exception in redback stream '" + serviceName + "'", e);
		} finally {
			if(timer != null) timer.mark();
	
		}
	}


	public abstract StreamHandler redbackAcceptStream(Session session, Payload payload) throws RedbackException;
}
