package io.redback.services;

import java.util.logging.Logger;
import io.firebus.Firebus;
import io.firebus.Payload;
import io.firebus.information.StreamInformation;
import io.firebus.interfaces.Consumer;
import io.firebus.utils.DataMap;
import io.redback.RedbackException;
import io.redback.utils.StringUtils;


public abstract class SignalServer extends AuthenticatedStreamProvider implements Consumer {
	private Logger logger = Logger.getLogger("io.redback");
	
	public SignalServer(String n, DataMap c, Firebus f) {
		super(n, c, f);
		firebus.registerConsumer(serviceName, this, 10);
	}

	public StreamInformation getStreamInformation() {
		return null;
	}


	public void consume(Payload payload) {
		try {
			onSignal(new DataMap(payload.getString()));
		} catch(Exception e) {
			logger.severe(StringUtils.getStackTrace(e));
		}
	}


	protected abstract void onSignal(DataMap signal) throws RedbackException;

}
