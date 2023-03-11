package io.redback.services.common;


import io.firebus.Firebus;
import io.firebus.Payload;
import io.firebus.data.DataMap;
import io.firebus.exceptions.FunctionErrorException;
import io.firebus.logging.Logger;
import io.firebus.threads.FirebusThread;
import io.redback.exceptions.RedbackException;
import io.redback.security.Session;
import io.redback.utils.Timer;

public abstract class ServiceProvider extends Provider implements io.firebus.interfaces.ServiceProvider {
	
	public ServiceProvider(String n, DataMap c, Firebus f) {
		super(n, c, f);
	}

	public Payload service(Payload payload) throws FunctionErrorException
	{
		Timer timer = null;
		try {
			Session session = new Session(payload.metadata.get("session"));
			session.setTimezone(payload.metadata.get("timezone"));
			if(Thread.currentThread() instanceof FirebusThread) 
				((FirebusThread)Thread.currentThread()).setTrackingId(session.getId());
			timer = new Timer();
			Payload response = redbackService(session, payload);
			Logger.info("rb.service", new DataMap("ms", timer.mark(), "req", payload.getDataObject()));
			return response;
		} catch(Exception e) {
			throw handleException("rb.service", "Exception in redback service '" + serviceName + "'", e);
		}
	}
	


	protected abstract Payload redbackService(Session session, Payload payload) throws RedbackException;
}
