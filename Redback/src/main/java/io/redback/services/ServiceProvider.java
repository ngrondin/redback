package io.redback.services;

import java.util.logging.Logger;

import io.firebus.Firebus;
import io.firebus.Payload;
import io.firebus.exceptions.FunctionErrorException;
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
		try {
			Session session = new Session(payload.metadata.get("session"));
			session.setTimezoneOffsetString(payload.metadata.get("timezone"));
			Timer timer = new Timer(serviceName, session.getId(), getLogline(payload));
			logger.finer("Service '" + serviceName + "' started");
			Payload response = redbackService(session, payload);
			logger.finer("Service '" + this.serviceName + "' finished");
			timer.mark();
			return response;
		} catch(Exception e) {
			logger.severe(StringUtils.getStackTrace(e));
			throw new FunctionErrorException("Exception in redback service '" + serviceName + "'", e);
		}
	}
	
	private String getLogline(Payload payload) {
		String mime = payload.metadata.get("mime");
		String body = null;
		if(mime != null && mime.equals("application/json")) {
			body = payload.getString().replaceAll("\r", "").replaceAll("\n", "").replaceAll("\t", "");
			return body;
		} else {
			return "";
		}
		
	}

	protected abstract Payload redbackService(Session session, Payload payload) throws RedbackException;
}
