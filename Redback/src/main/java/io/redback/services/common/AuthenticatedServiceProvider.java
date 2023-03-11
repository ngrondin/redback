package io.redback.services.common;

import io.firebus.Firebus;
import io.firebus.Payload;
import io.firebus.data.DataMap;
import io.firebus.logging.Logger;
import io.firebus.threads.FirebusThread;
import io.redback.client.AccessManagementClient;
import io.redback.exceptions.RedbackException;
import io.redback.security.Session;
import io.redback.security.UserProfile;

public abstract class AuthenticatedServiceProvider extends ServiceProvider
{
	protected String accessManagementService;
	protected AccessManagementClient accessManagementClient;


	public AuthenticatedServiceProvider(String n, DataMap c, Firebus f)
	{
		super(n, c, f);
		accessManagementService = config.getString("accessmanagementservice");
		accessManagementClient = new AccessManagementClient(firebus, accessManagementService);
	}
	
	protected Payload redbackService(Session session, Payload payload) throws RedbackException {
		Payload response = null;
		String token = payload.metadata.get("token");
		UserProfile up = null;
		
		Logger.finer("rb.authservice.start", new DataMap("token", token));

		if(token != null)
			up = accessManagementClient.validate(session, token);

		if(up != null)
		{			
			session.setUserProfile(up);
			session.setToken(token);
			if(Thread.currentThread() instanceof FirebusThread) 
				((FirebusThread)Thread.currentThread()).setUser(up.getUsername());
			response = redbackAuthenticatedService(session, payload);
		}
		else
		{
			response = redbackUnauthenticatedService(session, payload);
		}
		Logger.finer("rb.authservice.finish", null);
		return response;

	}

	public abstract Payload redbackAuthenticatedService(Session session, Payload payload) throws RedbackException;

	public abstract Payload redbackUnauthenticatedService(Session session, Payload payload) throws RedbackException;

	
}
