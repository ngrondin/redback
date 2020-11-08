package io.redback.services;

//import java.util.logging.Logger;

import io.firebus.Firebus;
import io.firebus.utils.DataMap;

public abstract class Service 
{
	//private Logger logger = Logger.getLogger("io.redback");
	protected String serviceName;
	protected Firebus firebus;
	protected DataMap config;
	
	public Service(String n, DataMap c, Firebus f)
	{
		serviceName = n;
		config = c;
		firebus = f;
	}

	public abstract void clearCaches();

}
