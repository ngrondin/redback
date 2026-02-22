package io.redback.services;

import io.firebus.Firebus;
import io.firebus.Payload;
import io.firebus.data.DataMap;
import io.firebus.information.ServiceInformation;
import io.redback.exceptions.RedbackException;
import io.redback.security.Session;
import io.redback.services.common.ServiceProvider;

public abstract class Initializer extends ServiceProvider {
	
	public Initializer(String n, DataMap c, Firebus f) {
		super(n, c, f);
	}	

	public ServiceInformation getServiceInformation() {
		return null;
	}


	protected Payload redbackService(Session session, Payload payload) throws RedbackException {
		return null;
	}
}