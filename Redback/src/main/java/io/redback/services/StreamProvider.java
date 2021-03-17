package io.redback.services;


import io.firebus.Firebus;

import io.firebus.utils.DataMap;

public abstract class StreamProvider extends Service implements io.firebus.interfaces.StreamProvider, io.firebus.interfaces.StreamHandler {

	public StreamProvider(String n, DataMap c, Firebus f) {
		super(n, c, f);
	}


	

}
