package io.redback.services.common;

//import java.util.logging.Logger;

import io.firebus.Firebus;
import io.firebus.Payload;
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
	
	protected String getLogline(Payload payload) {
		String mime = payload.metadata.get("mime");
		String body = null;
		if(mime != null && mime.equals("application/json")) {
			body = payload.getString().replaceAll("\r", "").replaceAll("\n", "").replaceAll("\t", "");
			return body;
		} else if(mime != null && mime.equals("text/plain")) {
			body = payload.getString();
		} else {
			body = "";
		}
		return body;
	}

	public abstract void clearCaches();

}
