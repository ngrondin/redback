package io.redback.services.common;

import io.firebus.Firebus;
import io.firebus.Payload;
import io.firebus.StreamEndpoint;
import io.firebus.data.DataMap;
import io.firebus.exceptions.FunctionErrorException;
import io.firebus.logging.Logger;
import io.redback.exceptions.RedbackException;
import io.redback.security.Session;
import io.redback.utils.Timer;

public abstract class StreamProvider extends Provider implements io.firebus.interfaces.StreamProvider {

	public StreamProvider(String n, DataMap c, Firebus f) {
		super(n, c, f);
	}

	public Payload acceptStream(Payload payload, StreamEndpoint streamEndpoint) throws FunctionErrorException {
		try {
			Timer timer = new Timer();
			Session session = getSession(payload);
			checkStarted();
			Payload acceptPayload = redbackAcceptStream(session, payload, streamEndpoint);
			if(writeRequestLog)
				Logger.info("rb.stream", new DataMap("ms", timer.mark(), "req", payload.getDataObject(), "session", session.getStats()));
			return acceptPayload;
		} catch(Exception e) {
			throw handleException("rb.stream", "Exception in redback stream '" + serviceName + "'", e);
		} 
	}

	protected abstract Payload redbackAcceptStream(Session session, Payload payload, StreamEndpoint streamEndpoint) throws RedbackException;
}