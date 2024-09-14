package io.redback.services.common;

import io.firebus.Firebus;
import io.firebus.Payload;
import io.firebus.data.DataMap;
import io.firebus.exceptions.FunctionErrorException;
import io.firebus.logging.Logger;
import io.redback.exceptions.RedbackException;
import io.redback.security.Session;
import io.redback.utils.Timer;

public abstract class ServiceProvider extends Provider implements io.firebus.interfaces.ServiceProvider {
	
	public ServiceProvider(String n, DataMap c, Firebus f) {
		super(n, c, f);
	}

	public Payload service(Payload payload) throws FunctionErrorException
	{
		try {
			Timer timer = new Timer();
			Session session = getSession(payload);
			Payload response = redbackService(session, payload);
			if(writeRequestLog)
				Logger.info("rb.service", new DataMap("ms", timer.mark(), "req", payload.getDataObject(), "session", session.getStats()));
			return response;
		} catch(Exception e) {
			throw handleException("rb.service", "Exception in redback service '" + serviceName + "'", e);
		}
	}
	
	protected abstract Payload redbackService(Session session, Payload payload) throws RedbackException;
}