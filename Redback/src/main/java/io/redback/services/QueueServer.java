package io.redback.services;

import java.util.Date;

import io.firebus.Firebus;
import io.firebus.Payload;
import io.firebus.data.DataMap;
import io.firebus.information.ServiceInformation;
import io.redback.exceptions.RedbackException;
import io.redback.security.Session;
import io.redback.services.common.AuthenticatedServiceProvider;

public abstract class QueueServer extends AuthenticatedServiceProvider {

	public QueueServer(String n, DataMap c, Firebus f) {
		super(n, c, f);
	}

	public ServiceInformation getServiceInformation() {
		return null;
	}

	public Payload redbackAuthenticatedService(Session session, Payload payload) throws RedbackException {
		try {
			DataMap requestData = payload.getDataMap();
			String action = requestData.getString("action");
			if(action.equals("enqueue")) {
				String service = requestData.getString("service");
				DataMap message = requestData.getObject("message");
				int requestTimeout = requestData.containsKey("timeout") ? requestData.getNumber("timeout").intValue() : 10000;
				Date schedule = requestData.containsKey("schedule") ? requestData.getDate("schedule") : null;
				String uniqueKey = requestData.containsKey("uniquekey") ? requestData.getString("uniquekey") : null;
				enqueue(session, service, message, requestTimeout, schedule, uniqueKey);
				return new Payload(new DataMap("result", "ok"));
			} else {
				throw new RedbackException("Invalid action: " + action);
			}
		} catch(Exception e) {
			throw new RedbackException("Error in Queue service", e);
		}
	}

	public Payload redbackUnauthenticatedService(Session session, Payload payload) throws RedbackException {
		throw new RedbackException("All requests need to be authenticated");
	}	
	
	protected abstract void enqueue(Session session, String service, DataMap message, int timeout, Date schedule, String uniqueKey) throws RedbackException;
}
