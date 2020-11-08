package io.redback.services;

import java.util.HashMap;
import java.util.Map;

import io.firebus.Firebus;
import io.firebus.Payload;
import io.firebus.StreamEndpoint;
import io.firebus.exceptions.FunctionErrorException;
import io.firebus.information.StreamInformation;
import io.firebus.utils.DataMap;
import io.redback.security.Session;

public abstract class StreamProvider extends Service implements io.firebus.interfaces.StreamProvider, io.firebus.interfaces.StreamHandler {

	public StreamProvider(String n, DataMap c, Firebus f) {
		super(n, c, f);
	}


	

}
