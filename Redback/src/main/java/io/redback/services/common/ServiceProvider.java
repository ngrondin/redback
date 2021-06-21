package io.redback.services.common;

import java.util.logging.Logger;

import io.firebus.Firebus;
import io.firebus.Payload;
import io.firebus.exceptions.FunctionErrorException;
import io.firebus.threads.FirebusThread;
import io.firebus.utils.DataMap;
import io.redback.RedbackException;
import io.redback.security.Session;
import io.redback.utils.StringUtils;
import io.redback.utils.Timer;

public abstract class ServiceProvider extends Service implements io.firebus.interfaces.ServiceProvider {
	private Logger logger = Logger.getLogger("io.redback");
	
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
			timer = new Timer(serviceName, session.getId(), getLogline(payload));
			logger.finer("Service '" + serviceName + "' started");
			Payload response = redbackService(session, payload);
			logger.finer("Service '" + this.serviceName + "' finished");
			return response;
		} catch(Exception e) {
			logger.severe(StringUtils.getStackTrace(e));
			throw new FunctionErrorException("Exception in redback service '" + serviceName + "'", e);
		} finally {
			if(timer != null) timer.mark();
			if(Thread.currentThread() instanceof FirebusThread) 
				((FirebusThread)Thread.currentThread()).setTrackingId(null);			
		}
	}
	


	protected abstract Payload redbackService(Session session, Payload payload) throws RedbackException;
}
