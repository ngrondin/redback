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

public abstract class DualProvider extends Provider implements io.firebus.interfaces.ServiceProvider, io.firebus.interfaces.StreamProvider {
	
	public DualProvider(String n, DataMap c, Firebus f) {
		super(n, c, f);
	}

	public Payload service(Payload payload) throws FunctionErrorException
	{
		try {
			Timer timer = new Timer();
			Payload response = redbackService(getSession(payload), payload);
			Logger.info("rb.service", new DataMap("ms", timer.mark(), "req", payload.getDataObject()));
			return response;
		} catch(Exception e) {
			throw handleException("rb.service", "Exception in redback service '" + serviceName + "'", e);
		}
	}
	
	public Payload acceptStream(Payload payload, StreamEndpoint streamEndpoint) throws FunctionErrorException {
		try {
			Timer timer = new Timer();
			Payload acceptPayload = redbackAcceptStream(getSession(payload), payload, streamEndpoint);
			Logger.info("rb.stream", new DataMap("ms", timer.mark(), "req", payload.getDataObject()));
			return acceptPayload;
		} catch(Exception e) {
			throw handleException("rb.stream", "Exception in redback stream '" + serviceName + "'", e);
		} 
	}
	
	protected abstract Payload redbackService(Session session, Payload payload) throws RedbackException;
	
	protected abstract Payload redbackAcceptStream(Session session, Payload payload, StreamEndpoint streamEndpoint) throws RedbackException;

}