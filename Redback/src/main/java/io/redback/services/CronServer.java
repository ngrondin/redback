package io.redback.services;

import io.firebus.Firebus;
import io.firebus.Payload;
import io.firebus.information.ServiceInformation;
import io.firebus.utils.DataMap;
import io.redback.RedbackException;
import io.redback.security.Session;
import io.redback.services.common.ServiceProvider;

public abstract class CronServer extends ServiceProvider {

	public CronServer(String n, DataMap c, Firebus f) {
		super(n, c, f);
	}

	public Payload redbackService(Session session, Payload payload) throws RedbackException {
		DataMap resp = new DataMap("result", "no actions");
		Payload respPayload = new Payload(resp.toString());
		return respPayload;
	}

	public ServiceInformation getServiceInformation() {
		return null;
	}


}
