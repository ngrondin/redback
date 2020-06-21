package io.redback.services;

import io.firebus.Firebus;
import io.firebus.Payload;
import io.firebus.exceptions.FunctionErrorException;
import io.firebus.information.ServiceInformation;
import io.firebus.interfaces.ServiceProvider;
import io.firebus.utils.DataMap;

public abstract class CronServer extends Service implements ServiceProvider {

	public CronServer(String n, DataMap c, Firebus f) {
		super(n, c, f);
	}

	public Payload service(Payload payload) throws FunctionErrorException {
		DataMap resp = new DataMap("result", "no actions");
		Payload respPayload = new Payload(resp.toString());
		return respPayload;
	}

	public ServiceInformation getServiceInformation() {
		return null;
	}


}
